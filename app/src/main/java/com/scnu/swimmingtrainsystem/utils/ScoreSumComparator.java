package com.scnu.swimmingtrainsystem.utils;/**
 * ${PROJET_NAME}
 * Created by lixinkun on 16/3/13 20:25.
 * Email EricLi1235@gmail.com.
 */

import com.scnu.swimmingtrainsystem.entity.ScoreSum;

import java.util.Comparator;

/**
 * User: lixinkun
 * Date: 2016-03-13
 * Time: 20:25
 * 成绩比较适配器
 */
public class ScoreSumComparator implements Comparator<ScoreSum>{

        @Override
        public int compare(ScoreSum lhs, ScoreSum rhs) {
            // TODO Auto-generated method stub
            ScoreSum temp1 = lhs;
            ScoreSum temp2 = rhs;
            int num = temp1.getScore().compareTo(temp2.getScore());
            if (num == 0)
                return temp1.getAthleteName().compareTo(temp2.getAthleteName());
            return num;
        }

}
