package com.marquesevadera.lotr.Activities;


import android.app.AlertDialog;
import android.app.Fragment;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marquesevadera.lotr.Enums.ACTION;
import com.marquesevadera.lotr.Enums.CALLER;
import com.marquesevadera.lotr.Enums.KEY;
 import com.marquesevadera.lotr.Enums.Role;
import com.marquesevadera.lotr.Enums.Constraints;
import com.marquesevadera.lotr.Fragments.NavTransaction;
import com.marquesevadera.lotr.Fragments.Staff.StaffNavProfile;
import com.marquesevadera.lotr.Fragments.TransactionDetails;
import com.marquesevadera.lotr.Model.Admin;

import com.marquesevadera.lotr.Fragments.Admin.AdminNavNotif;
import com.marquesevadera.lotr.Fragments.Admin.AdminNavProfile;
import com.marquesevadera.lotr.Fragments.Admin.AdminNavStaffs;
import com.marquesevadera.lotr.Fragments.NavRate;
import com.marquesevadera.lotr.Model.Database;
import com.marquesevadera.lotr.Model.Staff;
import com.marquesevadera.lotr.Model.Transaction;
import com.marquesevadera.lotr.R;

public class AdminHome extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener ,
        NavTransaction.OnTransactionSelectedListener ,
        AdminNavStaffs.OnStaffSelectedListener ,
        StaffNavProfile.OnConstraintSelectedListener ,
        TransactionDetails.OnSelectedProcessedByListener,
        AdminNavProfile.OnSelectedTodayListener{


    String currentUser;
    DatabaseReference adminRef;
    TextView adminNameTV , adminCodeTV;
    View header;


    Database database = new Database();
    private Staff staff;
    private ActionBarDrawerToggle toggle;
    private ValueEventListener adminListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.admin_toolbar);
        setSupportActionBar(toolbar);

        currentUser = database.getUserID();
        adminRef = database.getAdmin_tbl();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.admin_drawer_layout);

        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
                public void onDrawerOpened(View view){
                    super.onDrawerOpened(view);
                    adminListener = adminRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            UpdateNavHeader(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
        };


        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.admin_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        header = navigationView.getHeaderView(0);
        adminNameTV = (TextView)header.findViewById(R.id.adminNameTV);
        adminCodeTV = (TextView)header.findViewById(R.id.adminCodeTV);

        NavTransaction navTransaction= new NavTransaction();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY.ROLE, Role.ADMIN);
        bundle.putSerializable(KEY.CALLER_NAME, CALLER.ADMIN_HOME);
        bundle.putSerializable(KEY.CONSTRAINTS , Constraints.NONE);
        navTransaction.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.admin_container,navTransaction).commit();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.admin_home, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_logout){
            AlertLogout(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment currentFragment =  getFragmentManager().findFragmentById(R.id.admin_container);

        int id = item.getItemId();

        if(id == R.id.admin_myProfile){
            OpenNavProfile(currentFragment);

        }else if(id == R.id.admin_staffs){
            OpenNavStaff(currentFragment);

        }else if(id == R.id.admin_transactions){
            OpenNavTransaction(currentFragment);

        }else if(id == R.id.admin_rate){
           OpenNavRate(currentFragment);
        }else if(id == R.id.admin_notif) {
            OpenNavNotif(currentFragment);
        }else if(id == R.id.admin_logout){
            AlertLogout(null);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.admin_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    @Override
    protected void onDestroy() {
        if(adminListener!=null)
            FirebaseDatabase.getInstance().getReference("Admin").removeEventListener(adminListener);
        super.onDestroy();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.admin_drawer_layout);
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
    public void onTransactionSelected(Transaction transaction) {
        TransactionDetails transactionDetails = new TransactionDetails();
        Bundle args = new Bundle();
        args.putSerializable(KEY.ROLE , Role.ADMIN);
        args.putSerializable(KEY.TRANSACTION , transaction);
        transactionDetails.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.admin_container,transactionDetails).addToBackStack(null).commit();
    }

    @Override
    public void onStaffSelected(Staff staff , ACTION action) {
        this.staff = staff;
        if(action == ACTION.SELECT)
            SelectStaff(staff);

        else
            LongClickStaff(staff);
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
            args.putSerializable(StaffNavProfile.ARG_ACTION_BAR_TITLE , staff.getName() + "'s Dispatched Transactions");
        else if(constraints == Constraints.RECEIVED)
            args.putSerializable(StaffNavProfile.ARG_ACTION_BAR_TITLE , staff.getName() + "'s Received Transactions");
        else
            args.putSerializable(StaffNavProfile.ARG_ACTION_BAR_TITLE, staff.getName() + "'s  All Transactions");

        navTransaction.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.admin_container,navTransaction).addToBackStack(null).commit();
    }

    @Override
    public void onSelectedProcessedBy(Transaction transaction , Constraints cons) {
        NavTransaction navTransaction = new NavTransaction();
        Bundle args = new Bundle();

        args.putSerializable(KEY.CONSTRAINTS, cons);
        args.putSerializable(KEY.TRANSACTION , transaction);
        args.putSerializable(KEY.ROLE, Role.ADMIN);
        args.putSerializable(KEY.CALLER_NAME , CALLER.TRANSACTION_DETAILS);

        navTransaction.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.admin_container, navTransaction).addToBackStack(null).commit();
    }

    @Override
    public void onSelectedToday() {
        NavTransaction navTransaction = new NavTransaction();
        Bundle args = new Bundle();
        args.putSerializable(KEY.CONSTRAINTS, Constraints.TODAY);
        args.putSerializable(KEY.ROLE, Role.ADMIN);
        args.putSerializable(KEY.CALLER_NAME, CALLER.ADMIN_HOME);
        navTransaction.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.admin_container, navTransaction).addToBackStack(null).commit();
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
                startActivity(new Intent(AdminHome.this, LoginActivity.class));
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



    private void UpdateNavHeader(DataSnapshot dataSnapshot){
        Admin admin = dataSnapshot.child(currentUser).getValue(Admin.class);
        adminNameTV.setText(admin.getName());
        adminCodeTV.setText(admin.getAdmincode());

    }








   private void OpenNavProfile(Fragment currentFragment){
       AdminNavProfile adminNavProfile = new AdminNavProfile();

       if(!(currentFragment instanceof  AdminNavProfile))
           getFragmentManager().beginTransaction().replace(R.id.admin_container,adminNavProfile).addToBackStack(null).commit();
   }

    private void OpenNavStaff(Fragment currentFragment){
        AdminNavStaffs adminNavStaffs = new AdminNavStaffs();

        if(!(currentFragment instanceof AdminNavStaffs))
            getFragmentManager().beginTransaction().replace(R.id.admin_container,adminNavStaffs).addToBackStack(null).commit();
    }

    private void OpenNavRate(Fragment currentFragment){
        NavRate adminNavRate = new NavRate();

        if(!(currentFragment instanceof NavRate))
            getFragmentManager().beginTransaction().replace(R.id.admin_container,adminNavRate).addToBackStack(null).commit();
    }

    private void OpenNavNotif(Fragment currentFragment){
        AdminNavNotif adminNavNotif = new AdminNavNotif();

        if(!(currentFragment instanceof  AdminNavNotif))
            getFragmentManager().beginTransaction().replace(R.id.admin_container,adminNavNotif).addToBackStack(null).commit();
    }


    private void OpenNavTransaction(Fragment currentFragment){
        NavTransaction navTransaction= new NavTransaction();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY.ROLE, Role.ADMIN);
        bundle.putSerializable(KEY.CALLER_NAME, CALLER.ADMIN_HOME);
        bundle.putSerializable(KEY.CONSTRAINTS, Constraints.NONE);
        navTransaction.setArguments(bundle);

        if(!(currentFragment instanceof  NavTransaction) || currentFragment == null)
            getFragmentManager().beginTransaction().replace(R.id.admin_container,navTransaction).addToBackStack(null).commit();
    }


    private void SelectStaff(Staff staff){
        Bundle args = new Bundle();
        StaffNavProfile staffProfile = new StaffNavProfile();
        args.putSerializable(StaffNavProfile.ARG_STAFF, staff);
        args.putSerializable(StaffNavProfile.ARG_ROLE, Role.ADMIN);
        staffProfile.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.admin_container,staffProfile).addToBackStack(null).commit();
    }

    private void LongClickStaff(final Staff staff){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle("Remove Staff");
        mBuilder.setMessage(staff.getName() + " will be removed.");
        mBuilder.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                DatabaseReference staffRef = database.getStaff_tbl().child(staff.getStaffref());
                staffRef.removeValue();
                Toast.makeText(AdminHome.this, staff.getName() + " was successfully removed from your staff lists!", Toast.LENGTH_SHORT).show();
            }
        });
        mBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();
    }



}
