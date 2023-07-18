package io.agora.uikit.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

// https://docs.agora.io/cn/video-call-4.x/signature_verify
@Component
public class HmacShaUtil {
    // 将加密后的字节数组转换成字符串
    public String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    // HMAC/SHA256 加密，返回加密后的字符串
    public String hmacSha256(String message, String secret) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(
                    "utf-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(message.getBytes("utf-8"));
            return bytesToHex(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void main(String[] args) {
        // 拿到消息通知的 raw request body 并对其计算签名，也就是说下面代码中的 request_body 是反序列化之前的 binary byte
        // array，不是反序列化之后的 object
        String requestBody = "{\"eventType\":10,\"noticeId\":\"4eb720f0-8da7-11e9-a43e-53f411c2761f\",\"notifyMs\":1560408533119,\"payload\":{\"a\":\"1\",\"b\":2},\"productId\":1}";
        String secret = "secret";
        System.out.println(hmacSha256(requestBody, secret)); // de96da5acf03b0021ac3b4fa2225e7ae6f3533a30d50bb02c08ea4fa748bda24
    }
}
