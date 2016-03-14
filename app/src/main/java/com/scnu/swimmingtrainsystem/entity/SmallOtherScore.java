package com.scnu.swimmingtrainsystem.entity;

/**
 * 为了获取
 * Created by lixinkun on 16/2/27.
 */
public class SmallOtherScore {

        String pdate;
        String score;
        String athlete_name;
        String athlete_id;


        public String getPdate() {
            return pdate;
        }

        public void setPdate(String pdate) {
            this.pdate = pdate;
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

        public String getAthlete_id() {
            return athlete_id;
        }

        public void setAthlete_id(String athlete_id) {
            this.athlete_id = athlete_id;
        }

        @Override
        public String toString() {
            return "TempScore{" +
                    "pdate='" + pdate + '\'' +
                    ", score='" + score + '\'' +
                    ", athlete_name='" + athlete_name + '\'' +
                    ", athlete_id='" + athlete_id + '\'' +
                    '}';
        }

}
