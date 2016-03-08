package com.scnu.swimmingtrainsystem.model;

import org.litepal.crud.DataSupport;

/**
 * 保存其他成绩的类
 *
 * Created by lixinkun on 16/2/16.
 */
public class OtherScore extends DataSupport{

    private String pdate;

    private int uid;

    private int athlete_id;

    private String score;

    private String athlete_name;

    private int type;

    private int distance;

    public String getPdate() {
        return pdate;
    }

    public void setPdate(String pdate) {
        this.pdate = pdate;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getAthlete_id() {
        return athlete_id;
    }

    public void setAthlete_id(int athlete_id) {
        this.athlete_id = athlete_id;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getAthlete_name() {
        return athlete_name;
    }

    public void setAthlete_name(String athlete_name) {
        this.athlete_name = athlete_name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "OtherScore{" +
                "pdate='" + pdate + '\'' +
                ", uid=" + uid +
                ", athlete_id=" + athlete_id +
                ", score='" + score + '\'' +
                ", athlete_name='" + athlete_name + '\'' +
                ", type=" + type +
                ", distance=" + distance +
                '}';
    }
}
