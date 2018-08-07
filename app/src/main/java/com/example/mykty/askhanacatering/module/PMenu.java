package com.example.mykty.askhanacatering.module;

public class PMenu {
    String title;
    int icon;
    String count;
    String desc;

    public PMenu(String title, String desc){
        this.title = title;
        this.desc = desc;
    }

    public PMenu(String title, int icon, String count){
        this.title = title;
        this.icon = icon;
        this.count = count;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
