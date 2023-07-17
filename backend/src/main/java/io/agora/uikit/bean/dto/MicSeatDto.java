package io.agora.uikit.bean.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Accessors(chain = true)
public class MicSeatDto {
    // Room id
    private String roomId;
    // Mic seat no
    private Integer micSeatNo;
}
