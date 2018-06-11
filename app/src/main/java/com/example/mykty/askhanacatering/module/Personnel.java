package com.example.mykty.askhanacatering.module;

public class Personnel{
    String info;
    String id_number;
    String photo;
    String type;

    public Personnel(){}

    public Personnel(String info, String id_number, String photo, String type){
        this.info = info;
        this.id_number = id_number;
        this.photo = photo;
        this.type = type;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getId_number() {
        return id_number;
    }

    public void setId_number(String id_number) {
        this.id_number = id_number;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
