package com.scnu.swimmingtrainsystem.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 查询运动员成绩条件类
 * Created by lixinkun on 15/12/30.
 */
public class QueryScoreEntity implements Serializable {
    private Date startTime;
    private Date endTime;
    private String poolLength;
    private int stroke;
    private boolean isReset;
    private int athleteId;
    private int uid;
    private String distance;

    public QueryScoreEntity() {
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public String getPoolLength() {
        return poolLength;
    }


    public int getAthleteId() {
        return athleteId;
    }

    public int getUid() {
        return uid;
    }

    public String getDistance() {
        return distance;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setPoolLength(String poolLength) {
        this.poolLength = poolLength;
    }

    public int getStroke() {
        return stroke;
    }

    public void setStroke(int stroke) {
        this.stroke = stroke;
    }

    public void setAthleteId(int athleteId) {
        this.athleteId = athleteId;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public boolean isReset() {
        return isReset;
    }

    public void setIsReset(boolean isReset) {
        this.isReset = isReset;
    }


    @Override
    public String toString() {
        return "QueryScoreEntity{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", poolLength='" + poolLength + '\'' +
                ", stroke='" + stroke + '\'' +
                ", isReset=" + isReset +
                ", athleteId=" + athleteId +
                ", uid=" + uid +
                ", distance='" + distance + '\'' +
                '}';
    }
}
