package hcmute.vominhtri.mysimplechatapp.models;

import java.util.Date;

public class ChatMessage {
    public String senderId, receiverId, message, datetime;
    public Date datetimeObject;
    public String conversionId, conversionName, conversionImage;

    public ChatMessage() {
    }

    public ChatMessage(String senderId, String receiverId, String message, String datetime) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", message='" + message + '\'' +
                ", datetime='" + datetime + '\'' +
                ", conversionId='" + conversionId + '\'' +
                ", conversionName='" + conversionName + '\'' +
                '}';
    }
}

