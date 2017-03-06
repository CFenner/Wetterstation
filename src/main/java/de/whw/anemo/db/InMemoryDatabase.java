package de.whw.anemo.db;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.whw.anemo.model.SensorData;

public class InMemoryDatabase extends Thread {

    private static final Logger                 log       = LoggerFactory.getLogger(InMemoryDatabase.class);

    private final Map<String, List<SensorData>> db        = Collections.synchronizedMap(new HashMap<>());

    private static final InMemoryDatabase       _instance = new InMemoryDatabase();

    private InMemoryDatabase() {
        start();
    }

    public static InMemoryDatabase getInstance() {
        return _instance;
    }

    public void store(String sensorName, Instant timestamp, double value) {
        this.db.compute(sensorName, (key, val) -> {
            SensorData data = new SensorData();
            data.setSensorName(sensorName);
            data.setTimestamp(timestamp);
            data.setValue(value);
            log.info("store: {}", data);

            if (val == null) {
                List<SensorData> result = new ArrayList<>();
                result.add(data);
                return result;
            }
            val.add(data);
            return val;

        });
    }

    @Override
    public void run() {

        SensorDataDAO dao = JdbcManager.getInstance().getDbi().open(SensorDataDAO.class);

        AtomicInteger datapointCounter = new AtomicInteger(0);
        // populate database with values from last 15 minutes if existent
        for (SensorData data : dao.findNewerThan(Instant.now().minus(15, ChronoUnit.MINUTES).toEpochMilli())) {
            this.db.compute(data.getSensorName(), (key, val) -> {
                if (val == null) {
                    List<SensorData> result = new ArrayList<>();
                    result.add(data);
                    return result;
                }
                val.add(data);
                datapointCounter.incrementAndGet();
                return val;
            });
        }

        log.info("Prepopulated in memory database with {} datapoints", datapointCounter.get());

        while (true) {
            try {
                Thread.sleep(Duration.of(1, ChronoUnit.MINUTES).toMillis());
            }
            catch (InterruptedException e) {
                // ignore
            }
            Instant _15MinutesAgo = Instant.now().minus(15, ChronoUnit.MINUTES);
            this.db.forEach((sensorName, data) -> {
                for (Iterator<SensorData> iterator = data.iterator(); iterator.hasNext();) {
                    SensorData sensorData = iterator.next();
                    if (sensorData.getTimestamp().isBefore(_15MinutesAgo)) {
                        iterator.remove();
                    }
                    if (sensorData.getId() == 0l) {
                        log.debug("moving sensor data from memory to database: {}", sensorData);
                        dao.insert(sensorData.getSensorName(),
                                   sensorData.getTimestamp().toEpochMilli(),
                                   sensorData.getValue());
                    }

                }
            });
        }
    }

    public Optional<SensorData> getLastValue(String name) {
        if (this.db.containsKey(name) == false) {
            return Optional.empty();
        }
        return this.db.get(name).stream().sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())).findFirst();
    }

}
