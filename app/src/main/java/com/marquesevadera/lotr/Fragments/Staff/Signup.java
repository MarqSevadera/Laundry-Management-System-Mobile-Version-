package com.marquesevadera.lotr.Fragments.Staff;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marquesevadera.lotr.Activities.LoginActivity;
import com.marquesevadera.lotr.Model.Admin;
import com.marquesevadera.lotr.Model.Staff;
import com.marquesevadera.lotr.R;
import com.rengwuxian.materialedittext.MaterialEditText;

public class Signup extends Fragment implements View.OnClickListener{



    private static final int PASSWORD_LENGTH = 6;

    private String strAdmincode , strEmail, strPassword, strFullName , strAdminReference , strBranch;
    private EditText admincode , email , password , fullname;
    private ProgressDialog mDialog;

    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private String currentUser;
    private Admin admin;
    private Staff staff;
    private ValueEventListener mListener;


    public Signup() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_staff, container, false);
        TextView tv = (TextView)v.findViewById(R.id.staff_loginhere);
        tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        //initialize database
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        return v;
    }



    @Override
    public void onViewCreated(View view , @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        TextView tv = (TextView) view.findViewById(R.id.staff_loginhere);

        tv.setOnClickListener(this);

        Button adminRegister = (Button) view.findViewById(R.id.staff_register);
        adminRegister.setOnClickListener(this);

        mDialog = new ProgressDialog(getActivity());

        admincode = (MaterialEditText) view.findViewById(R.id.staff_adminCode);
        email = (MaterialEditText) view.findViewById(R.id.staff_email);
        password = (MaterialEditText) view.findViewById(R.id.staff_password);
        fullname = (MaterialEditText) view.findViewById(R.id.staff_fullName);


    }





    private void Register(){

        if(!isEditTextComplete()){
            return;
        }

        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage("Please wait..");
        mDialog.show();

        final DatabaseReference dbRef =  FirebaseDatabase.getInstance().getReference("Admin");


        mListener = dbRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                strAdminReference = null; //reset admin code
                for (DataSnapshot adminSnapshot : dataSnapshot.getChildren()) {
                    admin = adminSnapshot.getValue(Admin.class);
                    if (admin.getAdmincode().equals(strAdmincode)) {
                        strAdminReference = admin.getAdminref();
                        strBranch = admin.getBranch();
                        break;
                    }
                }


                if (strAdminReference != null) {
                    auth.createUserWithEmailAndPassword(strEmail, strPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                mDialog.dismiss();
                                currentUser = auth.getCurrentUser().getUid(); //this current user
                                addToStaffRequestDatabase(); //add to Admin's Staff Requests Database



                                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                                alertDialog.setTitle("Request Successfully Sent!");
                                alertDialog.setMessage("A request to join to your administrator has been successfully sent!");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                auth.signOut();
                                                removeListener(mListener , dbRef);
                                                LoginHere();

                                            }
                                        });
                                alertDialog.show();


                            } else {
                                mDialog.dismiss();
                                removeListener(mListener, dbRef);
                                email.setError("This email might be registered already or invalid!");

                            }
                        }
                    });
                } else {
                    mDialog.dismiss();
                    removeListener(mListener , dbRef);
                    admincode.setError("Invalid Admin Code!");

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });





    }



    private boolean isEditTextComplete(){
        strAdmincode = admincode.getText().toString().trim();
        strEmail = email.getText().toString().trim();
        strPassword = password.getText().toString().trim();
        strFullName  =  fullname.getText().toString().trim();

        if(TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strPassword) || TextUtils.isEmpty(strFullName) || TextUtils.isEmpty(strAdmincode)){
            if(TextUtils.isEmpty(strEmail))
                email.setError("This field is required!");

            if(TextUtils.isEmpty(strPassword))
                password.setError("This field is required!");

            if(TextUtils.isEmpty(strFullName))
                fullname.setError("This field is required!");

            if(TextUtils.isEmpty(strAdmincode))
                admincode.setError("This field is required");

            return false;

        } else if(strPassword.length() < PASSWORD_LENGTH){
            password.setError("Your password length must be 6 characters and above!");
            return false;

        } else{
            return true;
        }

    }

    private void addToStaffRequestDatabase(){
        staff = new Staff();
        staff.setAdminref(strAdminReference);
        staff.setName(strFullName);
        staff.setStaffref(auth.getCurrentUser().getUid());
        staff.setBranch(strBranch);
        staff.setEmail(strEmail);
        staff.setAddress("Not Set");
        staff.setPhone("Not Set");

        dbRef = database.getReference("StaffRequest");
        dbRef.child(currentUser).setValue(staff);

    }
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.staff_loginhere:LoginHere();
                break;
            case R.id.staff_register:Register();
                break;
        }
    }

    private void removeListener(ValueEventListener mListener , DatabaseReference dbRef){
        if(mListener  != null){
            dbRef.removeEventListener(mListener);
        }
    }

    private void LoginHere(){
        Intent intent = new Intent(getActivity() ,  LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }


}
