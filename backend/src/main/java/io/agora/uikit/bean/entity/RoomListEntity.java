package io.agora.uikit.bean.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import io.agora.uikit.bean.domain.RoomInfoOwnerDomain;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Document(collection = "room_list")
public class RoomListEntity {
    // Room id
    @MongoId
    private String roomId;
    // Room name
    private String roomName;
    // Room owner
    private RoomInfoOwnerDomain roomOwner;
    // Online users
    private Long onlineUsers;
    // Room seat count
    private Integer micSeatCount;
    // Mic seat style
    private String micSeatStyle;
    // Create time
    private Long createTime;
}
