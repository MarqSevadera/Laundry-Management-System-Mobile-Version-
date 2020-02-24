package com.marquesevadera.lotr.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.marquesevadera.lotr.Model.Staff;
import com.marquesevadera.lotr.R;


import java.util.ArrayList;

/**
 * Created by ASUS on 2/1/2018.
 */
public class StaffAdapter extends ArrayAdapter<Staff> {
    private Activity activity;
    private ArrayList<Staff> staffListItems;
    private static LayoutInflater inflater = null;


    public StaffAdapter(Activity activity, int textViewResourceId, ArrayList<Staff> _staffListItems) {
        super(activity, textViewResourceId ,_staffListItems);
        try{
            this.activity = activity;
            this.staffListItems = _staffListItems;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }catch (Exception e){

        }


    }

    public static class ViewHolder{
        public TextView display_name;
        public TextView display_email;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        final ViewHolder holder;
        try{
            if(convertView == null){
                v = inflater.inflate(R.layout.list_item_layout,null);
                holder = new ViewHolder();
                holder.display_name = (TextView) v.findViewById(R.id.tvStaffName);
                holder.display_email = (TextView) v.findViewById(R.id.tvStaffEmail);
                v.setTag(holder);
            }else{
                holder = (ViewHolder) v.getTag();
            }
            holder.display_email.setText(staffListItems.get(position).getEmail());
            holder.display_name.setText(staffListItems.get(position).getName());
        }catch (Exception e){}


        return v;
    }
}
