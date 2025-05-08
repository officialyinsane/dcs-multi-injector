package uk.co.obora.dcs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;
import uk.co.obora.dcs.dto.InboundPacket;
import uk.co.obora.dcs.dto.OutboundPacket;
import uk.co.obora.dcs.entity.DcsServer;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class Injector {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int DEFAULT_TIMEOUT_SECS = 3;

    public static InboundPacket doInjection(DcsServer server, String code) throws JsonProcessingException {
        TcpClient client = TcpClient.create()
            .host(server.getHostname())
            .port(server.getPort())
            .doOnConnected(connection -> log.info("Connected to DCS server {}", server.getName()));

        AtomicReference<Throwable> error = new AtomicReference<>();

        Mono<InboundPacket> response = client.connect()
            .doOnError(t -> handleError(t, "Failed to connect to DCS server " + server.getName(), error))
            .flatMap(connection -> {
                try {
                    return Mono.just(doServerCommunication(connection, server, code));
                } catch (Throwable t) {
                    handleError(t, "Failed to communicate with DCS server " + server.getName(), error);
                }
                return Mono.empty();
            });

        if (error.get() != null) {
            throw new RuntimeException("Encountered an error talking with server: " + server.getName(), error.get());
        }

        return response.block();
    }

    private static InboundPacket doServerCommunication(Connection connection, DcsServer server, String code) throws Throwable {
        OutboundPacket packet = OutboundPacket.builder()
            .type("lua")
            .script(code)
            .build();
        String jsonPacket = mapper.writeValueAsString(packet);

        AtomicReference<InboundPacket> response = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        connection.outbound()
            .sendString(Mono.just(server.getPassword() + "\n"))
            .sendString(Mono.just(jsonPacket + "\n"))
            .then()
            .thenMany(connection.inbound()
                .receive()
                .asString()
                .take(1)
                .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECS)) // TODO: Make this configurable per server
                .doOnNext(str -> handleServerResponse(str, server, response, error)))
            .then(Mono.defer(() -> disconnect(connection)))
            .block(Duration.ofSeconds(DEFAULT_TIMEOUT_SECS)); // TODO: Make this configurable per server

        if (error.get() != null) {
            throw error.get();
        }

        return response.get();
    }

    private static void handleServerResponse(String str, DcsServer server, AtomicReference<InboundPacket> responseCapture, AtomicReference<Throwable> errorCapture) {
        log.info("Received response from DCS server {}: {}", server.getName(), str);
        try {
            responseCapture.set(mapper.readValue(str, InboundPacket.class));
        } catch (JsonProcessingException e) {
            errorCapture.set(e);
        }
    }

    private static void handleError(Throwable t, String message, AtomicReference<Throwable> capture) {
        log.error(message, t);
        capture.set(t);
    }

    private static Mono<Void> disconnect(Connection connection) {
        connection.dispose();
        return Mono.empty();
    }
}
