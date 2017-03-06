package de.whw.anemo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.GpioFactory;

import de.whw.anemo.db.JdbcManager;
import de.whw.anemo.sensors.AnemometerSensor;
import de.whw.anemo.sensors.RainSensor;
import de.whw.anemo.sensors.TemperatureSensor;
import de.whw.anemo.sensors.WindDirectionSensor;
import de.whw.anemo.web.AnemometerWebserver;

public class App {

    static {
        BasicConfigurator.configure();
        org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
        org.apache.log4j.Logger.getLogger("de.whw.anemo").setLevel(Level.DEBUG);
        org.apache.log4j.Logger.getLogger("de.whw.anemo.web.AnemoNowWebSocket").setLevel(Level.INFO);
    }

    private static final Logger             log                   = LoggerFactory.getLogger(App.class);

    public static final AnemometerSensor    SENSOR_ANEMOMETER     = new AnemometerSensor();
    public static final WindDirectionSensor SENSOR_WIND_DIRECTION = new WindDirectionSensor();
    public static final RainSensor          SENSOR_RAIN           = new RainSensor();
    public static final TemperatureSensor   SENSOR_TEMPERATURE    = new TemperatureSensor();

    // ADD SENSORS HERE
    private static Sensor[]                 SENSORS               =
        new Sensor[] { SENSOR_ANEMOMETER, SENSOR_RAIN, SENSOR_WIND_DIRECTION, SENSOR_TEMPERATURE };

    private final List<Sensor>              sensors               = new ArrayList<>(Arrays.asList(SENSORS));

    private App() throws IOException {
        super();
        JdbcManager.getInstance().initializeDBTables();
        AnemometerWebserver.start();
        initializeSensors();
    }

    private void initializeSensors() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (WHWEnvironment.PI_ENVIRONMENT) {
                GpioFactory.getInstance().shutdown();
                log.info("Shutting down GPIOs");
            }
        }));
        if (WHWEnvironment.PI_ENVIRONMENT) {
            this.sensors.forEach(Sensor::initialize);
        }
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) throws IOException {
        new App();
    }

}
