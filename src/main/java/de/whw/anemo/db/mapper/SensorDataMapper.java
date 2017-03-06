package de.whw.anemo.db.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import de.whw.anemo.model.SensorData;

public class SensorDataMapper implements ResultSetMapper<SensorData> {

    public SensorDataMapper() {
        super();
    }

    @Override
    public SensorData map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        SensorData sensorData = new SensorData();
        sensorData.setId(r.getLong("id"));
        sensorData.setSensorName(r.getString("sensor_name"));
        sensorData.setTimestamp(Instant.ofEpochMilli(r.getLong("timestamp")));
        sensorData.setValue(r.getDouble("value"));
        return sensorData;
    }

}
