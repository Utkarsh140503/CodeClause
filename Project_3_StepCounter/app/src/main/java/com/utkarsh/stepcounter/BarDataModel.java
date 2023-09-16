package com.utkarsh.stepcounter;

public class BarDataModel {
    private int id;
    private String date;
    private String time;
    private int totalSteps;
    private float stepsPerSecond; // Add steps per second field

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public float getStepsPerSecond() {
        return stepsPerSecond;
    }

    public void setStepsPerSecond(float stepsPerSecond) {
        this.stepsPerSecond = stepsPerSecond;
    }
}
