package io.agora.uikit.bean.req;

import javax.validation.constraints.NotBlank;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SongOwnerReq {
    // User id
    @NotBlank(message = "userId cannot be empty")
    private String userId;

    // User name
    @NotBlank(message = "userName cannot be empty")
    private String userName;

    // User avatar
    @NotBlank(message = "userAvatar cannot be empty")
    private String userAvatar;
}
