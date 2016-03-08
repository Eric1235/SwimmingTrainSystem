package com.scnu.swimmingtrainsystem.model;

/**
 * 查询回来的成绩，用于展示
 * Created by lixinkun on 16/3/4.
 */
public class ExplicitScore {
    private String score;
    private int times;
    private int distance;
    private int athlete_id;
    private String up_time;
    private String athlete_name;
    private int stroke;
    private int plan_id;

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getAthlete_id() {
        return athlete_id;
    }

    public void setAthlete_id(int athlete_id) {
        this.athlete_id = athlete_id;
    }

    public String getUp_time() {
        return up_time;
    }

    public void setUp_time(String up_time) {
        this.up_time = up_time;
    }

    public String getAthlete_name() {
        return athlete_name;
    }

    public void setAthlete_name(String athlete_name) {
        this.athlete_name = athlete_name;
    }

    public int getStroke() {
        return stroke;
    }

    public void setStroke(int stroke) {
        this.stroke = stroke;
    }

    public int getPlan_id() {
        return plan_id;
    }

    public void setPlan_id(int plan_id) {
        this.plan_id = plan_id;
    }

    @Override
    public String toString() {
        return "ExplicitScore{" +
                "score='" + score + '\'' +
                ", times=" + times +
                ", distance=" + distance +
                ", athlete_id=" + athlete_id +
                ", up_time='" + up_time + '\'' +
                ", athlete_name='" + athlete_name + '\'' +
                ", stroke=" + stroke +
                ", plan_id=" + plan_id +
                '}';
    }
}
