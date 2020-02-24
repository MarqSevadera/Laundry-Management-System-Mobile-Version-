package com.marquesevadera.lotr.Fragments.Admin;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marquesevadera.lotr.Adapters.StaffAdapter;
import com.marquesevadera.lotr.Enums.ACTION;
import com.marquesevadera.lotr.Enums.KEY;
import com.marquesevadera.lotr.Model.Staff;
import com.marquesevadera.lotr.R;

import java.util.ArrayList;


public class AdminNavStaffs extends Fragment {

    private ListView staffListView;

    private DatabaseReference staff_tbl;
    private ArrayList<Staff> staffList;


    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private String currentUser = auth.getCurrentUser().getUid();

    private ValueEventListener staffListener;


    public AdminNavStaffs() {
        // Required empty public constructor
    }




    private OnStaffSelectedListener mCallback;

    public interface OnStaffSelectedListener{
        public void onStaffSelected(Staff staff , ACTION action);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnStaffSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnStaffSelectedListener");
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.staff_list_layout, container, false);
        initViews(view);
        return view;
    }



    private void initViews(View view){
        final TextView tv  = (TextView) view.findViewById(R.id.empty_request);

        staff_tbl= FirebaseDatabase.getInstance().getReference();

        staffListView = (ListView) view.findViewById(R.id.staffListView);
        staffList = new ArrayList<>();

        staffListener =  staff_tbl.child("Staff").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                staffList.clear();
                for (DataSnapshot reqSnapshot : dataSnapshot.getChildren()) {
                    Staff staff = reqSnapshot.getValue(Staff.class);
                    if(staff.getAdminref().equals(currentUser)){
                        staffList.add(staff);
                    }
                }

                if(staffList.isEmpty()){
                    tv.setText(R.string.empty_record);
                }else{
                    tv.setText("");
                }

                if(getActivity() == null) return;

                StaffAdapter adapter = new StaffAdapter(getActivity(), 0 ,staffList);
                staffListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        staffListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Staff staff = staffList.get(position);
                mCallback.onStaffSelected(staff , ACTION.SELECT);
            }
        });


        staffListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Staff staff = staffList.get(position);
                mCallback.onStaffSelected(staff , ACTION.LONG_PRESS);
                return true;
            }
        });
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Staffs");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(staffListener!=null)
         FirebaseDatabase.getInstance().getReference("Staff").removeEventListener(staffListener);
    }





}
