package com.scnu.swimmingtrainsystem.util;/**
 * ${PROJET_NAME}
 * Created by lixinkun on 16/3/15 10:48.
 * Email EricLi1235@gmail.com.
 */

import android.annotation.SuppressLint;

import com.scnu.swimmingtrainsystem.entity.ScoreSum;
import com.scnu.swimmingtrainsystem.model2db.Score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: lixinkun
 * Date: 2016-03-15
 * Time: 10:48
 * FIXME
 * 用于对数据进行统计和转换
 */
public class Statistics {

    /**
     * 将一个运动员的多次成绩综合统计
     *
     * @param list
     * @return
     */
    public static String scoreSum(List<String> list) {
        int hour = 0;
        int minute = 0;
        int second = 0;
        int millisecond = 0;
        for (String s : list) {
            int msc = Integer.parseInt(s.substring(9)) * 10;
            millisecond += msc;

            int sec = Integer.parseInt(s.substring(5, 7));
            second += sec;

            int min = Integer.parseInt(s.substring(2, 4));
            minute += min;

            int h = Integer.parseInt(s.substring(0, 1));
            hour += h;
        }
        second += millisecond / 1000;
        millisecond = millisecond % 1000 / 10;
        minute += second / 60;
        second = second % 60;
        hour += minute / 60;
        minute = minute % 60;
        return String.format("%1$01d:%2$02d'%3$02d''%4$02d", hour, minute,
                second, millisecond);
    }


    public static String getScoreSubtraction(String s1, String s2) {
        int Subtraction = timeString2TimeInt(s1) - timeString2TimeInt(s2);
        return timeInt2TimeString(Subtraction);

    }

    /**
     * 获得平均成绩
     * @param mSwimTime
     * @param tempScores
     * @return
     */
    public static List<ScoreSum> getAvgScore(int mSwimTime,List<ScoreSum> tempScores){
        List<ScoreSum> avgScores = new ArrayList<>();
        for (ScoreSum ss : tempScores) {
            ScoreSum scoreSum = new ScoreSum();
            int msec = timeString2TimeInt(ss.getScore());
            int avgsec = msec / (mSwimTime - 2);
            String avgScore = timeInt2TimeString(avgsec);
            scoreSum.setScore(avgScore);
            scoreSum.setAthleteName(ss.getAthleteName());
            avgScores.add(scoreSum);
        }
        return avgScores;
    }

    /**
     * 成绩进行整理，获得每个运动员的总成绩
     * @param mScores
     * @param
     * @param isReset
     * @return
     */
    public static List<ScoreSum> getAllScoreSum(List<Score> mScores,boolean isReset){
        List<ScoreSum> temps = new ArrayList<ScoreSum>();
        List<Integer> aidList = getAthleteIdInScores(mScores);
        //使用适配器遍历运动员
        for(Integer athlete_id : aidList) {
//		if(aidItor.hasNext()){
//			int athlete_id = aidItor.next();
            List<Score> scores = getScoresByAid(athlete_id, mScores);
            ScoreSum p = new ScoreSum();
            p.setAthleteName("我觉得不需要");
            p.setAthlete_id(athlete_id);
            /**
             * 本次成绩是间歇的
             */
            if (isReset) {
                List<String> sum = new ArrayList<String>();
                for (Score s : scores) {
                    sum.add(s.getScore());
                }
                /**
                 * 对成绩进行累加
                 */
                p.setScore(scoreSum(sum));
            }
            /**
             * 本次成绩不是间歇的，直接获取最后一个成绩就是总成绩，不需要累加
             */
            else {
                p.setScore(scores.get(scores.size() - 1).getScore());
            }

            temps.add(p);
        }
        /**
         * 对成绩进行排序，使用collection进行排序
         */
        Collections.sort(temps, new ScoreComparable());
        return  temps;
    }

    /**
     * 从列表中得到aid的scores
     * @param aid
     * @param mScores
     * @return
     */
    public static List<Score> getScoresByAid(int aid,List<Score> mScores){
        List<Score> scores = new ArrayList<>();
        //对成绩进行遍历
        for(Score s : mScores){
            if(s.getAthlete_id() == aid){
                scores.add(s);
            }
        }
        return  scores;
    }

    /**
     * 从成绩列表中获取运动员列表，是要全部遍历么？
     *
     * @param mScores
     * @return
     */
    public static List<Integer> getAthleteIdInScores(List<Score> mScores){
        List<Integer> aidList = new ArrayList<>();
//		Set<Integer> aidSet = new HashSet<>();
        for(Score s : mScores){
            aidList.add(s.getAthlete_id());
//			aidSet.add(s.getAthlete_id());
        }
        return aidList;
//		return aidSet;
    }

    /**
     * 把成绩按照times进行分类，得到每次time的列表
     * @param mScores
     * @param maxTime
     * @return
     */
    public static List<List<Score>> getScoresListByTimes(List<Score> mScores,int maxTime){
        List<List<Score>> listss = new ArrayList<>();
        //遍历整个数组,从1到n
        for(int i = 1 ; i <= maxTime ; i ++ ){
            List<Score> mSubScores = getScoresByTimes(mScores,i);
            listss.add(mSubScores);
        }
        return listss;
    }

    public static List<Score> getScoresByTimes(List<Score> mScores,int i){
        List<Score> mSubScores = new ArrayList<>();
        for(Score s : mScores){
            if(s.getTimes() == i){
                mSubScores.add(s);
            }
        }
        return mSubScores;
    }

    /**
     * 将时间字符串转化成毫秒数
     *
     * @param timeString
     * @return
     */
    public static int timeString2TimeInt(String timeString) {
        int msc = Integer.parseInt(timeString.substring(9)) * 10;
        int sec = Integer.parseInt(timeString.substring(5, 7));
        int min = Integer.parseInt(timeString.substring(2, 4));
        int hour = Integer.parseInt(timeString.substring(0, 1));
        int totalMsec = msc + sec * 1000 + min * 60000 + hour * 3600000;
        return totalMsec;

    }

    @SuppressLint("DefaultLocale")
    public static String timeInt2TimeString(int totalMsec) {
        // 秒数
        long time_count_s = totalMsec / 1000;
        // 小时数
        long hour = time_count_s / 3600;
        // 分
        long min = time_count_s / 60 - hour * 60;
        // 秒
        long sec = time_count_s - hour * 3600 - min * 60;
        // 毫秒
        long msec = totalMsec % 1000 / 10;

        return String.format("%1$01d:%2$02d'%3$02d''%4$02d", hour, min, sec,
                msec);
        // %1$01d:%2$02d'%3$ 03d''%4$ 03d
    }


}
