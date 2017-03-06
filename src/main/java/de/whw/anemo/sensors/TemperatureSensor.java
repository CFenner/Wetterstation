package de.whw.anemo.sensors;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.gpio.extension.ads.ADS1115GpioProvider;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;

import de.whw.anemo.ADS1115GpioProviderInstance;
import de.whw.anemo.Sensor;
import de.whw.anemo.WHWConstants;
import de.whw.anemo.db.InMemoryDatabase;

public class TemperatureSensor implements Sensor {

    private static final Logger log  = LoggerFactory.getLogger(TemperatureSensor.class);

    public static final String  NAME = "TEMP";

    @Override
    public void initialize() {

        ADS1115GpioProviderInstance.getInstance().setEventThreshold(25, WHWConstants.SENSOR_ADS1115_A1);

        // create analog pin value change listener
        ADS1115GpioProviderInstance.ADS115INPUT_A1.addListener(new GpioPinListenerAnalog() {

            @Override
            public void handleGpioPinAnalogValueChangeEvent(GpioPinAnalogValueChangeEvent event) {
                // RAW value
                double analogVal = event.getValue();

                // percentage
                double percent = ((analogVal * 100) / ADS1115GpioProvider.ADS1115_RANGE_MAX_VALUE);

                // approximate voltage ( *scaled based on PGA setting )
                double voltage =
                    ADS1115GpioProviderInstance.getInstance().getProgrammableGainAmplifier(event.getPin()).getVoltage()
                                 * (percent / 100);

                // display output
                log.info(" ("
                         + event.getPin().getName()
                         + ") : VOLTS="
                         + voltage
                         + "  | PERCENT="
                         + percent
                         + "% | RAW="
                         + analogVal
                         + "       ");

                double TEMP;
                // magic numbers, found while googling around
                TEMP = Math.log(10000.0 * ((3.3 / voltage - 1)));
                TEMP = 1 / (0.001129148 + (0.000234125 + (0.0000000876741 * TEMP * TEMP)) * TEMP);
                TEMP = TEMP - 273.15;
                log.info("TEMP: " + TEMP);

                InMemoryDatabase.getInstance().store(getSensorName(), Instant.now(), TEMP);

            }
        });

    }

    @Override
    public String getSensorName() {
        return NAME;
    }

}
