package com.scnu.swimmingtrainsystem.entity;/**
 * ${PROJET_NAME}
 * Created by lixinkun on 16/3/14 17:59.
 * Email EricLi1235@gmail.com.
 */

/**
 * User: lixinkun
 * Date: 2016-03-14
 * Time: 17:59
 * 用于展示的实体
 */
public class SpinnerEntity {
    private String displayString;
    private int usedNo;
    private boolean isReset;
    private String usedString;

    public String getDisplayString() {
        return displayString;
    }

    public void setDisplayString(String displayString) {
        this.displayString = displayString;
    }

    public int getUsedNo() {
        return usedNo;
    }

    public void setUsedNo(int usedNo) {
        this.usedNo = usedNo;
    }

    public boolean isReset() {
        return isReset;
    }

    public void setIsReset(boolean isReset) {
        this.isReset = isReset;
    }

    public String getUsedString() {
        return usedString;
    }

    public void setUsedString(String usedString) {
        this.usedString = usedString;
    }

    @Override
    public String toString() {
        return "SpinnerEntity{" +
                "displayString='" + displayString + '\'' +
                ", usedNo=" + usedNo +
                ", isReset=" + isReset +
                ", usedString='" + usedString + '\'' +
                '}';
    }
}
