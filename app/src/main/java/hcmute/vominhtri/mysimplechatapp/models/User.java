package hcmute.vominhtri.mysimplechatapp.models;

import java.io.Serializable;

public class User implements Serializable {
    public String name, image, email, token, id;

    public User() {
    }

    public User(String name, String image, String email, String token, String id) {
        this.name = name;
        this.image = image;
        this.email = email;
        this.token = token;
        this.id = id;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", token='" + token + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
