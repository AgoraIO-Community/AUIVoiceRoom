package io.agora.uikit.bean.enums;

import lombok.Getter;

@Getter
public enum MicSeatStatusEnum {
    MIC_SEAT_STATUS_IDLE(0),
    MIC_SEAT_STATUS_USED(1),
    MIC_SEAT_STATUS_LOCKED(2),
    ;

    private Integer code;

    MicSeatStatusEnum(Integer code) {
        this.code = code;
    }
}
