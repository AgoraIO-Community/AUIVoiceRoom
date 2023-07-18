package io.agora.uikit.bean.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
public class ChatRoomCreateRoomReq {
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;

    @NotBlank(message = "userId cannot be empty")
    private String userId;

    @NotBlank(message = "userName cannot be empty")
    private String userName;

    private String description;

    private String custom;
}
