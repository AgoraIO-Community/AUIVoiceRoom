package io.agora.uikit.bean.domain;

import io.agora.uikit.bean.enums.SongStatusEnum;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SongDomain {
    // Song code
    private String songCode;
    // Song name
    private String name;
    // Singer
    private String singer;
    // Poster
    private String poster;
    // Release time
    private String releaseTime;
    // Duration
    private Integer duration;
    // Music url
    private String musicUrl;
    // Lyric url
    private String lrcUrl;
    // Owner
    private SongOwnerDomain owner;
    // Song status
    private SongStatusEnum status;
    // Pin time
    private Long pinAt;
    // Create time
    private Long createAt;
}
