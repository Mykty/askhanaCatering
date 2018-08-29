package com.example.mykty.askhanacatering.module;

public class EaterItem {
    String id_number;
    String time;

    public EaterItem(){}

    public EaterItem(String id_number, String time){
        this.time = time;
        this.id_number = id_number;
    }

    public String getId_number() {
        return id_number;
    }

    public void setId_number(String id_number) {
        this.id_number = id_number;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
