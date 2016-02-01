package com.flywolf.familytree;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class LeafsAdapter
        extends BaseAdapter {

    private ArrayList<DbWorker.Relative> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public LeafsAdapter(Context context, ArrayList<DbWorker.Relative> listData) {
        this.listData = listData;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.leaf_layout, null);
            holder = new ViewHolder();
            holder.leafBirthday = (TextView) convertView.findViewById(R.id.leafBirthday);
            holder.leafText = (TextView) convertView.findViewById(R.id.leafText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.leafBirthday.setText(listData.get(position).getBirthday().toString());
        holder.leafText.setText(listData.get(position).getName());

       /* if (position % 2 == 1) {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.list_row_color1));
        } else {
            convertView.setBackgroundColor(context.getResources().getColor(R.color.list_row_color2));
        }
*/
        return convertView;
    }

    static class ViewHolder {
        ImageView leaf;
        ImageView leafFrame;
        TextView leafBirthday;
        TextView leafText;
    }

}
