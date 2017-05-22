package de.whw.anemo;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.gpio.extension.ads.ADS1115GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1115Pin;
import com.pi4j.gpio.extension.ads.ADS1x15GpioProvider.ProgrammableGainAmplifierValue;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class ADS1115GpioProviderInstance {

    private static final Logger            log            = LoggerFactory.getLogger(ADS1115GpioProviderInstance.class);

    private static ADS1115GpioProvider     instance       = null;

    public static final GpioPinAnalogInput ADS115INPUT_A0 =
        GpioFactory.getInstance().provisionAnalogInputPin(getInstance(), ADS1115Pin.INPUT_A0, "AnalogInput-A0");
    public static final GpioPinAnalogInput ADS115INPUT_A1 =
        GpioFactory.getInstance().provisionAnalogInputPin(getInstance(), ADS1115Pin.INPUT_A1, "AnalogInput-A1");
    public static final GpioPinAnalogInput ADS115INPUT_A2 =
        GpioFactory.getInstance().provisionAnalogInputPin(getInstance(), ADS1115Pin.INPUT_A2, "AnalogInput-A2");
    public static final GpioPinAnalogInput ADS115INPUT_A3 =
        GpioFactory.getInstance().provisionAnalogInputPin(getInstance(), ADS1115Pin.INPUT_A3, "AnalogInput-A3");

    public static synchronized ADS1115GpioProvider getInstance() {
        while (instance == null) {
            try {
                instance = new ADS1115GpioProvider(I2CBus.BUS_1, ADS1115GpioProvider.ADS1115_ADDRESS_0x48);
            }
            catch (UnsupportedBusNumberException | IOException e) {
                log.error("could not initialize ADS1115 on I2C Bus...", e);
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException ignore) {
                    return null;
                }
            }
            // ATTENTION !!
            // It is important to set the PGA (Programmable Gain Amplifier) for all analog input pins.
            // (You can optionally set each input to a different value)
            // You measured input voltage should never exceed this value!
            //
            // In my testing, I am using a Sharp IR Distance Sensor (GP2Y0A21YK0F) whose voltage never exceeds 3.3 VDC
            // (http://www.adafruit.com/products/164)
            //
            // PGA value PGA_4_096V is a 1:1 scaled input,
            // so the output values are in direct proportion to the detected voltage on the input pins
            instance.setProgrammableGainAmplifier(ProgrammableGainAmplifierValue.PGA_6_144V, ADS1115Pin.ALL);

            // Define a threshold value for each pin for analog value change events to be raised.
            // It is important to set this threshold high enough so that you don't overwhelm your program with change events for insignificant changes
            instance.setEventThreshold(500, ADS1115Pin.ALL);

            // Define the monitoring thread refresh interval (in milliseconds).
            // This governs the rate at which the monitoring thread will read input values from the ADC chip
            // (a value less than 50 ms is not permitted)
            instance.setMonitorInterval(100);
        }

        return instance;
    }

}
