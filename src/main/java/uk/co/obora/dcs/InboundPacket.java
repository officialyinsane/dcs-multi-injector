package uk.co.obora.dcs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class InboundPacket {

    @JsonProperty("type")
    private String type;

    @JsonProperty("status")
    private String status;
}
