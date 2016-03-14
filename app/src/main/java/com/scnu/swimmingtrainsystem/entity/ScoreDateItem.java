package com.scnu.swimmingtrainsystem.entity;

/**
 * 记录返回的时间列表
 * Created by lixinkun on 16/3/2.
 */
public class ScoreDateItem {
    private String times;
    private int distance;
    private int plan_id;
    private String pdate;
    private boolean isChecked;

    public ScoreDateItem() {
        this.isChecked = false;
    }

    public String getTimes() {
        return times;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getPlan_id() {
        return plan_id;
    }

    public void setPlan_id(int plan_id) {
        this.plan_id = plan_id;
    }

    public String getPdate() {
        return pdate;
    }

    public void setPdate(String pdate) {
        this.pdate = pdate;
    }

    @Override
    public String toString() {
        return "ScoreDateItem{" +
                "times='" + times + '\'' +
                ", distance=" + distance +
                ", plan_id=" + plan_id +
                ", pdate='" + pdate + '\'' +
                ", isChecked=" + isChecked +
                '}';
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
