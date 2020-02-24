package com.marquesevadera.lotr.Fragments;


import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.marquesevadera.lotr.Activities.LoginActivity;
import com.marquesevadera.lotr.R;

import com.marquesevadera.lotr.Fragments.Staff.Signup;


public class SignupMain extends Fragment implements View.OnClickListener{


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return  inflater.inflate(R.layout.fragment_signup, container, false);

    }

    @Override
    public void onViewCreated(View view , @Nullable Bundle savedInstanceState){
        super.onViewCreated(view , savedInstanceState);
        initView(view);
    }



    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.btnAdmin: getFragmentManager().beginTransaction().replace(R.id.signup_container, new com.marquesevadera.lotr.Fragments.Admin.Signup()).addToBackStack(null).commit();
                break;
            case R.id.btnStaff: getFragmentManager().beginTransaction().replace(R.id.signup_container, new Signup()).addToBackStack(null).commit();
                break;
            case R.id.signup_loginhere:{
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }

        }
    }


    private void initView(View view){
        Button adminButton = (Button) view.findViewById(R.id.btnAdmin);
        Button staffButton = (Button) view.findViewById(R.id.btnStaff);
        TextView signupLoginhere = (TextView) view.findViewById(R.id.signup_loginhere);
        signupLoginhere.setPaintFlags(signupLoginhere.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        adminButton.setOnClickListener(this);
        staffButton.setOnClickListener(this);
        signupLoginhere.setOnClickListener(this);
    }





}
