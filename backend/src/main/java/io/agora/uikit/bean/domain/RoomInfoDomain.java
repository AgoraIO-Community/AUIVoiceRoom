package io.agora.uikit.bean.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RoomInfoDomain {
    // Room id
    private String roomId;
    // Room name
    private String roomName;
    // Room thumbnail
    private String roomThumbnail;
    // Room background image
    private String roomBackgroundImage;
    // Room Notice
    private String roomNotice;
    // Room owner
    private RoomInfoOwnerDomain roomOwner;
    // Room seat count
    private Integer micSeatCount;
    // Mic seat style
    private String micSeatStyle;
    // Create time
    private Long createTime;
}
