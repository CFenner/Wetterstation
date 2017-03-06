package de.whw.anemo.sensors;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.gpio.extension.ads.ADS1115GpioProvider;
import com.pi4j.gpio.extension.ads.ADS1115Pin;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;

import de.whw.anemo.ADS1115GpioProviderInstance;
import de.whw.anemo.Sensor;
import de.whw.anemo.db.InMemoryDatabase;

public class WindDirectionSensor implements Sensor {

    public static final String  NAME        = "WINDDIR";
    private static final Logger log         = LoggerFactory.getLogger(WindDirectionSensor.class);

    private boolean             initialized = false;

    @Override
    public void initialize() {
        if (this.initialized) {
            throw new IllegalStateException("sensor " + getClass() + " already initialized");
        }
        this.initialized = true;

        ADS1115GpioProviderInstance.getInstance().setEventThreshold(500, ADS1115Pin.INPUT_A0);

        ADS1115GpioProviderInstance.ADS115INPUT_A0.addListener(new GpioPinListenerAnalog() {

            @Override
            public void handleGpioPinAnalogValueChangeEvent(GpioPinAnalogValueChangeEvent event) {
                // RAW value
                double value = event.getValue();

                // percentage
                double percent = ((value * 100) / ADS1115GpioProvider.ADS1115_RANGE_MAX_VALUE);

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
                         + value
                         + "       ");
                float r1 = 10_000f;
                for (ResistanceMapping r : MAPPINGS) {
                    float r2 = r.resistance;
                    float v = (float) 3.3 / (r1 + r2) * r2;
                    if (voltage + 0.01 > v && voltage - 0.01 < v) {
                        InMemoryDatabase.getInstance().store(getSensorName(), Instant.now(), r.direction);
                    }
                }
            }
        });

    }

    @Override
    public String getSensorName() {
        return NAME;
    }

    private static final class ResistanceMapping {

        public final float direction;
        public final int   resistance;

        public ResistanceMapping(float direction, int resistance) {
            super();
            this.direction = direction;
            this.resistance = resistance;
        }

    }

    private static final ResistanceMapping[] MAPPINGS = { new ResistanceMapping(0, 33_000),
                                                          new ResistanceMapping(22.5f, 6_570),
                                                          new ResistanceMapping(45, 8_200),
                                                          new ResistanceMapping(67.5f, 891),
                                                          new ResistanceMapping(90, 1_000),
                                                          new ResistanceMapping(112.5f, 688),
                                                          new ResistanceMapping(135, 2_200),
                                                          new ResistanceMapping(157.5f, 1_410),
                                                          new ResistanceMapping(180, 3_900),
                                                          new ResistanceMapping(202.5f, 3_140),
                                                          new ResistanceMapping(225, 16_000),
                                                          new ResistanceMapping(247.5f, 14_120),
                                                          new ResistanceMapping(270, 120_000),
                                                          new ResistanceMapping(292.5f, 42_120),
                                                          new ResistanceMapping(315, 64_900),
                                                          new ResistanceMapping(337.5f, 21_880) };

}
