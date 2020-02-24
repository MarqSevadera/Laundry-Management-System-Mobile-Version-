package com.marquesevadera.lotr.Fragments.Admin;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.marquesevadera.lotr.Model.Database;
import com.marquesevadera.lotr.Model.Staff;
import com.marquesevadera.lotr.Adapters.StaffAdapter;
import com.marquesevadera.lotr.R;

import java.util.ArrayList;

/**
 * Created by ASUS on 2/1/2018.
 */
public class AdminNavNotif extends Fragment{

    private ListView requestListView;

    private ArrayList<Staff> staffList;

    private ValueEventListener mListener;

    private Database database = new Database();


    private String currentUser = database.getUserID();

    private MenuItem item_show_ignored;

    private TextView emptyListTV;

    public AdminNavNotif() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.staff_list_layout,container,false);
        setHasOptionsMenu(true);
        initViews(view);
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        item_show_ignored = menu.findItem(R.id.action_show_ignored);
        item_show_ignored.setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Staff Requests");

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_show_ignored){
            ShowData(database.getStaffIgnore_tbl());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        removeListener(database.getStaffReq_tbl() , mListener);
        super.onDestroy();

    }






    private void initViews(final View view){

        emptyListTV = (TextView) view.findViewById(R.id.empty_request);
        requestListView = (ListView) view.findViewById(R.id.staffListView);
        staffList = new ArrayList<>();

        ShowData(database.getStaffReq_tbl());

        requestListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Staff staff = staffList.get(position);
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                mBuilder.setTitle("Staff Request");
                mBuilder.setMessage(staff.getName() + " want to be your staff.");
                mBuilder.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        AcceptStaffRequest(staff);
                        Toast.makeText(getActivity(), staff.getName() + " was successfully added to your staffs!", Toast.LENGTH_SHORT).show();
                    }
                });
                mBuilder.setNegativeButton("IGNORE", new DialogInterface.OnClickListener() {
                    @Override

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        IgnoreStaffRequest(staff);
                        Toast.makeText(getActivity(), staff.getName() + " was successfully ignored!", Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog alertDialog = mBuilder.create();
                alertDialog.show();
            }
        });

    }


    private void AcceptStaffRequest(Staff staff){

        RemoveFromStaffRequest(staff.getStaffref());
        RemoveFromIgnoredRequest(staff.getStaffref());

        DatabaseReference staffTbl  = database.getStaff_tbl();
        staffTbl.child(staff.getStaffref()).setValue(staff);

        increaseStaffCount();

    }

    private void IgnoreStaffRequest(Staff staff){
        RemoveFromStaffRequest(staff.getStaffref());

        DatabaseReference ignoreTbl = database.getStaffIgnore_tbl();
        ignoreTbl.child(staff.getStaffref()).setValue(staff);

    }

    private void RemoveFromStaffRequest(String staffID){
        DatabaseReference staff = database.getStaffReq_tbl().child(staffID);
        staff.removeValue();
    }

    private void RemoveFromIgnoredRequest(String staffID){
        DatabaseReference staff = database.getStaffIgnore_tbl().child(staffID);
        staff.removeValue();
    }



    private void increaseStaffCount(){
        DatabaseReference countRef = database.getAdmin_tbl().child(currentUser).child("staffcount");
        countRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {

                if (currentData.getValue() == null) {
                    currentData.setValue(1);
                } else {
                    currentData.setValue((Long) currentData.getValue() + 1);
                }

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }






    private void ShowData(DatabaseReference dbRef){

        removeListener(dbRef , mListener);

        mListener = dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                staffList.clear();
                for (DataSnapshot reqSnapshot : dataSnapshot.getChildren()) {
                    Staff staff = reqSnapshot.getValue(Staff.class);
                    if (staff.getAdminref().equals(currentUser)) {
                        staffList.add(staff);
                    }
                }

                if (staffList.isEmpty()) {
                    emptyListTV.setText(R.string.empty_record);
                } else {
                    emptyListTV.setText("");
                }

                StaffAdapter adapter = new StaffAdapter(getActivity(), 0, staffList);
                requestListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void removeListener(DatabaseReference dbRef ,ValueEventListener listener){
        if(listener != null){
            dbRef.removeEventListener(listener);
        }
    }



}
