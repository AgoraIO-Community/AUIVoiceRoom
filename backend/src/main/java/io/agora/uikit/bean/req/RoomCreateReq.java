package io.agora.uikit.bean.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Data
@Accessors(chain = true)
public class RoomCreateReq {
    // Room id
    private String roomId;

    // Room name
    @NotBlank(message = "roomName cannot be empty")
    private String roomName;

    // User id
    @NotBlank(message = "userId cannot be empty")
    private String userId;

    // User name
    @NotBlank(message = "userName cannot be empty")
    private String userName;

    // User avatar
    @NotBlank(message = "userAvatar cannot be empty")
    private String userAvatar;

    // Mic seat count
    @NotNull(message = "micSeatCount cannot be empty")
    @PositiveOrZero(message = "micSeatCount must be greater than or equal to 0")
    private int micSeatCount;

    // Mic seat style
    private String micSeatStyle;
}
