package edu.uncc.emessageme.models;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {

    String uid, name;

    ArrayList<String> blocked;

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

    public ArrayList<String> getBlocked() {
        return blocked;
    }

    public void setBlocked(ArrayList<String> blocked) {
        this.blocked = blocked;
    }
}
