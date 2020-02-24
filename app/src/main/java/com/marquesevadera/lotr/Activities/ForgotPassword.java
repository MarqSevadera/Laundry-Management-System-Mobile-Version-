package com.marquesevadera.lotr.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.marquesevadera.lotr.Enums.Role;
import com.marquesevadera.lotr.Model.Database;
import com.marquesevadera.lotr.Model.LaundryRate;
import com.marquesevadera.lotr.R;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * Created by ASUS on 3/15/2018.
 */
public class ForgotPassword extends AppCompatActivity {

    Button button;
    MaterialEditText edtEmail;
    public ForgotPassword() {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_pass_layout);
        initView();
    }




    private void initView(){
        button = (Button)findViewById(R.id.btnSendRequest);
        edtEmail = (MaterialEditText)findViewById(R.id.edtEmail);

    }

    public void onClick(View v){
        final ProgressDialog mDialog = new ProgressDialog(ForgotPassword.this);
        mDialog.setMessage("Please wait...");
        mDialog.show();
        final String email = edtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            mDialog.dismiss();
            edtEmail.setError("Enter your email!");
        } else {
            mDialog.dismiss();
            FirebaseAuth auth = FirebaseAuth.getInstance();
           auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ForgotPassword.this);
                        mBuilder.setTitle("Reset password request sent!");
                        mBuilder.setMessage("We have sent an instruction to " + email + "to reset your password.");
                        mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();

                            }
                        });

                        AlertDialog alertDialog = mBuilder.create();
                        alertDialog.show();
                    }
                }
            });
        }
    }

}
