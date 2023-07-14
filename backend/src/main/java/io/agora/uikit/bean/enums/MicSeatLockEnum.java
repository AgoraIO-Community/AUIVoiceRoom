package io.agora.uikit.bean.enums;

import lombok.Getter;

@Getter
public enum MicSeatLockEnum {
    MIC_SEAT_LOCK_NO(0),
    MIC_SEAT_LOCK_YES(1),
    ;

    private Integer code;

    MicSeatLockEnum(Integer code) {
        this.code = code;
    }
}
