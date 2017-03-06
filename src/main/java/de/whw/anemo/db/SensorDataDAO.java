package de.whw.anemo.db;

import java.io.Closeable;
import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import de.whw.anemo.db.mapper.SensorDataMapper;
import de.whw.anemo.model.SensorData;

@RegisterMapper(SensorDataMapper.class)
public interface SensorDataDAO extends Closeable {

    @SqlQuery("SELECT * from sensor_data")
    List<SensorData> findAll();

    @SqlUpdate("CREATE TABLE IF NOT EXISTS `sensor_data` ( `id` INTEGER, `sensor_name` text NOT NULL, `timestamp` TEXT NOT NULL, `value` REAL NOT NULL, PRIMARY KEY(id))")
    void createSensorDataTable();

    @SqlUpdate("insert into sensor_data (sensor_name, timestamp, value) values (:sensor_name, :timestamp, :value)")
    void insert(@Bind("sensor_name") String sensorName, @Bind("timestamp") long timestamp, @Bind("value") double value);

    @SqlQuery("SELECT * from sensor_data where timestamp > :timestamp")
    List<SensorData> findNewerThan(@Bind("timestamp") long timestamp);

}
