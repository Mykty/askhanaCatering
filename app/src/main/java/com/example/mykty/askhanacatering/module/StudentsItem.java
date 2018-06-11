package com.example.mykty.askhanacatering.module;

import java.io.Serializable;

public class StudentsItem implements Serializable {
    private String childName;

    public StudentsItem(String childName){
        this.childName = childName;
    }
    public String getChildName() {
        return childName;
    }
    public void setChildName(String childName) {
        this.childName = childName;
    }
}