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

#include "GenericEntryFilter.h"
#include "GenericLoggingCore.h"
#include "GenericSink.h"

#include "Logger.h"
#include "Metadata.h"
#include "MetadataFormatter.h"


namespace mm
{
namespace logging
{

typedef internal::GenericLoggingCore<LoggerData, EntryData, StampData>
   LoggingCore;

typedef LoggingCore::SinkType LogSink;
typedef internal::GenericStdErrLogSink<LoggingCore::MetadataType,
        internal::MetadataFormatter> StdErrLogSink;
typedef internal::GenericFileLogSink<LoggingCore::MetadataType,
        internal::MetadataFormatter> FileLogSink;

typedef internal::GenericEntryFilter<LoggingCore::MetadataType> EntryFilter;

} // namespace logging
} // namespace mm
