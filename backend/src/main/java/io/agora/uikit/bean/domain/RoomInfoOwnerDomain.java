package io.agora.uikit.bean.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RoomInfoOwnerDomain {
    // User id
    private String userId;
    // User name
    private String userName;
    // User avatar
    private String userAvatar;
}
