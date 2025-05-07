package uk.co.obora.dcs;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class OutboundPacket {

    private String type;
    private String script;
}
