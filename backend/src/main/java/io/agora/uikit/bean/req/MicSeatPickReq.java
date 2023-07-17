package io.agora.uikit.bean.req;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

import lombok.Data;

@Data
public class MicSeatPickReq {
    // Room id
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;

    // User id
    @NotBlank(message = "userId cannot be empty")
    private String userId;

    // Owner
    @NotNull(message = "owner cannot be empty")
    @Valid
    private MicSeatOwnerReq owner;

    // Mic seat no
    @NotNull(message = "micSeatNo cannot be empty")
    @PositiveOrZero(message = "micSeatNo must be greater than or equal to 0")
    private Integer micSeatNo;
}
