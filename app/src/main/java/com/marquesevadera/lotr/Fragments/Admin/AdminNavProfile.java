package com.marquesevadera.lotr.Fragments.Admin;

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
import com.marquesevadera.lotr.Enums.Constraints;
import com.marquesevadera.lotr.Enums.Role;
import com.marquesevadera.lotr.Fragments.NavTransaction;
import com.marquesevadera.lotr.Fragments.Staff.StaffNavProfile;
import com.marquesevadera.lotr.Model.Admin;
import com.marquesevadera.lotr.Model.Database;
import com.marquesevadera.lotr.Model.Transaction;
import com.marquesevadera.lotr.R;

import java.text.SimpleDateFormat;
import java.util.Date;


public class AdminNavProfile extends Fragment {

    private EditText editPhone , editBranch, editAddress , editName;
    private TextView staffCounter , todayCounter , codeDisplay;
    private Menu menu;
    private MenuItem edit,done , cancel;

    private Admin admin;


    private DatabaseReference adminRef;
    private String currentUserID;
    private ValueEventListener mListener;
    private Database database = new Database();
    private int todayTransactions;


    public AdminNavProfile() {
        // Required empty public constructor
    }



    private OnSelectedTodayListener mCallback;
    public interface OnSelectedTodayListener{
        public void onSelectedToday();
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = ( OnSelectedTodayListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement  OnSelectedTodayListener");
        }
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUserID = database.getUserID();
        adminRef = database.getAdmin_tbl().child(currentUserID);

        InitAdminListener();

        getTodayTransactions();

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.admin_nav_profile, container, false);
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

        getActivity().setTitle("My Profile");

        initView(view);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        edit = menu.findItem(R.id.edit_action);
        done = menu.findItem(R.id.done_action);
        cancel = menu.findItem(R.id.cancel_action);

        if(item.getItemId() == R.id.edit_action){
            editAddress.setEnabled(true);
            editName.setEnabled(true);
            editBranch.setEnabled(true);
            editPhone.setEnabled(true);
            edit.setVisible(false);
            done.setVisible(true);
            cancel.setVisible(true);
        }
        if(item.getItemId() == R.id.done_action){
            editAddress.setEnabled(false);
            editName.setEnabled(false);
            editPhone.setEnabled(false);
            editBranch.setEnabled(false);
            edit.setVisible(true);
            done.setVisible(false);
            cancel.setVisible(false);
            SaveEdit();
        }
        if(item.getItemId() == R.id.cancel_action){
            editAddress.setEnabled(false);
            editName.setEnabled(false);
            editPhone.setEnabled(false);
            editBranch.setEnabled(false);
            edit.setVisible(true);
            done.setVisible(false);
            cancel.setVisible(false);
            InitAdminListener();
        }
        return super.onOptionsItemSelected(item);
    }




    @Override
    public void onDestroy() {
        removeListener(adminRef , mListener);
        super.onDestroy();

    }


    private void initView(View v){

        codeDisplay = (TextView) v.findViewById(R.id.code_display);
        todayCounter = (TextView) v.findViewById(R.id.today_counter);
        staffCounter = (TextView) v.findViewById(R.id.staff_counter);
        editName = (EditText) v.findViewById(R.id.admin_name_holder);
        editAddress = (EditText) v.findViewById(R.id.edit_address);
        editBranch= (EditText) v.findViewById(R.id.edit_branch);
        editPhone = (EditText) v.findViewById(R.id.edit_phone);


        staffCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().beginTransaction().replace(R.id.admin_container, new AdminNavStaffs()).addToBackStack(null).commit();
            }
        });

        todayCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onSelectedToday();
            }
        });



    }


    private void ShowData(DataSnapshot dataSnapshot){
        if(dataSnapshot!=null) {
            admin = dataSnapshot.getValue(Admin.class);
        }

        codeDisplay.setText(admin.getAdmincode());
        staffCounter.setText(Integer.toString(admin.getStaffcount()));
        editName.setText(admin.getName());
        editAddress.setText(admin.getAddress());
        editBranch.setText(admin.getBranch());
        editPhone.setText(admin.getPhone());

    }



    private void SaveEdit(){
        String response = getEditTextResponse();
        if(!TextUtils.isEmpty(response)){
            Toast.makeText(getActivity(), response , Toast.LENGTH_SHORT).show();
            ShowData(null);
            return;
        }

        try{
            admin.setName(editName.getText().toString());
            admin.setAddress(editAddress.getText().toString());
            admin.setBranch(editBranch.getText().toString());
            admin.setPhone(editPhone.getText().toString());
            adminRef.setValue(admin);
            Toast.makeText(getActivity(),"Profile Updated!" , Toast.LENGTH_SHORT).show();
        }catch (Exception e){e.printStackTrace();}

    }

    private String getEditTextResponse(){
        String response = "";
        if(TextUtils.isEmpty(editName.getText().toString().trim())){
            response = "You should enter your name!";
        }else if(TextUtils.isEmpty(editBranch.getText().toString().trim())){
            response = "You should enter your branch!";
        }
        return  response;
    }



    private void removeListener(DatabaseReference dbRef , ValueEventListener listener){
        if(listener != null)
            dbRef.removeEventListener(listener);
    }

    private void getTodayTransactions(){
        SimpleDateFormat sf = new SimpleDateFormat("MM/dd/yyyy");
        final String today = sf.format(new Date());

        database.getTransaction_tbl().child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                todayTransactions = 0;
               for(DataSnapshot ds : dataSnapshot.getChildren()){
                   Transaction tr = ds.getValue(Transaction.class);
                   if(tr.getReceived_date().equals(today))
                       todayTransactions++;
               }
                todayCounter.setText(Integer.toString(todayTransactions));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void InitAdminListener(){
        removeListener(adminRef , mListener);

        mListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ShowData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        adminRef.addValueEventListener(mListener);
    }

}
