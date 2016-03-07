package com.duncan.napierattendancesystem;

/**
 * Created by Duncan on 01/03/2016.
 */
public class EventData {

    private String trimester;
    private String day;
    private String time;
    private String module;
    private String event;
    private int week;

    public EventData(String trimester, String day, String time, String module, String event, int week){
        this.trimester=trimester;
        this.day = day;
        this.time = time;
        this.module =module;
        this.event = event;
        this.week = week;
    }
    public String getTrimester() {
        return trimester;
    }

    public void setTrimester(String trimester) {
        this.trimester = trimester;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }


    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }
}
