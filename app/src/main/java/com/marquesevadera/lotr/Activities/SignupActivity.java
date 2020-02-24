package com.marquesevadera.lotr.Activities;



import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import com.marquesevadera.lotr.Fragments.SignupMain;
import com.marquesevadera.lotr.R;

public class SignupActivity extends AppCompatActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.signup_container,new SignupMain()).commit();

    }




}
