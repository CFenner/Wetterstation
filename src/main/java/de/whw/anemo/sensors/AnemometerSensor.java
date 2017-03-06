package de.whw.anemo.sensors;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import de.whw.anemo.Sensor;
import de.whw.anemo.WHWConstants;
import de.whw.anemo.db.InMemoryDatabase;

public class AnemometerSensor implements Sensor, GpioPinListenerDigital {

    @SuppressWarnings("unused")
    private static final Logger  log         = LoggerFactory.getLogger(AnemometerSensor.class);

    public static final String   NAME        = "ANEMO";

    private boolean              initialized = false;

    private GpioPinDigitalOutput LED;
    private GpioPinDigitalInput  anemometerInput;

    private long                 last        = 0;

    @Override
    public void initialize() {

        if (this.initialized) {
            throw new IllegalStateException("sensor " + getClass() + " already initialized");
        }
        this.initialized = true;

        // configure GPIO pins
        final GpioController gpio = GpioFactory.getInstance();
        this.anemometerInput = gpio.provisionDigitalInputPin(WHWConstants.SENSOR_ANEMOMETER_PIN);
        this.LED = gpio.provisionDigitalOutputPin(WHWConstants.OUTPUT_LED_PIN, "MyLED", PinState.HIGH);

        gpio.export(PinMode.DIGITAL_INPUT, this.anemometerInput);

        // set shutdown state for this input pin
        this.anemometerInput.setShutdownOptions(true);
        this.LED.setShutdownOptions(true);

        // create and register gpio pin listener
        this.anemometerInput.addListener(this);

    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        this.LED.setState(event.getState());
        if (event.getState() == PinState.HIGH) {
            long now = System.currentTimeMillis();
            if (this.last == 0) {
                this.last = now;
                return;
            }
            InMemoryDatabase.getInstance().store(getSensorName(), Instant.now(), now - this.last);
            this.last = now;
        }
    }

    @Override
    public String getSensorName() {
        return NAME;
    }

}
