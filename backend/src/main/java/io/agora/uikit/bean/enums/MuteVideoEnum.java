package io.agora.uikit.bean.enums;

import java.util.Objects;

import lombok.Getter;

@Getter
public enum MuteVideoEnum {
    MUTE_VIDEO_NO(0),
    MUTE_VIDEO_YES(1),
    ;

    private Integer code;

    MuteVideoEnum(Integer code) {
        this.code = code;
    }

    public static MuteVideoEnum getEnumByCode(Integer code) {
        for (MuteVideoEnum muteVideoEnum : MuteVideoEnum.values()) {
            if (Objects.equals(muteVideoEnum.code, code)) {
                return muteVideoEnum;
            }
        }
        return null;
    }
}
