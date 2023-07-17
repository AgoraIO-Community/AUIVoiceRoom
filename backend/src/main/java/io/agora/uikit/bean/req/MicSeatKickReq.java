package io.agora.uikit.bean.req;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import lombok.Data;

@Data
public class MicSeatKickReq {
    // Room id
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;

    // User id
    @NotBlank(message = "userId cannot be empty")
    private String userId;

    // Mic seat no
    @NotNull(message = "micSeatNo cannot be empty")
    @PositiveOrZero(message = "micSeatNo must be greater than or equal to 0")
    private Integer micSeatNo;
}
