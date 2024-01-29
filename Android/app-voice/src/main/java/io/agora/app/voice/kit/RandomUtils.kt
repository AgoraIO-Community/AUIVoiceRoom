package io.agora.app.voice.kit

import java.util.Random

object RandomUtils {
    fun randomAvatar(): String {
        val randomValue = Random().nextInt(8) + 1
        return "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_${randomValue}.png"
    }

    fun randomUserId() = Random().nextInt(99999999).toString()

    fun randomRoomName(): String {
        val randomValue = (Random().nextInt(9) + 1) * 10000 + Random().nextInt(10000)
        return "room_${randomValue}"
    }

    fun randomUserName(): String {
        val userNames = arrayListOf(
            "安迪",
            "路易",
            "汤姆",
            "杰瑞",
            "杰森",
            "布朗",
            "吉姆",
            "露西",
            "莉莉",
            "韩梅梅",
            "李雷",
            "张三",
            "李四",
            "小红",
            "小明",
            "小刚",
            "小霞",
            "小智",
        )
        val randomValue = Random().nextInt(userNames.size) + 1
        return userNames[randomValue % userNames.size]
    }

}