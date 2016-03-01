package com.duncan.napierattendancesystem;

public class ListItem {

    private String id;
    private String fname;
    private String sname;
    private String present;

    public ListItem(String id, String fname, String sname, String present){
        this.id = id;
        this.fname = fname;
        this.sname = sname;
        this.present = present;
    }

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}

    public String getFname() {return fname;}
    public void setFname(String fname) {this.fname = fname;}

    public String getSname() { return sname;}
    public void setSname(String sname) {this.sname = sname;}

    public String getPresent() { return present; }
    public void setPresent(String present) { this.present = present; }
}
