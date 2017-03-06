package de.whw.anemo.sensors;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import de.whw.anemo.Sensor;
import de.whw.anemo.WHWConstants;
import de.whw.anemo.db.InMemoryDatabase;

public class RainSensor implements Sensor, GpioPinListenerDigital {

    private static final Logger log         = LoggerFactory.getLogger(RainSensor.class);

    private static final String NAME        = "RAIN";

    private boolean             initialized = false;

    private GpioPinDigitalInput rainInput;

    @Override
    public void initialize() {

        if (this.initialized) {
            throw new IllegalStateException("sensor " + getClass() + " already initialized");
        }
        this.initialized = true;

        // configure GPIO pins
        final GpioController gpio = GpioFactory.getInstance();
        this.rainInput = gpio.provisionDigitalInputPin(WHWConstants.SENSOR_RAIN_PIN);

        gpio.export(PinMode.DIGITAL_INPUT, this.rainInput);

        // set shutdown state for this input pin
        this.rainInput.setShutdownOptions(true);

        // create and register gpio pin listener
        this.rainInput.addListener(this);

    }

    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (event.getState() == PinState.HIGH) {
            log.debug("pin: {}, state: {}", event.getPin().getName(), event.getState().toString());
            InMemoryDatabase.getInstance().store(getSensorName(), Instant.now(), 1);
        }
    }

    @Override
    public String getSensorName() {
        return NAME;
    }

}
