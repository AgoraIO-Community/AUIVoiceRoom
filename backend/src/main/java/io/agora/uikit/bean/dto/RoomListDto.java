package io.agora.uikit.bean.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
public class RoomListDto<T> {
    // Page size
    private int pageSize;
    // Count
    private int count;
    // List
    private List<T> list;
}
