package com.example.mykty.askhanacatering.module;

import java.util.ArrayList;

public class Order {
    String keys;
    String title;
    String orderPersonName;
    String status;
    String date;
    String time;
    String personCount;
    String phoneNumber;
    String menu;

    public Order(){}

    public Order(String keys, String title, String orderPersonName, String status, String date, String time,String personCount,String phoneNumber, String menu){
        this.keys = keys;
        this.title = title;
        this.orderPersonName = orderPersonName;
        this.status = status;
        this.date = date;
        this.time = time;
        this.personCount = personCount;
        this.title = title;
        this.phoneNumber = phoneNumber;
        this.menu = menu;
    }


    public Order(String title, String orderPersonName, String status, String date, String time,String personCount,String phoneNumber/*,ArrayList<String>  menu*/){
        this.title = title;
        this.orderPersonName = orderPersonName;
        this.status = status;
        this.date = date;
        this.time = time;
        this.personCount = personCount;
        this.title = title;
        this.phoneNumber = phoneNumber;
       // this.menu = menu;
    }

    public String getKeys() {
        return keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrderPersonName() {
        return orderPersonName;
    }

    public void setOrderPersonName(String orderPersonName) {
        this.orderPersonName = orderPersonName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPersonCount() {
        return personCount;
    }

    public void setPersonCount(String personCount) {
        this.personCount = personCount;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
