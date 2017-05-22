package de.whw.anemo;

import com.pi4j.gpio.extension.ads.ADS1115Pin;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

public interface WHWConstants {

    // GPIO Pins are numbered by WiringPI numbers, see:
    // https://pinout.xyz/pinout/pin8_gpio14
    public static final Pin OUTPUT_LED_PIN        = RaspiPin.GPIO_01;

    public static final Pin SENSOR_ANEMOMETER_PIN = RaspiPin.GPIO_04;
    public static final Pin SENSOR_RAIN_PIN       = RaspiPin.GPIO_05;

    // These are the pins on the ADS1115 analog to digital converter
    public static final Pin SENSOR_ADS1115_A0     = ADS1115Pin.INPUT_A0;
    public static final Pin SENSOR_ADS1115_A1     = ADS1115Pin.INPUT_A1;

}
