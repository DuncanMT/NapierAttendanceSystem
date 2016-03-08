package com.duncan.napierattendancesystem;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Duncan on 01/03/2016.
 */
public class EventData implements Serializable {

    private String trimester;
    private String day;
    private String time;
    private String module;
    private String event;
    private ArrayList<Integer> weeks = new ArrayList<>();

    public EventData(String trimester, String day, String time, String module, String event, int week){
        this.trimester=trimester;
        this.day = day;
        this.time = time;
        this.module =module;
        this.event = event;
        weeks.add(week);
    }

    public EventData(String message){
        event = message;
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


    public ArrayList<Integer> getWeeks() {
        return weeks;
    }

    public void addWeek(int week) {
        this.weeks.add(week);
    }
}
