package com.example.mykty.askhanacatering.module;

import java.io.Serializable;

public class Student  implements Serializable {
    String name;
    String id_number;
    String card_number;
    String photo;
    String qr_code;


    public Student(){}

    public Student(String name, String id_number, String card_number, String photo, String qr_code){
        this.name = name;
        this.id_number = id_number;
        this.card_number = card_number;
        this.photo = photo;
        this.qr_code = qr_code;
    }

    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getQr_code() {
        return qr_code;
    }

    public void setQr_code(String qr_code) {
        this.qr_code = qr_code;
    }
}
