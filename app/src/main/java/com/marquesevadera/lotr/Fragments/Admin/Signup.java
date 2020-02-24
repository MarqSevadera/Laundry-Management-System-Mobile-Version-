package com.marquesevadera.lotr.Fragments.Admin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.marquesevadera.lotr.Activities.LoginActivity;
import com.marquesevadera.lotr.Model.Admin;
import com.marquesevadera.lotr.Model.LaundryRate;
import com.marquesevadera.lotr.R;
import com.rengwuxian.materialedittext.MaterialEditText;


import org.w3c.dom.Text;

import java.util.Random;


public class Signup extends Fragment implements View.OnClickListener{

    private static final int PASSWORD_LENGTH = 6;
    private static final int ADMINCODE_LENGTH = 6;

    private Button btnRegister;
    private EditText email , password , fullname;
    private ProgressDialog mDialog;
    private TextView loginHere;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private FirebaseAuth auth;
    private String strBranch , strEmail,strPassword , strFullName;
    private FirebaseUser user;
    public Signup(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_admin, container, false);

        TextView tv = (TextView)view.findViewById(R.id.admin_loginhere);
        tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        return view;

    }

    @Override
    public void onViewCreated(View view , @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        initView(view);


    }




    private void initView(View view){

        btnRegister = (Button)view.findViewById(R.id.admin_register);
        email = (MaterialEditText)view.findViewById(R.id.admin_email);
        password = (MaterialEditText)view.findViewById(R.id.admin_password);
        fullname = (MaterialEditText)view.findViewById(R.id.admin_fullName);
        loginHere = (TextView)view.findViewById(R.id.admin_loginhere);


        mDialog = new ProgressDialog(getActivity());
        btnRegister.setOnClickListener(this);
        loginHere.setOnClickListener(this);

        //initialize database
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

    }




    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.admin_loginhere: LoginHere();
                break;
            case R.id.admin_register:Register();
        }

    }




    public void Register(){

                if(!isEditTextComplete()){
                    return;
                }

                mDialog = new ProgressDialog(getActivity());
                mDialog.setMessage("Please wait..");
                mDialog.show();

                auth.createUserWithEmailAndPassword(strEmail, strPassword).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mDialog.dismiss();
                            addToDatabase();
                            user = auth.getCurrentUser();
                            user.sendEmailVerification().addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getActivity(),
                                                "Verification email sent to " + user.getEmail(),
                                                Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getActivity(), LoginActivity.class));
                                        getActivity().finish();

                                    } else {
                                        Log.e("SEND EMAIL VERIFICATION", "sendEmailVerification", task.getException());
                                        Toast.makeText(getActivity(),
                                                "Failed to send verification email.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            mDialog.dismiss();
                            email.setError("The email might be registered already or invalid!");
                        }
                    }
                });



    }



    private void addToDatabase(){

        //adding to Admin Table
        String currentUser = auth.getCurrentUser().getUid();
        dbRef = database.getReference("Admin");
        String adminCode = generateAdminCode();
        Admin admin = new Admin();
        admin.setBranch(strBranch);
        admin.setName(strFullName);
        admin.setPhone("Add phone here..");
        admin.setAddress("Add address here..");
        admin.setAdmincode(adminCode);
        admin.setAdminref(currentUser);

        dbRef.child(currentUser).setValue(admin);

        //adding to Laundry Rate Table

        dbRef = database.getReference("LaundryRate");
        LaundryRate lr = new LaundryRate(); //since we have default values in Laundry rate, we dont need to set its fields
        dbRef.child(auth.getCurrentUser().getUid()).setValue(lr);


    }



    private boolean isEditTextComplete(){

          strEmail = email.getText().toString().trim();
          strPassword = password.getText().toString().trim();
          strFullName  =  fullname.getText().toString().trim();

        if(TextUtils.isEmpty(strEmail) || TextUtils.isEmpty(strPassword) || TextUtils.isEmpty(strFullName)){
            if(TextUtils.isEmpty(strEmail))
                email.setError("This field is required!");

            if(TextUtils.isEmpty(strPassword))
                password.setError("This field is required!");

            if(TextUtils.isEmpty(strFullName))
                fullname.setError("This field is required!");

            return false;

        } else if(strPassword.length() < PASSWORD_LENGTH){
            password.setError("Your password length must be 6 characters and above!");
            return false;

        } else{
            return true;
        }

    }



    private void LoginHere(){
        Intent intent = new Intent(getActivity() , LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }


    private String generateAdminCode(){

            String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
            StringBuilder salt = new StringBuilder();
            Random rnd = new Random();
            while (salt.length() < ADMINCODE_LENGTH) { // length of the random string.
                int index = (int) (rnd.nextFloat() * SALTCHARS.length());
                salt.append(SALTCHARS.charAt(index));
            }
            String saltStr = salt.toString();
            return saltStr;
    }


}

