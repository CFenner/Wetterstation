package de.whw.anemo.web;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.whw.anemo.db.InMemoryDatabase;
import de.whw.anemo.model.SensorData;
import de.whw.anemo.sensors.AnemometerSensor;
import de.whw.anemo.sensors.TemperatureSensor;
import de.whw.anemo.sensors.WindDirectionSensor;

@WebSocket
public class AnemoNowWebSocket {

    private static final Logger         log      = LoggerFactory.getLogger(AnemoNowWebSocket.class);

    // Store sessions if you want to, for example, broadcast a message to all users
    private static final Queue<Session> sessions = new ConcurrentLinkedQueue<>();

    @OnWebSocketConnect
    public void connected(Session session) {
        log.debug("_WS_ connected");
        sessions.add(session);
    }

    @OnWebSocketClose
    public void closed(Session session,
                       @SuppressWarnings("unused") int statusCode,
                       @SuppressWarnings("unused") String reason) {
        log.debug("_WS_ disconnected");
        sessions.remove(session);
    }

    @OnWebSocketMessage
    public void message(Session session, String message) throws IOException {
        log.debug("_WS_ message: {}", message);

        Optional<SensorData> lastValue;
        String result = "ANEMO,";
        lastValue = InMemoryDatabase.getInstance().getLastValue(AnemometerSensor.NAME);

        if (lastValue.isPresent()) {
            result += lastValue.get().getTimestamp().toEpochMilli() + "," + (2.4d * 1000d / lastValue.get().getValue());
        }
        else {
            result += Instant.now().toEpochMilli() + "," + 0.0;
        }

        result += "\nWINDDIR,";
        lastValue = InMemoryDatabase.getInstance().getLastValue(WindDirectionSensor.NAME);

        if (lastValue.isPresent()) {
            result += lastValue.get().getTimestamp().toEpochMilli() + "," + lastValue.get().getValue();
        }
        else {
            result += Instant.now().toEpochMilli() + ",?";
        }

        result += "\nTEMP,";
        lastValue = InMemoryDatabase.getInstance().getLastValue(TemperatureSensor.NAME);
        if (lastValue.isPresent()) {
            result += lastValue.get().getTimestamp().toEpochMilli() + "," + lastValue.get().getValue();
        }
        else {
            result += Instant.now().toEpochMilli() + ",?";
        }
        session.getRemote().sendString(result); // and send it back

    }

}
