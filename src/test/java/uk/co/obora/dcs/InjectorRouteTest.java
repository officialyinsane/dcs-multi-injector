package uk.co.obora.dcs;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.co.obora.dcs.dto.InboundPacket;
import uk.co.obora.dcs.entity.DcsServer;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("This test requires a running DCS instance")
public class InjectorRouteTest {

    @Test
    public void connectionFailThrowsException() {
        DcsServer server = DcsServer.getDefaultInstallationDetails();
        server.setPort(999);

        try {
            Injector.doInjection(server, "test");

            fail("should have thrown the exception");
        } catch (Throwable t) {
            assertNotNull(t);
        }
    }

    @Test
    public void connectionSuccess() {
        DcsServer server = DcsServer.getDefaultInstallationDetails();

        try {
            Injector.doInjection(server, "test");
        } catch (Throwable t) {
            fail("should not have thrown the exception");
        }
    }

    @Test
    public void connectionWithSendingTriggerOutText() {
        DcsServer server = DcsServer.getDefaultInstallationDetails();

        String code = "trigger.action.outText('hello world', 30, false);";

        try {
            AtomicReference<InboundPacket> response = new AtomicReference<>();
            AtomicReference<Throwable> ex = new AtomicReference<>();

            Injector.doInjection(server, code).subscribe(
                response::set,
                ex::set
            );

            assertNotNull(response.get());
            assertNull(ex.get());
            assertEquals("OK", response.get().getStatus());
            assertEquals("receipt", response.get().getType());

        } catch (Throwable t) {
            fail("should not have thrown the exception");
        }
    }
}
