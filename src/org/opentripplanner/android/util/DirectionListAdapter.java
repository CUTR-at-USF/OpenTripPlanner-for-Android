package org.opentripplanner.android.util;

import org.opentripplanner.android.R;
import org.opentripplanner.android.R.id;
import org.opentripplanner.android.model.Direction;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DirectionListAdapter extends ArrayAdapter<Direction>{
	Context context; 
    int layoutResourceId;    
    Direction data[] = null;
    
    public DirectionListAdapter(Context context, int layoutResourceId, Direction[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        DirectionHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new DirectionHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            holder.txtDirection = (TextView)row.findViewById(R.id.directionText);
            
            row.setTag(holder);
        }
        else
        {
            holder = (DirectionHolder)row.getTag();
        }
        
        Direction dir = data[position];
        holder.txtDirection.setText(dir.getDirectionText());
        holder.imgIcon.setImageResource(dir.getIcon());
        
        return row;
    }
    
    static class DirectionHolder
    {
        ImageView imgIcon;
        TextView txtDirection;
    }
}
