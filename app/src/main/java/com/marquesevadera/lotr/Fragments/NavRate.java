package com.marquesevadera.lotr.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marquesevadera.lotr.Enums.Role;
import com.marquesevadera.lotr.Model.Database;
import com.marquesevadera.lotr.Model.LaundryRate;
import com.marquesevadera.lotr.R;

public class NavRate extends android.app.Fragment {

    EditText editRegular, editHandwashed, editHeavyFabrics , editDays , editFine;


    Menu menu;
    MenuItem edit,done , cancel;
    private Role UserRole;
    private String laundryRateTable = "LaundryRate" , staffTable = "Staff";
    private DatabaseReference rateRef,staffRef;
    private String currentUserID , AdminReference;
    private FirebaseUser currentUser;
    private LaundryRate rate;

    private ValueEventListener StaffListener , LaundryRateListener;


    public NavRate() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View v = inflater.inflate(R.layout.nav_rate, container, false);




        rateRef = FirebaseDatabase.getInstance().getReference(laundryRateTable);
        staffRef = FirebaseDatabase.getInstance().getReference(staffTable);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        rate = new LaundryRate();


        if(currentUser.isEmailVerified()){ //if it is verified we know that it's an admin , else its a user
            UserRole = Role.ADMIN;
            setHasOptionsMenu(true);
        }else{
            UserRole = Role.STAFF;
            setHasOptionsMenu(false); //we dont want the user to edit our rate variables!
            InitStaffListener();
        }

        InitLaundryRateListener();

        return v;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Rate");
        initView();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_menu, menu);
        this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        edit = menu.findItem(R.id.edit_action);
        done = menu.findItem(R.id.done_action);
        cancel = menu.findItem(R.id.cancel_action);

        if(item.getItemId() == R.id.edit_action){
            editRegular.setEnabled(true);
            editHandwashed.setEnabled(true);
            editHeavyFabrics.setEnabled(true);
            editFine.setEnabled(true);
            editDays.setEnabled(true);
            edit.setVisible(false);
            done.setVisible(true);
            cancel.setVisible(true);
        }
        if(item.getItemId() == R.id.done_action){
            editRegular.setEnabled(false);
            editHandwashed.setEnabled(false);
            editHeavyFabrics.setEnabled(false);
            editFine.setEnabled(false);
            editDays.setEnabled(false);
            edit.setVisible(true);
            done.setVisible(false);
            cancel.setVisible(false);

            SaveEdit();

        }

        if(item.getItemId() == R.id.cancel_action){
            InitLaundryRateListener();
            editRegular.setEnabled(false);
            editHandwashed.setEnabled(false);
            editHeavyFabrics.setEnabled(false);
            editFine.setEnabled(false);
            editDays.setEnabled(false);
            edit.setVisible(true);
            done.setVisible(false);
            cancel.setVisible(false);
        }

        return super.onOptionsItemSelected(item);


    }

    private void UpdateRateValues(DataSnapshot dataSnapshot){

        if(UserRole == Role.ADMIN){
            rate = dataSnapshot.child(currentUserID).getValue(LaundryRate.class);
        }else{
            String adminID = AdminReference;
            rate = dataSnapshot.child(adminID).getValue(LaundryRate.class);

        }


        String strReg = Double.toString(rate.getRegular());
        String strHandwashed = Double.toString(rate.getHandwashed());
        String strHeavyFabrics = Double.toString(rate.getHeavyfabric());
        String strDays = Integer.toString(rate.getDays());
        String strFine = Double.toString(rate.getFine());


        editRegular.setText(strReg);
        editHandwashed.setText(strHandwashed);
        editHeavyFabrics.setText(strHeavyFabrics);
        editDays.setText(strDays);
        editFine.setText(strFine);

    }

    private void initView(){

        editRegular = (EditText) getActivity().findViewById(R.id.regular_price);
        editHandwashed = (EditText) getActivity().findViewById(R.id.additonal_rate1);
        editHeavyFabrics = (EditText) getActivity().findViewById(R.id.additonal_rate2);
        editDays = (EditText) getActivity().findViewById(R.id.days);
        editFine = (EditText) getActivity().findViewById(R.id.fine);

    }

    private void SaveEdit(){

        try {
            rate.setRegular(Double.parseDouble(editRegular.getText().toString()));
            rate.setHandwashed(Double.parseDouble(editHandwashed.getText().toString()));
            rate.setHeavyfabric(Double.parseDouble(editHeavyFabrics.getText().toString()));
            rate.setDays(Integer.parseInt(editDays.getText().toString()));
            rate.setFine(Double.parseDouble(editFine.getText().toString()));
            rateRef.child(currentUserID).setValue(rate);
            Toast.makeText(getActivity(), "Rate Updated!", Toast.LENGTH_SHORT).show();
        }catch (Exception ex){ex.printStackTrace();}

    }

    private void InitStaffListener(){
        StaffListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot ds) {
                AdminReference = ds.child(currentUserID).child("adminref").getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        staffRef.addValueEventListener(StaffListener);
    }

    private void InitLaundryRateListener(){
        //remove Listener if it exist already!
        removeListener( rateRef , LaundryRateListener);

        LaundryRateListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UpdateRateValues(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        rateRef.addValueEventListener(LaundryRateListener);
    }

    private void removeListener(DatabaseReference dr , ValueEventListener listener){
        if(listener !=null){
            dr.removeEventListener(listener);
        }
    }


}