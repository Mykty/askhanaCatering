package com.example.mykty.askhanacatering.module;

public class Report {
    String type;
    String name;
    String date;
    String fKezen;
    String time;

    public Report(){}

    public Report(String type, String name, String fKezen, String date, String time){
        this.type = type;
        this.name = name;
        this.date = date;
        this.fKezen = fKezen;
        this.time = time;
    }

    public String getfKezen() {
        return fKezen;
    }

    public void setfKezen(String fKezen) {
        this.fKezen = fKezen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
