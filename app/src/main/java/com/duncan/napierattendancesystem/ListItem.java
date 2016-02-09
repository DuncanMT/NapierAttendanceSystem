package com.duncan.napierattendancesystem;

public class ListItem {

    private String text;
    private String colour;

    public ListItem(String text, String colour){
        this.text = text;
        this.colour = colour;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public String getColour() {
        return colour;
    }
    public void setColour(String colour) {
        this.colour = colour;
    }

}
