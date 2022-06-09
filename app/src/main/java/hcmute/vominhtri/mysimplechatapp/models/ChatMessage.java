package hcmute.vominhtri.mysimplechatapp.models;

import java.util.Date;

public class ChatMessage {
    private String senderId, receiverId, message, datetime;
    private Date datetimeObject;
    private String conversionId, conversionName, conversionImage;

    public ChatMessage() {
    }

    public ChatMessage(String senderId, String receiverId, String message, String datetime) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.datetime = datetime;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public Date getDatetimeObject() {
        return datetimeObject;
    }

    public void setDatetimeObject(Date datetimeObject) {
        this.datetimeObject = datetimeObject;
    }

    public String getConversionId() {
        return conversionId;
    }

    public void setConversionId(String conversionId) {
        this.conversionId = conversionId;
    }

    public String getConversionName() {
        return conversionName;
    }

    public void setConversionName(String conversionName) {
        this.conversionName = conversionName;
    }

    public String getConversionImage() {
        return conversionImage;
    }

    public void setConversionImage(String conversionImage) {
        this.conversionImage = conversionImage;
    }

}

