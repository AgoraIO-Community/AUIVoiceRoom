package io.agora.asceneskit.voice

import io.agora.auikit.service.ktv.KTVApi
import io.agora.rtc2.RtcEngineEx
import io.agora.rtm.RtmClient

data class AUIAPIConfig(
    val ktvApi: KTVApi? = null,
    val rtcEngineEx: RtcEngineEx? = null,
    val rtmClient: RtmClient? = null
)