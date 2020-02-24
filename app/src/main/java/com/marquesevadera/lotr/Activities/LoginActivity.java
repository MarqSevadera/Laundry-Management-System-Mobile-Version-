package com.marquesevadera.lotr.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marquesevadera.lotr.Enums.KEY;
import com.marquesevadera.lotr.Enums.Role;
import com.marquesevadera.lotr.Model.Staff;
import com.marquesevadera.lotr.R;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActivity extends AppCompatActivity {

    private static final int DELAY_BEFORE_EXPIRY = 3000;
    private boolean ClickTwice = false;

    private EditText EdtEmail, EdtPassword;
    private String StrEmail, StrPassword;

    private Button BtnLogin;

    private FirebaseAuth Auth;
    private String CurrentUser = "";
    private String TblName;
    private ValueEventListener StaffListener , AdminListener , StaffRequestListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        InitViews();

    }

    private void InitViews(){
        TextView registerTV = (TextView)findViewById(R.id.registerTV);
        registerTV.setPaintFlags(registerTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        TextView forgetTV = (TextView) findViewById(R.id.forgetTV);
        forgetTV.setPaintFlags(forgetTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        EdtEmail = (MaterialEditText) findViewById(R.id.edtEmail);
        EdtPassword = (MaterialEditText)findViewById(R.id.edtPassword);
        BtnLogin = (Button) findViewById(R.id.btnLogin);

        Auth = FirebaseAuth.getInstance();

        BtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Login();
            }

        });



    }


    private void ValidateCredentials(final Role role , final String databaseName) {

        final ProgressDialog mDialog = new ProgressDialog(LoginActivity.this);
        mDialog.setMessage("Please wait...");
        mDialog.show();

        if(!isCredentialComplete()){
            mDialog.dismiss();
            return;
        }


         Auth.signInWithEmailAndPassword(StrEmail, StrPassword).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
             @Override
             public void onComplete(Task<AuthResult> task) {

                 if (task.isSuccessful()) {
                     CurrentUser = Auth.getCurrentUser().getUid();
                     TblName = databaseName;
                     DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(databaseName);
                     mDialog.dismiss();

                     if (role == Role.ADMIN)
                         ValidateAdmin(dbRef, Auth.getCurrentUser());
                     else
                         ValidateStaff(dbRef, Auth.getCurrentUser());

                 } else {
                     mDialog.dismiss();
                     EdtEmail.setError("Invalid Email or Password!");
                     EdtPassword.setText("");
                     EdtPassword.setError("");
                     Toast.makeText(LoginActivity.this, "Invalid Email or Password!", Toast.LENGTH_SHORT);
                 }
             }
         });



    }


    private void removeListener(ValueEventListener mListener , DatabaseReference dbref){
        if(mListener != null){
           dbref.removeEventListener(mListener);
        }

    }




    private void ValidateAdmin(final DatabaseReference dbRef, final FirebaseUser currentUser){

      AdminListener =   dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(currentUser.getUid()).exists()) {

                    if (currentUser.isEmailVerified()) {
                        Toast.makeText(LoginActivity.this, "Successfully Logged in!", Toast.LENGTH_SHORT).show();
                        dbRef.removeEventListener(AdminListener);
                        finish();
                        startActivity(new Intent(LoginActivity.this, AdminHome.class));
                    } else {
                        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                        alertDialog.setTitle("Verify Email");
                        alertDialog.setMessage("You need to verify your email before you can log in!");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        dbRef.removeEventListener(AdminListener);
                                    }
                                });
                        alertDialog.show();
                        Auth.signOut();
                    }

                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Email or Password!", Toast.LENGTH_SHORT).show();
                    EdtEmail.setError("Invalid Email or Password!");
                    EdtPassword.setText("");
                    EdtPassword.setError("");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void ValidateStaff(final DatabaseReference dbRef, final FirebaseUser currentUser){
        StaffListener  = dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(currentUser.getUid()).exists()){
                    //Staff's verification is done by the admin itself, no need to check if the email is verified
                    dbRef.removeEventListener(StaffListener);
                    Toast.makeText(LoginActivity.this, "Successfully Logged in!", Toast.LENGTH_SHORT).show();
                    Staff staff = dataSnapshot.child(currentUser.getUid()).getValue(Staff.class);
                    Intent intent = new Intent(LoginActivity.this , StaffHome.class);
                    intent.putExtra(KEY.STAFF,staff);
                    finish();
                    startActivity(intent);
                }else{
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("StaffRequest");
                    dbRef.removeEventListener(StaffListener);
                    CheckStaffRequestTable(ref , currentUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void CheckStaffRequestTable(final DatabaseReference dbRef , final FirebaseUser currentUser){
       StaffRequestListener =  dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(currentUser.getUid()).exists()){

                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(LoginActivity.this);
                    mBuilder.setTitle("Pending Request");
                    mBuilder.setMessage("We already sent a request to your administrator. Wait for his/her confirmation before you can login!");
                    mBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            dbRef.removeEventListener(StaffRequestListener);

                        }
                    });

                    AlertDialog alertDialog = mBuilder.create();
                    alertDialog.show();

                }else{
                    Toast.makeText(LoginActivity.this, "Invalid Email or Password!" , Toast.LENGTH_SHORT).show();
                    EdtEmail.setError("Invalid Email or Password!");
                    EdtPassword.setText("");
                    EdtPassword.setError("");
                    dbRef.removeEventListener(StaffRequestListener);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void onClick(View v){
        switch(v.getId()){
            case R.id.registerTV: SignupNow();
                break;
            case R.id.forgetTV: ForgotPassword();
        }
    }

    private void Login(){
        CheckBox cb = (CheckBox)findViewById(R.id.chkbx_AsAdmin);

        if(cb.isChecked())  ValidateCredentials(Role.ADMIN,"Admin");

        else  ValidateCredentials(Role.STAFF,"Staff");
    }

    private void SignupNow(){
        Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(intent);

    }




    private boolean isCredentialComplete(){

         StrEmail = EdtEmail.getText().toString().trim();
         StrPassword = EdtPassword.getText().toString().trim();

        if(TextUtils.isEmpty(StrEmail) && TextUtils.isEmpty(StrPassword)){
            EdtEmail.setError("Email is required!");
            EdtPassword.setError("Password is required!");
            return false;
        }else if(TextUtils.isEmpty(StrEmail) && !TextUtils.isEmpty(StrPassword)){
            EdtPassword.setError("Password is required!");
            return false;
        }else if(!TextUtils.isEmpty(StrEmail) && TextUtils.isEmpty(StrPassword)){
            EdtEmail.setError("Email is required!");
            return false;
        }else{
            return true;
        }


    }

    @Override
    public void onBackPressed(){

        if(ClickTwice){
            if(Auth != null) Auth.signOut();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
            System.exit(0);
        }

        else{
            Toast.makeText(LoginActivity.this,"Press Back Again to Exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable(){
                @Override
                public void run(){
                    ClickTwice = false;

                }
            },DELAY_BEFORE_EXPIRY);

            ClickTwice = true;

        }


    }


    @Override
    protected void onDestroy() {
        removeListener(AdminListener, FirebaseDatabase.getInstance().getReference("Admin"));
        removeListener(StaffListener, FirebaseDatabase.getInstance().getReference("Staff"));
        removeListener(StaffRequestListener, FirebaseDatabase.getInstance().getReference("StaffRequest"));
        super.onDestroy();

    }

    private void ForgotPassword(){
        Intent intent = new Intent(LoginActivity.this, ForgotPassword.class);
        startActivity(intent);

    }




}
