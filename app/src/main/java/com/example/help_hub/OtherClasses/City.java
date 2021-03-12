package com.example.help_hub.OtherClasses;

public class City implements Comparable<City> {

    private String title;


    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int compareTo(City o) {
        return this.getTitle().compareTo(o.getTitle());
    }
}
