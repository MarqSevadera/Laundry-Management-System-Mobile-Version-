package com.marquesevadera.lotr.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.marquesevadera.lotr.Model.Transaction;
import com.marquesevadera.lotr.R;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ASUS on 2/19/2018.
 */
public class TransactionAdapter extends ArrayAdapter<Transaction> {

    private Activity activity;
    private ArrayList<Transaction> transactionListItems;
    private static LayoutInflater inflater = null;

    public TransactionAdapter(Activity activity, int textViewResourceId, ArrayList<Transaction> _transactionListItems) {
        super(activity, textViewResourceId ,_transactionListItems);
        try{
            this.activity = activity;
            this.transactionListItems = _transactionListItems;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }catch (Exception e){

        }
    }


    public static class ViewHolder{
        public TextView customer_name;
        public TextView date_received;
        public TextView tot_price;
        public TextView transaction_id;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        final ViewHolder holder;
        try{
            if(convertView == null){
                v = inflater.inflate(R.layout.transaction_list_item_layout,null);
                holder = new ViewHolder();
                holder.customer_name = (TextView) v.findViewById(R.id.tvCustomerName);
                holder.date_received = (TextView) v.findViewById(R.id.tvDateReceived);
                holder.tot_price = (TextView) v.findViewById(R.id.tvTotalPrice);
                holder.transaction_id = (TextView) v.findViewById(R.id.tvTransaction);
                v.setTag(holder);
            }else{
                holder = (ViewHolder) v.getTag();
            }

            Transaction transaction = transactionListItems.get(position);
            DecimalFormat df = new DecimalFormat("#.##");
            holder.customer_name.setText(transaction.getName());
            holder.date_received.setText(transaction.getReceived_date());
            holder.tot_price.setText(df.format(transaction.getTotprice() + transaction.getFine()));
            holder.transaction_id.setText(transaction.getTransaction_id());
            if(transaction.isSettled()){
                holder.customer_name.setTextColor(Color.GRAY);
                holder.date_received.setTextColor(Color.GRAY);
                holder.tot_price.setTextColor(Color.GRAY);
                holder.transaction_id.setTextColor(Color.GRAY);
            }else{
                try{
                    Date today = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    Date dispatchmentDate;
                    dispatchmentDate = sdf.parse(transaction.getDispatchment_date());

                    if(today.equals(dispatchmentDate) || today.after(dispatchmentDate)){
                        holder.customer_name.setTextColor(Color.RED);
                        holder.transaction_id.setTextColor(Color.RED);
                    }
                }catch (Exception ex){ex.printStackTrace();}

            }
        }catch (Exception e){}


        return v;
    }
}
