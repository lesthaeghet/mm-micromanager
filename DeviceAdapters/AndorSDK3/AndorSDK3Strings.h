#ifndef ANDORSDK3STRINGS_H_
#define ANDORSDK3STRINGS_H_

#include <string>

class TAndorSDK3Strings
{
public:
   static const std::string PIXEL_READOUT_RATE;
   static const std::string ELECTRONIC_SHUTTERING_MODE;
   static const std::string TEMPERATURE_CONTROL;
   static const std::string PIXEL_ENCODING;
   static const std::string ACCUMULATE_COUNT;
   static const std::string TEMPERATURE_STATUS;
   static const std::string FAN_SPEED;
   static const std::string SENSOR_TEMPERATURE;
   static const std::string SPURIOUS_NOISE_FILTER;
   static const std::string SENSOR_COOLING;
   static const std::string OVERLAP;
   static const std::string FRAME_RATE;
};

const std::string TAndorSDK3Strings::PIXEL_READOUT_RATE("PixelReadoutRate");
const std::string TAndorSDK3Strings::ELECTRONIC_SHUTTERING_MODE("ElectronicShutteringMode");
const std::string TAndorSDK3Strings::TEMPERATURE_CONTROL("TemperatureControl");
const std::string TAndorSDK3Strings::PIXEL_ENCODING("PixelEncoding");
const std::string TAndorSDK3Strings::ACCUMULATE_COUNT("AccumulateCount");
const std::string TAndorSDK3Strings::TEMPERATURE_STATUS("TemperatureStatus");
const std::string TAndorSDK3Strings::FAN_SPEED("FanSpeed");
const std::string TAndorSDK3Strings::SENSOR_TEMPERATURE("SensorTemperature");
const std::string TAndorSDK3Strings::SPURIOUS_NOISE_FILTER("SpuriousNoiseFilter");
const std::string TAndorSDK3Strings::SENSOR_COOLING("SensorCooling");
const std::string TAndorSDK3Strings::OVERLAP("Overlap");
const std::string TAndorSDK3Strings::FRAME_RATE("FrameRate");

#endif //include only once

