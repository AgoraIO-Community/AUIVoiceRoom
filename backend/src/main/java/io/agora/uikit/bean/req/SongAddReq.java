package io.agora.uikit.bean.req;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SongAddReq {
    // Room id
    @NotBlank(message = "roomId cannot be empty")
    private String roomId;

    // User id
    @NotBlank(message = "userId cannot be empty")
    private String userId;

    // Song code
    @NotBlank(message = "songCode cannot be empty")
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
    @NotNull(message = "owner cannot be empty")
    @Valid
    private SongOwnerReq owner;
}
