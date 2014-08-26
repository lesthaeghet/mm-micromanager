// COPYRIGHT:     University of California, San Francisco, 2014,
//                All Rights reserved
//
// LICENSE:       This file is distributed under the "Lesser GPL" (LGPL) license.
//                License text is included with the source distribution.
//
//                This file is distributed in the hope that it will be useful,
//                but WITHOUT ANY WARRANTY; without even the implied warranty
//                of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//                IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//                CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//                INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
// AUTHOR:        Mark Tsuchida

#pragma once

#include "GenericLinePacket.h"
#include "GenericLogger.h"
#include "GenericMetadata.h"
#include "GenericPacketQueue.h"
#include "GenericSink.h"

#include <boost/bind.hpp>
#include <boost/container/vector.hpp>
#include <boost/enable_shared_from_this.hpp>
#include <boost/shared_ptr.hpp>
#include <boost/thread.hpp>

#include <algorithm>
#include <set>
#include <string>
#include <vector>

namespace mm
{
namespace logging
{


enum SinkMode
{
   SinkModeSynchronous,
   SinkModeAsynchronous,
};


namespace detail
{


template <
   typename TLoggerData,
   typename UEntryData,
   typename VStampData
>
class GenericLoggingCore :
   public boost::enable_shared_from_this<
      GenericLoggingCore<TLoggerData, UEntryData, VStampData>
   >
{
public:
   typedef GenericMetadata<TLoggerData, UEntryData, VStampData> MetadataType;
   typedef detail::GenericSink<MetadataType> SinkType;

private:
   typedef detail::GenericLinePacket<MetadataType> LogLineType;
   typedef boost::container::vector<LogLineType> LineVectorType;

   // When acquiring both syncSinksMutex_ and asyncQueueMutex_, acquire in that
   // order.

   boost::mutex syncSinksMutex_; // Protect all access to synchronousSinks_
   std::vector< boost::shared_ptr<SinkType> > synchronousSinks_;

   boost::mutex asyncQueueMutex_; // Protect start/stop and sinks change
   detail::GenericPacketQueue<LogLineType> asyncQueue_;
   // Changes to asynchronousSinks_ must be made with asyncQueueMutex_ held
   // _and_ the queue receive loop stopped.
   std::vector< boost::shared_ptr<SinkType> > asynchronousSinks_;

public:
   GenericLoggingCore() { StartAsyncReceiveLoop(); }
   ~GenericLoggingCore() { StopAsyncReceiveLoop(); }

   /**
    * Create a new logger.
    *
    * Loggers are callables taking the entry metadata and entry text.
    */
   detail::GenericLogger<UEntryData> NewLogger(TLoggerData metadata)
   {
      // Loggers hold a shared pointer to the LoggingCore, so that they are
      // guaranteed to be safe to call at any time.
      return detail::GenericLogger<UEntryData>(
            boost::bind<void>(&GenericLoggingCore::SendEntryToShared,
               this->shared_from_this(), metadata, _1, _2));
   }

   /**
    * Add a synchronous or asynchronous sink.
    */
   void AddSink(boost::shared_ptr<SinkType> sink, SinkMode mode)
   {
      switch (mode)
      {
         case SinkModeSynchronous:
         {
            boost::lock_guard<boost::mutex> lock(syncSinksMutex_);
            synchronousSinks_.push_back(sink);
            break;
         }
         case SinkModeAsynchronous:
         {
            boost::lock_guard<boost::mutex> lock(asyncQueueMutex_);
            StopAsyncReceiveLoop();
            asynchronousSinks_.push_back(sink);
            StartAsyncReceiveLoop();
            break;
         }
      }
   }

   /**
    * Remove a synchronous or asynchronous sink.
    *
    * Nothing is done if the sink is not registered with the specified
    * concurrency mode.
    */
   void RemoveSink(boost::shared_ptr<SinkType> sink, SinkMode mode)
   {
      switch (mode)
      {
         case SinkModeSynchronous:
         {
            boost::lock_guard<boost::mutex> lock(syncSinksMutex_);
            typename std::vector< boost::shared_ptr<SinkType> >::iterator it =
               std::find(synchronousSinks_.begin(), synchronousSinks_.end(),
                     sink);
            if (it != synchronousSinks_.end())
               synchronousSinks_.erase(it);
            break;
         }
         case SinkModeAsynchronous:
         {
            boost::lock_guard<boost::mutex> lock(asyncQueueMutex_);
            StopAsyncReceiveLoop();
            typename std::vector< boost::shared_ptr<SinkType> >::iterator it =
               std::find(asynchronousSinks_.begin(), asynchronousSinks_.end(),
                     sink);
            if (it != asynchronousSinks_.end())
               asynchronousSinks_.erase(it);
            StartAsyncReceiveLoop();
            break;
         }
      }
   }

   /**
    * Add and/or remove multiple sinks, atomically.
    *
    * This function should be used for example to switch files when performing
    * log rotation, because it ensures that no entries are lossed during the
    * switch.
    *
    * SinkModePairIterator should be an iterator type over
    * std::pair<boost::shared_ptr<SinkType>, SinkMode>.
    */
   template <typename SinkModePairIterator>
   void AtomicSwapSinks(SinkModePairIterator firstToRemove,
         SinkModePairIterator lastToRemove,
         SinkModePairIterator firstToAdd,
         SinkModePairIterator lastToAdd)
   {
      // Lock both sink lists in the designated order. Since locking
      // syncSinksMutex_ causes logging to block, subsequently draining the
      // async queue by stopping the receive loop causes all sinks to
      // synchronize (emit up to the same log entry).
      boost::lock_guard<boost::mutex> lockSyncs(syncSinksMutex_);
      boost::lock_guard<boost::mutex> lockAsyncQ(asyncQueueMutex_);
      StopAsyncReceiveLoop();

      // Inefficient nested loop, but good enough for this purpose.
      for (SinkModePairIterator it = firstToRemove; it != lastToRemove; ++it)
      {
         typedef std::vector< boost::shared_ptr<SinkType> > SinkListType;
         SinkListType* pSinkList = 0;
         switch (it->second)
         {
            case SinkModeSynchronous:
               pSinkList = &synchronousSinks_;
               break;
            case SinkModeAsynchronous:
               pSinkList = &asynchronousSinks_;
               break;
         }
         typename SinkListType::iterator foundIt =
            std::find(pSinkList->begin(), pSinkList->end(), it->first);
         if (foundIt != pSinkList->end())
            pSinkList->erase(foundIt);
      }

      for (SinkModePairIterator it = firstToAdd; it != lastToAdd; ++it)
      {
         switch (it->second)
         {
            case SinkModeSynchronous:
               synchronousSinks_.push_back(it->first);
               break;
            case SinkModeAsynchronous:
               asynchronousSinks_.push_back(it->first);
               break;
         }
      }

      StartAsyncReceiveLoop();
   }

   /**
    * Set log entry filters for attached sinks, atomically.
    *
    * SinkModePairFilterPairIterator must be an iterator type over
    * std::pair<
    *    std::pair<boost::shared_ptr<SinkType>, SinkMode>,
    *    boost::shared_ptr<SinkType::FilterType>
    * >.
    */
   template <typename SinkModePairFilterPairIterator>
   void AtomicSetSinkFilters(SinkModePairFilterPairIterator first,
         SinkModePairFilterPairIterator last)
   {
      boost::lock_guard<boost::mutex> lockSyncs(syncSinksMutex_);
      boost::lock_guard<boost::mutex> lockAsyncQ(asyncQueueMutex_);
      StopAsyncReceiveLoop();

      for (SinkModePairFilterPairIterator it = first; it != last; ++it)
      {
         boost::shared_ptr<SinkType> sink = it->first.first;
         SinkMode mode = it->first.second;
         boost::shared_ptr< detail::GenericEntryFilter<MetadataType> > filter =
            it->second;

         typedef std::vector< boost::shared_ptr<SinkType> > SinkListType;
         SinkListType* pSinkList = 0;
         switch (mode)
         {
            case SinkModeSynchronous:
               pSinkList = &synchronousSinks_;
               break;
            case SinkModeAsynchronous:
               pSinkList = &asynchronousSinks_;
               break;
         }
         typename SinkListType::iterator foundIt =
            std::find(pSinkList->begin(), pSinkList->end(), sink);
         if (foundIt != pSinkList->end())
            (*foundIt)->SetFilter(filter);
      }

      StartAsyncReceiveLoop();
   }

private:
   // Static wrapper allowing the use of a shared_ptr for the target instance
   static void
   SendEntryToShared(boost::shared_ptr<GenericLoggingCore> self,
         TLoggerData loggerData, UEntryData entryData, const char* entryText)
   { self->SendEntry(loggerData, entryData, entryText); }

   void SendEntry(TLoggerData loggerData, UEntryData entryData,
         const char* entryText)
   {
      VStampData stampData;
      stampData.Stamp();

      LineVectorType lines;
      SplitEntryIntoLines<MetadataType>(lines, loggerData, entryData,
            stampData, entryText);

      {
         boost::lock_guard<boost::mutex> lock(syncSinksMutex_);

         for (typename std::vector< boost::shared_ptr<SinkType> >::iterator
               it = synchronousSinks_.begin(), end = synchronousSinks_.end();
               it != end; ++it)
         {
            (*it)->Consume(lines);
         }
      }
      asyncQueue_.SendLines(lines.begin(), lines.end());
   }

   // Called on the receive thread of GenericPacketQueue
   void RunAsynchronousSinks(LineVectorType& lines)
   {
      for (typename std::vector< boost::shared_ptr<SinkType> >::iterator
            it = asynchronousSinks_.begin(), end = asynchronousSinks_.end();
            it != end; ++it)
      {
         (*it)->Consume(lines);
      }
   }

   void StartAsyncReceiveLoop()
   {
      asyncQueue_.RunReceiveLoop(
            boost::bind<void>(&GenericLoggingCore::RunAsynchronousSinks,
               this, _1));
   }

   void StopAsyncReceiveLoop()
   {
      asyncQueue_.ShutdownReceiveLoop();
   }
};


} // namespace detail
} // namespace logging
} // namespace mm
