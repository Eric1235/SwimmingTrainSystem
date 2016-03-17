package com.scnu.swimmingtrainsystem.utils;/**
 * ${PROJET_NAME}
 * Created by lixinkun on 16/3/15 22:03.
 * Email EricLi1235@gmail.com.
 */

import com.scnu.swimmingtrainsystem.model2db.Score;

import java.util.Comparator;

/**
 * User: lixinkun
 * Date: 2016-03-15
 * Time: 22:03
 * 比较成绩
 */
public class ScoreComparator implements Comparator<Score> {
    @Override
    public int compare(Score score, Score t1) {
        int num = score.getScore().compareTo(t1.getScore());
        return num;
    }
}
