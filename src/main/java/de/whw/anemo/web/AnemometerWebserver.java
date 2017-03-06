package de.whw.anemo.web;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.whw.anemo.db.JdbcManager;
import de.whw.anemo.db.mapper.SensorDataMapper;
import de.whw.anemo.model.SensorData;
import spark.Request;
import spark.Response;
import spark.Spark;

public class AnemometerWebserver {

    private static final Logger log = LoggerFactory.getLogger(AnemometerWebserver.class);

    @SuppressWarnings("unused")
    public static void start() {
        new AnemometerWebserver();
    }

    public AnemometerWebserver() {
        log.info("Starting webserver...");
        Spark.port(8080);
        Spark.webSocket("/socket/anemonow", AnemoNowWebSocket.class);
        Spark.staticFiles.externalLocation("www-data");
        Spark.get("/rest/anemometer", this::anemometer);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Stopping webserver");
            AnemometerWebserver.this.stop();
        }));
        log.info("Webserver started...");
    }

    public Object anemometer(Request req, @SuppressWarnings("unused") Response res) {

        return JdbcManager.getInstance().getDbi().withHandle((handle) -> {

            long now = System.currentTimeMillis();
            // t = minutes in the past to observe
            String t = req.queryParams("t");
            int minutes = 0;
            try {
                minutes = Integer.parseInt(t);
            }
            catch (NumberFormatException e) {
                // ignore
            }
            if (minutes <= 4 || minutes > Duration.of(365, ChronoUnit.DAYS).toMinutes()) {
                minutes = 5;
            }

            int datapoints = 20;

            long start = now - (minutes * 60 * 1000);
            long step = (now - start) / datapoints;

            List<SensorData> data =
                handle.createQuery("select * from sensor_data where timestamp > :timestamp and sensor_name = :sensor_name ORDER BY timestamp DESC")
                      .bind("timestamp", System.currentTimeMillis() - (minutes * 60 * 1000))
                      .bind("sensor_name", "anemometer").map(new SensorDataMapper()).list();

            Map<Long, List<SensorData>> collect = data.stream().collect(Collectors.groupingBy((s) -> {
                return (s.getTimestamp().toEpochMilli() - start) / step;
            }));

            StringBuilder result = new StringBuilder();
            collect.forEach((k, v) -> {
                OptionalDouble avg = v.stream().mapToDouble(SensorData::getValue).average();
                double val = 0;
                if (avg.isPresent() && Double.compare(0d, avg.getAsDouble()) != 0) {
                    val = 2.4d * 1000d / avg.getAsDouble();
                }
                result.append(start + k * step);
                result.append(',');
                result.append(val);
                result.append('\n');
            });
            return result.toString();
        });

    }

    public void stop() {
        Spark.stop();
    }

}
