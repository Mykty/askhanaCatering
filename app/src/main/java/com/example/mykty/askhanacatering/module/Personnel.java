package com.example.mykty.askhanacatering.module;

public class Personnel{
    String key;
    String info;
    String id_number;
    String card_number;
    String photo;
    String type;

    public Personnel(){}

    public Personnel(String key, String info, String id_number, String card_number, String photo, String type){
        this.key = key;
        this.info = info;
        this.card_number = card_number;
        this.id_number = id_number;
        this.photo = photo;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
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
