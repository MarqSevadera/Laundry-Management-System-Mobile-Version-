package com.marquesevadera.lotr.Activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marquesevadera.lotr.Enums.CALLER;
import com.marquesevadera.lotr.Enums.KEY;
import com.marquesevadera.lotr.Enums.Role;
import com.marquesevadera.lotr.Enums.Constraints;
import com.marquesevadera.lotr.Fragments.TransactionDetails;
import com.marquesevadera.lotr.Model.Staff;

import com.marquesevadera.lotr.Fragments.NavRate;
import com.marquesevadera.lotr.Fragments.Staff.StaffNavProfile;
import com.marquesevadera.lotr.Fragments.NavTransaction;
import com.marquesevadera.lotr.Model.Transaction;
import com.marquesevadera.lotr.R;

public class StaffHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener ,
        NavTransaction.OnTransactionSelectedListener ,
        StaffNavProfile.OnConstraintSelectedListener ,
        TransactionDetails.OnSelectedProcessedByListener{





    String currentUser;
    FirebaseDatabase database;
    DatabaseReference staffRef;
    TextView staffNameTV;
    View header;

    private ActionBarDrawerToggle toggle;
    private ValueEventListener staffListener;

    private Staff staff;

    String adminRef;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        if(getIntent().getExtras()!=null){
            staff =  (Staff) getIntent().getSerializableExtra(KEY.STAFF);
            adminRef = staff.getAdminref();
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        staffRef = database.getReference("Staff");


        initDrawer();



        FragmentManager fm = getFragmentManager();
        Fragment fragment = new NavTransaction();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY.CALLER_NAME , CALLER.STAFF_HOME);
        bundle.putSerializable(KEY.STAFF,staff);
        bundle.putSerializable(KEY.ROLE, Role.STAFF);
        fragment.setArguments(bundle);
        fm.beginTransaction().replace(R.id.staff_container, fragment).commit();
    }






    private void initDrawer(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.staff_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.staff_drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            public void onDrawerOpened(View view){
                super.onDrawerOpened(view);
                staffListener = staffRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        staff = dataSnapshot.child(currentUser).getValue(Staff.class);
                        staffNameTV.setText(staff.getName());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.staff_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        header = navigationView.getHeaderView(0);
        staffNameTV = (TextView)header.findViewById(R.id.staffNameTV);
    }




    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.staff_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            super.onBackPressed();
        }
        else {
            int backStackEntryCount = getFragmentManager().getBackStackEntryCount();
            if(backStackEntryCount == 0)
                AlertLogout("Exit");
            else
                super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.staff_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_logout){
            AlertLogout(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.staff_container);

        if(id == R.id.staff_profile) {

          OpenNavProfile(fragment);

        }else if(id == R.id.staff_transaction){

            OpenNavTransaction(fragment);

        }else if(id == R.id.staff_rate){

            OpenNavRate(fragment);

        }
        else if(id == R.id.staff_logout){

            AlertLogout(null);
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.staff_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void AlertLogout(String message){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        if(message == null) message = "Logout";

        mBuilder.setTitle(message);
        mBuilder.setMessage("Are you sure you want to " + message + "?");
        mBuilder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(StaffHome.this, LoginActivity.class));
                finish();


            }
        });
        mBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override

            public void onClick(DialogInterface dialog, int which) {
                //Firebase Ignore shit here
                dialog.dismiss();

            }
        });

        AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        if(staffListener!=null)
            FirebaseDatabase.getInstance().getReference("Staff").removeEventListener(staffListener);
        super.onDestroy();

    }


    @Override
    public void onTransactionSelected(Transaction transaction) {

        TransactionDetails transactionDetails = new TransactionDetails();
        Bundle args = new Bundle();
        args.putSerializable(KEY.TRANSACTION , transaction);
        args.putSerializable(KEY.STAFF , staff);
        args.putSerializable(KEY.ROLE , Role.STAFF);
        transactionDetails.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.staff_container,transactionDetails).addToBackStack(null).commit();

    }


    @Override
    public void onConstraintSelected(Constraints constraints) {
        NavTransaction navTransaction = new NavTransaction();
        Bundle args = new Bundle();

        args.putSerializable(KEY.STAFF , staff);
        args.putSerializable(KEY.CONSTRAINTS, constraints);
        args.putSerializable(KEY.ROLE , Role.STAFF);
        args.putSerializable(KEY.CALLER_NAME  , CALLER.STAFF_PROFILE);

        if(constraints == Constraints.DISPATCHED)
            args.putSerializable(KEY.ACTION_BAR_TITLE ,"Dispatched Transactions");
        else if(constraints == Constraints.RECEIVED)
            args.putSerializable(KEY.ACTION_BAR_TITLE ,"Received Transactions");
        else
            args.putSerializable(KEY.ACTION_BAR_TITLE,"All Transactions");

        navTransaction.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.staff_container,navTransaction).addToBackStack(null).commit();
    }


    @Override
    public void onSelectedProcessedBy(Transaction transaction, Constraints cons) {
        NavTransaction navTransaction = new NavTransaction();
        Bundle args = new Bundle();

        args.putSerializable(KEY.TRANSACTION, transaction);
        args.putSerializable(KEY.CONSTRAINTS, cons);
        args.putSerializable(KEY.CALLER_NAME, CALLER.TRANSACTION_DETAILS);
        args.putSerializable(KEY.ROLE, Role.STAFF);
        navTransaction.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.staff_container,navTransaction).addToBackStack(null).commit();
    }



    private void OpenNavTransaction(Fragment currentFragment){
        NavTransaction navTransaction = new NavTransaction();

        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY.CALLER_NAME , CALLER.STAFF_HOME);
        bundle.putSerializable(KEY.STAFF,staff);
        bundle.putSerializable(KEY.ROLE, Role.STAFF);

        navTransaction.setArguments(bundle);


        if(!(currentFragment instanceof  NavTransaction)){
            getFragmentManager().beginTransaction().replace(R.id.staff_container, navTransaction).addToBackStack(null).commit();
        }

    }



    private void OpenNavRate(Fragment currentFragment){
        NavRate navRate = new NavRate();

        if(!(currentFragment instanceof NavRate)){
            getFragmentManager().beginTransaction().replace(R.id.staff_container , navRate).addToBackStack(null).commit();
        }
    }


    private void OpenNavProfile(Fragment currentFragment) {
        StaffNavProfile staffNavProfile = new StaffNavProfile();
        if (!(currentFragment instanceof StaffNavProfile)) {
            getFragmentManager().beginTransaction().replace(R.id.staff_container, staffNavProfile).addToBackStack(null).commit();
        }
    }
}


