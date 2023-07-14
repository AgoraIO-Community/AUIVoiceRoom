package io.agora.uikit.bean.domain;

import io.agora.uikit.bean.enums.MicSeatStatusEnum;
import io.agora.uikit.bean.enums.MuteAudioEnum;
import io.agora.uikit.bean.enums.MuteVideoEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MicSeatDomain {
    // Mic seat no
    private Integer micSeatNo;
    // Mic seat status
    private MicSeatStatusEnum micSeatStatus;
    // Mic seat user info
    private MicSeatOwnerDomain owner;
    // Audio mute status
    private MuteAudioEnum isMuteAudio;
    // Video mute status
    private MuteVideoEnum isMuteVideo;
}
