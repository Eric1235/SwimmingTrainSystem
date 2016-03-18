package com.scnu.swimmingtrainsystem.adapter;
/**
 * ${PROJET_NAME}
 * Created by lixinkun on 16/3/14 15:14.
 * Email EricLi1235@gmail.com.
 */

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.scnu.swimmingtrainsystem.R;
import com.scnu.swimmingtrainsystem.db.DBManager;
import com.scnu.swimmingtrainsystem.model2db.Score;
import com.scnu.swimmingtrainsystem.entity.ScoreSum;

import java.util.ArrayList;
import java.util.List;

/**
 * User: lixinkun
 * Date: 2016-03-14
 * Time: 15:14
 * 可以考虑不要用viewholder，数据少，直接显示就好了，这样就在最后不显示距离了
 */
public class NameScoreListAdapter extends BaseExpandableListAdapter {
    private DBManager dbManager;
    private Context mContext;
    private List<List<Score>> mLists = new ArrayList<List<Score>>();
    private List<ScoreSum> mTemps = new ArrayList<ScoreSum>();
    private List<ScoreSum> mAvgScores = new ArrayList<ScoreSum>();
    private int mSwimTime = 0;

    public NameScoreListAdapter(Context mContext, List<List<Score>> mLists,
                                List<ScoreSum> mTemps, List<ScoreSum> avgScores, int mSwimTime,DBManager dbManager) {
        this.mContext = mContext;
        this.mLists = mLists;
        this.mTemps = mTemps;
        this.mAvgScores = avgScores;
        this.mSwimTime = mSwimTime;
        this.dbManager = dbManager;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return mLists.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
            convertView = View.inflate(mContext,
                    R.layout.show_score_list_item_sub, null);
            TextView rank = (TextView) convertView
                    .findViewById(R.id.show_rank);
            TextView score = (TextView) convertView
                    .findViewById(R.id.show_score);
            TextView name = (TextView) convertView
                    .findViewById(R.id.show_name);

        if (groupPosition < mSwimTime - 2) {
            rank.setText("第" + (childPosition + 1) + "名");
            score.setText(mLists.get(groupPosition)
                    .get(childPosition).getScore());
            /**
             * 要转换为名字啦
             */
            int athlete_id = mLists.get(groupPosition).get(childPosition).getAthlete_id();
            name.setText(dbManager.getAthleteByAid(athlete_id).getName());
        } else if (groupPosition == (mSwimTime - 2)) {
            rank.setText("第" + (childPosition + 1) + "名");
            score.setText(mTemps.get(childPosition).getScore());
            int aid = mTemps.get(childPosition).getAthlete_id();
            name.setText(dbManager.getAthleteByAid(aid).getName());
        } else {
            rank.setText("第" + (childPosition + 1) + "名");
            score.setText(mAvgScores.get(childPosition)
                    .getScore());
            int aid = mAvgScores.get(childPosition)
                    .getAthlete_id();
            name.setText(dbManager.getAthleteByAid(aid).getName());
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // TODO Auto-generated method stub
        if (mLists.size() == 0) {
            return 0;
        }
        return mLists.get(0).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return groupPosition;
    }

    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return mSwimTime;
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
            convertView = View.inflate(mContext,
                    R.layout.query_score_list_item_head, null);
            TextView timeTextView = (TextView) convertView
                    .findViewById(R.id.test_date);
            TextView curDistance = (TextView) convertView
                    .findViewById(R.id.test_plan);
        if (groupPosition < getGroupCount() - 2) {
            timeTextView.setText("第" + (groupPosition + 1)
                    + "趟");
            curDistance.setText("当前距离 "
                    + mLists.get(groupPosition).get(0).getDistance() + "米");
        } else if (groupPosition == (getGroupCount() - 2)) {
            /**
             * 想一下如何隐藏或者正确显示米数
             */
            timeTextView.setText("本轮总计");
            curDistance.setVisibility(View.GONE);
        } else {
            timeTextView.setText("平均成绩");
            curDistance.setVisibility(View.GONE);
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return false;
    }


}

