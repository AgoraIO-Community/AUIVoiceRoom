package io.agora.uikit.bean.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
@Accessors(chain = true)
public class MicSeatEnterReq {
    // Room id
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;

    // User id
    @NotBlank(message = "userId cannot be empty")
    private String userId;

    // User name
    @NotBlank(message = "userName cannot be empty")
    private String userName;

    // User avatar
    @NotBlank(message = "userAvatar cannot be empty")
    private String userAvatar;

    // Mic seat no
    @NotNull(message = "micSeatNo cannot be empty")
    @PositiveOrZero(message = "micSeatNo must be greater than or equal to 0")
    private Integer micSeatNo;
}
