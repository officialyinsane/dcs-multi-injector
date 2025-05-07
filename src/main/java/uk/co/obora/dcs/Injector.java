package uk.co.obora.dcs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.TcpClient;
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

        OutboundPacket packet = OutboundPacket.builder()
                .type("lua")
                .script(code)
                .build();
        String jsonPacket = mapper.writeValueAsString(packet);

        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicReference<InboundPacket> response = new AtomicReference<>();

        client.connect()
            .doOnError(t -> {
                log.error("Failed to connect to DCS server {}", server.getName(), t);
                error.set(t);
            })
            .flatMap(connection -> connection.outbound()
                .sendString(Mono.just(server.getPassword() + "\n"))
                .sendString(Mono.just(jsonPacket + "\n"))
                .then()
                .thenMany(connection.inbound()
                    .receive()
                    .asString()
                    .take(1)
                    .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECS)) // TODO: Make this configurable per server
                    .doOnNext(str -> {
                        log.info("Received response from DCS server {}: {}", server.getName(), str);
                        try {
                            response.set(mapper.readValue(str, InboundPacket.class));
                        } catch (JsonProcessingException e) {
                            error.set(e);
                        }
                    }))
                .then(Mono.defer(() -> {
                    connection.dispose();
                    return Mono.empty();
                }))
            ).block();

        if (error.get() != null) {
            throw new RuntimeException("Encountered an error talking with server: " + server.getName(), error.get());
        }

        return response.get();
    }

}
