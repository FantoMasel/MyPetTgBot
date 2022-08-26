package io.proj3ct.SpringGostBot.model;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity(name = "userComplaints")
public class UserForComp {

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Id
    private Long id;
    private Long chatId;

    private String userName;



    private Timestamp registeredAt;

    private String massage;


    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }

    public void setMassage(String massage) {
        this.massage = massage;
    }
    public String getMassage() {
        return massage;
    }

    @Override
    public String toString() {
        return "UserForComp{" +
                "chatId=" + chatId +
                ", userName='" + userName + '\'' +
                ", registeredAt=" + registeredAt +
                ", massage='" + massage + '\'' +
                '}';
    }
}

