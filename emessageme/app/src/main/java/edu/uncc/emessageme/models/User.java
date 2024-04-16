package edu.uncc.emessageme.models;

import java.io.Serializable;

public class User implements Serializable {

    String uid, name;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public User(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public User() {

    }
}
