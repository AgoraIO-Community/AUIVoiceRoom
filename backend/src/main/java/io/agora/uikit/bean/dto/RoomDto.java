package io.agora.uikit.bean.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
public class RoomDto {
    // Room id
    private String roomId;
    // Room name
    private String roomName;
}
