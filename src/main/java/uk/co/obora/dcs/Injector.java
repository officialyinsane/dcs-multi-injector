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

@Slf4j
public class Injector {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final int DEFAULT_TIMEOUT_SECS = 3;

    public static Mono<InboundPacket> doInjection(DcsServer server, String code) throws JsonProcessingException {
        TcpClient client = TcpClient.create()
            .host(server.getHostname())
            .port(server.getPort())
            .doOnConnected(connection -> log.info("Connected to DCS server {}", server.getName()));

        return client.connect()
            .doOnError(t -> handleError(t, "Failed to connect to DCS server " + server.getName()/*, error*/))
            .flatMap(connection -> {
                try {
                    return doServerCommunication(connection, server, code);
                } catch (Throwable t) {
                    handleError(t, "Failed to communicate with DCS server " + server.getName()/*, error*/);
                }
                return Mono.empty();
            });
    }

    private static Mono<InboundPacket> doServerCommunication(Connection connection, DcsServer server, String code) {
        try {
            OutboundPacket packet = OutboundPacket.builder()
                .type("lua")
                .script(code)
                .build();
            String jsonPacket = mapper.writeValueAsString(packet);

            return Mono.just(jsonPacket)
                .flatMap(json -> connection.outbound()
                    .sendString(Mono.just(server.getPassword() + "\n"))
                    .sendString(Mono.just(json + "\n"))
                    .then()
                    .thenReturn(connection))
                .flatMap(conn -> conn.inbound()
                    .receive()
                    .asString()
                    .take(1)
                    .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECS)) // TODO: Make this configurable per server
                    .single()
                    .flatMap(response -> handleServerResponse(response, server)))
                .doFinally(signalType -> connection.dispose());

        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    private static Mono<InboundPacket> handleServerResponse(String response, DcsServer server) {
        log.info("Received response from DCS server {}: {}", server.getName(), response);
        try {
            return Mono.just(mapper.readValue(response, InboundPacket.class));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    private static void handleError(Throwable t, String message) {
        log.error(message, t);
    }

}
