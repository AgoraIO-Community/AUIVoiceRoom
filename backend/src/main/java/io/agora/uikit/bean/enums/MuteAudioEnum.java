package io.agora.uikit.bean.enums;

import java.util.Objects;

import lombok.Getter;

@Getter
public enum MuteAudioEnum {
    MUTE_AUDIO_NO(0),
    MUTE_AUDIO_YES(1),
    ;

    private Integer code;

    MuteAudioEnum(Integer code) {
        this.code = code;
    }

    public static MuteAudioEnum getEnumByCode(Integer code) {
        for (MuteAudioEnum muteVideoEnum : MuteAudioEnum.values()) {
            if (Objects.equals(muteVideoEnum.code, code)) {
                return muteVideoEnum;
            }
        }
        return null;
    }
}
