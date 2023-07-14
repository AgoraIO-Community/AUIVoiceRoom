package io.agora.uikit.bean.req;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@Accessors(chain = true)
public class ChatRoomCreateUserReq {
    @NotBlank(message = "userName cannot be empty")
    private String userName;
}
