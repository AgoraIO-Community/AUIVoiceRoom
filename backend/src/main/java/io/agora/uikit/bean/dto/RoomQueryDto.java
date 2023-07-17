package io.agora.uikit.bean.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.agora.uikit.bean.domain.RoomInfoDomain;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
public class RoomQueryDto {
    // Room id
    private RoomInfoDomain roomInfo;
}
