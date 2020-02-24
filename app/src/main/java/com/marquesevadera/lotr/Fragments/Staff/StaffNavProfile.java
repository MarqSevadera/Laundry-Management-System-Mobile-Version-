package com.marquesevadera.lotr.Fragments.Staff;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marquesevadera.lotr.Enums.Role;
import com.marquesevadera.lotr.Enums.Constraints;
import com.marquesevadera.lotr.Model.Database;
import com.marquesevadera.lotr.Model.Staff;
import com.marquesevadera.lotr.R;


public class StaffNavProfile extends Fragment {

    private EditText editPhone , editBranch, editAddress , editName;
    private TextView txtSettled , txtReceived ,txtTotal;
    private Menu menu;
    private MenuItem edit,done , cancel;


    private Staff staff;



    private ValueEventListener staffListener;
    private Database database = new Database();
    private DatabaseReference staffReference;
    private String staffID;


    Role role;

    public static String ARG_STAFF = "Staff";
    public static String ARG_CONSTRAINTS = "Constraints";
    public static String ARG_ROLE = "Role";
    public static String ARG_ACTION_BAR_TITLE = "Title";
    public static String ARG_STAFF_ID = "StaffID";

    public StaffNavProfile() {
        // Required empty public constructor
    }

    private OnConstraintSelectedListener mCallback;

   public interface OnConstraintSelectedListener{
        void onConstraintSelected(Constraints constraints);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnConstraintSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnConstraintSelectedListener");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(getArguments() != null){
            role = (Role) getArguments().getSerializable(ARG_ROLE);

            if(role == Role.ADMIN){
                staff = (Staff) getArguments().getSerializable(ARG_STAFF);
                staffID = staff.getStaffref();
            }else{
                staffID = getArguments().getString(ARG_STAFF_ID);
                staffReference = database.getStaff_tbl().child(staffID);
                InitStaffListener();
            }


        }else{
            role = Role.STAFF;
            staffID = database.getUserID();
            staffReference = database.getStaff_tbl().child(staffID);
            InitStaffListener();
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.staff_nav_profile, container, false);
        if(role == Role.ADMIN)
            setHasOptionsMenu(false);
        else
            setHasOptionsMenu(true);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit_menu, menu);
        this.menu = menu;
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        AddTextViewListener();

        if(role == Role.ADMIN){
            getActivity().setTitle(staff.getName());
            SetProfileValues(null);
        }
        else{
            getActivity().setTitle("My Profile");
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        edit = menu.findItem(R.id.edit_action);
        done = menu.findItem(R.id.done_action);
        cancel = menu.findItem(R.id.cancel_action);

        if(item.getItemId() == R.id.edit_action){
            editAddress.setEnabled(true);
            editName.setEnabled(true);
            editPhone.setEnabled(true);
            edit.setVisible(false);
            done.setVisible(true);
            cancel.setVisible(true);
        }
        if(item.getItemId() == R.id.done_action){
            editAddress.setEnabled(false);
            editName.setEnabled(false);
            editPhone.setEnabled(false);
            edit.setVisible(true);
            done.setVisible(false);
            cancel.setVisible(false);
            SaveEdit();
        }
        if(item.getItemId() == R.id.cancel_action){
            editAddress.setEnabled(false);
            editName.setEnabled(false);
            editPhone.setEnabled(false);
            edit.setVisible(true);
            done.setVisible(false);
            cancel.setVisible(false);
            InitStaffListener();
        }

        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onDestroy() {
        removeListener(FirebaseDatabase.getInstance().getReference("Staff"), staffListener);
        super.onDestroy();
    }



    private void InitStaffListener(){
        removeListener(staffReference, staffListener);
        staffListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SetProfileValues(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        staffReference.addValueEventListener(staffListener);
    }



    private void initView(){
        //Text Views
        txtReceived = (TextView) getActivity().findViewById(R.id.receivedTV);
        txtSettled= (TextView) getActivity().findViewById(R.id.settledTV);
        txtTotal = (TextView) getActivity().findViewById(R.id.totalTV);
        //Edit Texts
        editName = (EditText) getActivity().findViewById(R.id.staff_name_holder);
        editAddress = (EditText) getActivity().findViewById(R.id.staff_address);
        editPhone = (EditText) getActivity().findViewById(R.id.staff_phone);
        editBranch = (EditText) getActivity().findViewById(R.id.staff_branch);


    }



    private void AddTextViewListener(){

        txtReceived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onConstraintSelected(Constraints.RECEIVED);
            }
        });


        txtSettled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onConstraintSelected(Constraints.DISPATCHED);
            }
        });


        txtTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onConstraintSelected(Constraints.ALL);
            }
        });
    }






    private void SetProfileValues(DataSnapshot dataSnapshot){

        if(dataSnapshot!=null) {
            staff = dataSnapshot.getValue(Staff.class);
        }

        int totalProcessed = staff.getReceived() + staff.getSettled();

        txtSettled.setText(Integer.toString(staff.getSettled()));
        txtReceived.setText(Integer.toString(staff.getReceived()));
        txtTotal.setText(Integer.toString(totalProcessed));
        editName.setText(staff.getName());
        editAddress.setText(staff.getAddress());
        editBranch.setText(staff.getBranch());
        editPhone.setText(staff.getPhone());

    }


    private String getEditTextResponse(){
        String response = "";
        if(TextUtils.isEmpty(editName.getText().toString().trim())){
            response = "You should enter your name!";
        }
        return  response;
    }



    private void SaveEdit(){
        String response = getEditTextResponse();
        if(!TextUtils.isEmpty(response)){
            Toast.makeText(getActivity(), response , Toast.LENGTH_SHORT).show();
            SetProfileValues(null);
            return;
        }

        try{
            staff.setName(editName.getText().toString());
            staff.setAddress(editAddress.getText().toString());
            staff.setPhone(editPhone.getText().toString());
            staffReference.setValue(staff);
            Toast.makeText(getActivity(),"Profile Updated!" , Toast.LENGTH_SHORT).show();
        }catch (Exception e){e.printStackTrace();}

    }


    private void removeListener(DatabaseReference dr , ValueEventListener listener){
        if(listener !=null){
            dr.removeEventListener(listener);
        }
    }



}
