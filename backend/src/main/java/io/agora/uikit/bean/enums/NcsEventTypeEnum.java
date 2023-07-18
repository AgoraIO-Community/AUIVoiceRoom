package io.agora.uikit.bean.enums;

import java.util.Objects;

import lombok.Getter;

@Getter
public enum NcsEventTypeEnum {
    CHANNEL_CREATE(101),
    CHANNEL_DESTROY(102),
    BROADCASTER_JOIN_CHANNEL(103),
    BROADCASTER_LEAVE_CHANNEL(104),
    AUDIENCE_JOIN_CHANNEL(105),
    AUDIENCE_LEAVE_CHANNEL(106),;

    private Integer code;

    NcsEventTypeEnum(Integer code) {
        this.code = code;
    }

    public static NcsEventTypeEnum getEnumByCode(Integer code) {
        for (NcsEventTypeEnum ncsEventTypeEnum : NcsEventTypeEnum.values()) {
            if (Objects.equals(ncsEventTypeEnum.code, code)) {
                return ncsEventTypeEnum;
            }
        }
        return null;
    }
}
