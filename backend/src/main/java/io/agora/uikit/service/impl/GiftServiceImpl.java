package io.agora.uikit.service.impl;

import io.agora.uikit.service.IGiftService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GiftServiceImpl implements IGiftService {

    @Override
    public List<Map<String, Object>> list() {
        Map<String, Object> root = new HashMap<>();
        root.put("tabId", 1);
        root.put("displayName", "Gifts");

        List<Map<String, Object>> gifts = new ArrayList<>();

        Map<String, Object> gift1 = new HashMap<>();
        gift1.put("giftId", "2665752a-e273-427c-ac5a-4b2a9c82b255");
        gift1.put("giftName", "Sweet Heart");
        gift1.put("giftPrice", 1);
        gift1.put("giftIcon", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift1.png");
        gifts.add(gift1);

        Map<String, Object> gift2 = new HashMap<>();
        gift2.put("giftId", "ff3bbb9e-ef18-430f-aa61-5bddf75eb722");
        gift2.put("giftName", "Flower");
        gift2.put("giftPrice", 2);
        gift2.put("giftIcon", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift2.png");
        gifts.add(gift2);

        Map<String, Object> gift3 = new HashMap<>();
        gift3.put("giftId", "94f296fa-86d9-4552-84db-025b05ed9f8d");
        gift3.put("giftName", "Sweet Heart");
        gift3.put("giftPrice", 5);
        gift3.put("giftIcon", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift3.png");
        gifts.add(gift3);

        Map<String, Object> gift4 = new HashMap<>();
        gift4.put("giftId", "d4cd0526-d8db-4e00-8fc0-d5228907a517");
        gift4.put("giftName", "Super Agora");
        gift4.put("giftPrice", 10);
        gift4.put("giftIcon", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift4.png");
        gifts.add(gift4);

        Map<String, Object> gift5 = new HashMap<>();
        gift5.put("giftId", "c1997f02-d927-46f5-adda-e6af6714bd75");
        gift5.put("giftName", "Star");
        gift5.put("giftPrice", 20);
        gift5.put("giftIcon", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift5.png");
        gifts.add(gift5);

        Map<String, Object> gift6 = new HashMap<>();
        gift6.put("giftId", "0c62b402-376f-4fbb-b584-769a8249189e");
        gift6.put("giftName", "Lollipop");
        gift6.put("giftPrice", 30);
        gift6.put("giftIcon", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift6.png");
        gifts.add(gift6);

        Map<String, Object> gift7 = new HashMap<>();
        gift7.put("giftId", "ce3f8bc3-74d7-43be-a040-c397d5c49f6d");
        gift7.put("giftName", "Diamond");
        gift7.put("giftPrice", 50);
        gift7.put("giftIcon", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift7.png");
        gifts.add(gift7);

        Map<String, Object> gift8 = new HashMap<>();
        gift8.put("giftId", "948b1a3b-b2c6-41fc-99b7-a5b9457cd159");
        gift8.put("giftName", "Crown");
        gift8.put("giftPrice", 100);
        gift8.put("giftIcon", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift8.png");
        gifts.add(gift8);

        Map<String, Object> gift9 = new HashMap<>();
        gift9.put("giftId", "f1e12397-feb7-4c01-b834-f11faf321dbf");
        gift9.put("giftName", "Mic");
        gift9.put("giftPrice", 500);
        gift9.put("giftIcon", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift9.png");
        gifts.add(gift9);

        Map<String, Object> gift10 = new HashMap<>();
        gift10.put("giftId", "e915438c-7fbd-4e03-840f-0036ec97c824");
        gift10.put("giftName", "Balloon");
        gift10.put("giftEffect", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pag/ballon.pag");
        gift10.put("effectMD5", "141761700268c0290852af8f6a501c10");
        gift10.put("giftPrice", 666);
        gift10.put("giftIcon", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift10.png");
        gifts.add(gift10);

        Map<String, Object> gift11 = new HashMap<>();
        gift11.put("giftId", "0c832b52-8f2e-4202-958b-9410db2d9438");
        gift11.put("giftName", "Plant");
        gift11.put("giftEffect", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pag/planet.pag");
        gift11.put("effectMD5", "41f3eeff249be268004d82a1d1eaf481");
        gift11.put("giftPrice", 888);
        gift11.put("giftIcon", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift11.png");
        gifts.add(gift11);

        Map<String, Object> gift12 = new HashMap<>();
        gift12.put("giftId", "beada6a3-eae6-450e-869c-743d02fa95e7");
        gift12.put("giftName", "Rocket");
        gift12.put("giftEffect", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pag/rocket.pag");
        gift12.put("effectMD5", "de5094b30eebeadf8b8f5d8357a19578");
        gift12.put("giftPrice", 1000);
        gift12.put("giftIcon", "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift12.png");
        gifts.add(gift12);

        root.put("gifts", gifts);
        return new ArrayList<>() {
            {
                add(root);
            }
        };
    }
}
