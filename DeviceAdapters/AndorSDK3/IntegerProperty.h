#ifndef _INTEGERPROPERTY_H_
#define _INTEGERPROPERTY_H_

#include "atcore++.h"

using namespace andor;

class MySequenceThread;
class CAndorSDK3Camera;

class TIntegerProperty : public IObserver
{
public:
   TIntegerProperty(const std::string MM_name, IInteger* integer_feature,
                    CAndorSDK3Camera* camera, MySequenceThread* thd,
                    SnapShotControl* snapShotController, bool readOnly, bool limited_);
   ~TIntegerProperty();

   void Update(ISubject* Subject);
   int OnInteger(MM::PropertyBase* pProp, MM::ActionType eAct);
   typedef MM::Action<TIntegerProperty> CPropertyAction;
   int Get(){return static_cast<int>(integer_feature_->Get());}

private:
   IInteger* integer_feature_;
   CAndorSDK3Camera* camera_;
   std::string MM_name_;
   MySequenceThread * thd_;
   SnapShotControl* snapShotController_;
   bool limited_;
};

#endif // _INTEGERPROPERTY_H_