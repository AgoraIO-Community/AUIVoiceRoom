package io.agora.uikit.bean.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class MicSeatPayload {
    @NotNull(message = "seatNo cannot be null")
    private Integer seatNo;
    private String desc;
}
