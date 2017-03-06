package de.whw.anemo.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcManager {

    private static final Logger log       = LoggerFactory.getLogger(JdbcManager.class);

    private static JdbcManager  _instance = null;

    private final DBI           dbi;

    private JdbcManager(DBI dbi) {
        this.dbi = dbi;
    }

    public static JdbcManager getInstance() {
        if (_instance == null) {
            synchronized (JdbcManager.class) {
                if (_instance == null) {
                    try {
                        Class.forName("org.sqlite.JDBC");
                    }
                    catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    _instance = new JdbcManager(new DBI(new ConnectionFactory() {

                        @Override
                        public Connection openConnection() throws SQLException {
                            String url = "jdbc:sqlite:anemo.db";
                            Connection con = DriverManager.getConnection(url);
                            return con;
                        }
                    }));

                }
            }
        }
        return _instance;
    }

    public void initializeDBTables() throws IOException {
        try (Handle handle = JdbcManager.getInstance().getDbi().open()) {
            try (SensorDataDAO dao = handle.attach(SensorDataDAO.class)) {
                dao.createSensorDataTable();
            }
        }
    }

    public void backupDatabase(String backupName) {
        log.info("creating database backup");
        try (Handle handle = JdbcManager.getInstance().getDbi().open()) {
            try (Statement stmt = handle.getConnection().createStatement()) {
                stmt.executeUpdate("backup to " + backupName);
                log.info("database backup created to " + backupName);
            }
            catch (SQLException e) {
                log.error("Could not create database backup to " + backupName, e);
            }
        }
    }

    public void restoreDatabase(String backupName) {
        log.info("Restoring database...");

        if (new File(backupName).exists() == false) {
            log.info("no backup db found, starting from scratch");
            return;
        }

        try (Handle handle = JdbcManager.getInstance().getDbi().open()) {
            try (Statement stmt = handle.getConnection().createStatement()) {
                stmt.executeUpdate("restore from " + backupName);
            }
            catch (SQLException e) {
                log.error("Could not restore database backup from " + backupName, e);
            }
        }
    }

    public DBI getDbi() {
        return this.dbi;
    }

}
