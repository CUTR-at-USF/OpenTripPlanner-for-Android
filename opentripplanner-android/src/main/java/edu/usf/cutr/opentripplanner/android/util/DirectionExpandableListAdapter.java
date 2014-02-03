/*
 * Copyright 2012 University of South Florida
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package edu.usf.cutr.opentripplanner.android.util;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import edu.usf.cutr.opentripplanner.android.R;
import edu.usf.cutr.opentripplanner.android.model.Direction;

/**
 * @author Khoa Tran
 */

public class DirectionExpandableListAdapter extends BaseExpandableListAdapter {

    Context context;

    int layoutResourceId;

    Direction data[] = null;

    public DirectionExpandableListAdapter(Context context, int layoutResourceId, Direction[] data) {
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<Direction> subDirections = data[groupPosition].getSubDirections();

        if (subDirections != null && !subDirections.isEmpty()) {
            return subDirections.get(childPosition);
        }

        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<Direction> subDirections = data[groupPosition].getSubDirections();

        if (subDirections != null) {
            return subDirections.size();
        }

        return 0;
    }

    public TextView getGenericView() {
        // Layout parameters for the ExpandableListView
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        TextView textView = new TextView(context);
        textView.setLayoutParams(lp);
        // Center the text vertically
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        // Set the text starting position
        textView.setPadding(36, 0, 0, 0);
        return textView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        TextView textView = getGenericView();
        Direction subDirection = (Direction) getChild(groupPosition, childPosition);

        String text = subDirection == null ? "null here" : subDirection.getDirectionText();

        textView.setText(text);
        return textView;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return data[groupPosition];
    }

    @Override
    public int getGroupCount() {
        return data.length;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {
//        TextView textView = getGenericView();
//        textView.setText(getGroup(groupPosition).toString());

        View row = convertView;
        DirectionHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new DirectionHolder();
            holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);
            holder.txtDirection = (TextView) row.findViewById(R.id.directionText);

            row.setTag(holder);
        } else {
            holder = (DirectionHolder) row.getTag();
        }

        Direction dir = data[groupPosition];
        holder.txtDirection.setText(dir.getDirectionIndex() + ". " + dir.getDirectionText());
        holder.imgIcon.setImageResource(dir.getIcon());

        return row;

//        return textView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    static class DirectionHolder {

        ImageView imgIcon;

        TextView txtDirection;
    }
}