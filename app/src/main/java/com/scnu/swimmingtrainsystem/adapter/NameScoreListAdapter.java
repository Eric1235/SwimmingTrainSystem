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
 * FIXME
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
        ChildHolder childHolder = null;
        if (convertView == null) {
            childHolder = new ChildHolder();
            convertView = View.inflate(mContext,
                    R.layout.show_score_list_item_sub, null);
            childHolder.rank = (TextView) convertView
                    .findViewById(R.id.show_rank);
            childHolder.score = (TextView) convertView
                    .findViewById(R.id.show_score);
            childHolder.name = (TextView) convertView
                    .findViewById(R.id.show_name);
            convertView.setTag(childHolder);
        } else {
            childHolder = (ChildHolder) convertView.getTag();
        }

        if (groupPosition < mSwimTime - 2) {
            childHolder.rank.setText("第" + (childPosition + 1) + "名");
            childHolder.score.setText(mLists.get(groupPosition)
                    .get(childPosition).getScore());
            /**
             * 要转换为名字啦
             */
            int athlete_id = mLists.get(groupPosition).get(childPosition).getAthlete_id();
            childHolder.name.setText(dbManager.getAthleteByAid(athlete_id).getName());
        } else if (groupPosition == (mSwimTime - 2)) {
            childHolder.rank.setText("第" + (childPosition + 1) + "名");
            childHolder.score.setText(mTemps.get(childPosition).getScore());
            int aid = mTemps.get(childPosition).getAthlete_id();
            childHolder.name.setText(dbManager.getAthleteByAid(aid).getName());
        } else {
            childHolder.rank.setText("第" + (childPosition + 1) + "名");
            childHolder.score.setText(mAvgScores.get(childPosition)
                    .getScore());
            int aid = mAvgScores.get(childPosition)
                    .getAthlete_id();
            childHolder.name.setText(dbManager.getAthleteByAid(aid).getName());
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
        GroupHolder groupHolder = null;
        if (convertView == null) {
            groupHolder = new GroupHolder();
            convertView = View.inflate(mContext,
                    R.layout.query_score_list_item_head, null);
            groupHolder.timeTextView = (TextView) convertView
                    .findViewById(R.id.test_date);
            groupHolder.curDistance = (TextView) convertView
                    .findViewById(R.id.test_plan);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }

        if (groupPosition < getGroupCount() - 2) {
            groupHolder.timeTextView.setText("第" + (groupPosition + 1)
                    + "趟");
            groupHolder.curDistance.setText("当前距离 "
                    + mLists.get(groupPosition).get(0).getDistance() + "米");
        } else if (groupPosition == (getGroupCount() - 2)) {
            /**
             * 想一下如何隐藏或者正确显示米数
             */
            groupHolder.timeTextView.setText("本轮总计");
        } else {
            groupHolder.timeTextView.setText("平均成绩");
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

    final class GroupHolder {
        private TextView timeTextView;
        private TextView curDistance;
    }

    final class ChildHolder {
        private TextView rank;
        private TextView score;
        private TextView name;
    }

}

