package com.marquesevadera.lotr.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.marquesevadera.lotr.Adapters.TransactionAdapter;
import com.marquesevadera.lotr.Enums.CALLER;
import com.marquesevadera.lotr.Enums.KEY;
import com.marquesevadera.lotr.Enums.Role;
import com.marquesevadera.lotr.Enums.Constraints;
import com.marquesevadera.lotr.Model.Database;
import com.marquesevadera.lotr.Model.Staff;
import com.marquesevadera.lotr.Model.Transaction;
import com.marquesevadera.lotr.Activities.NewTransaction;
import com.marquesevadera.lotr.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ASUS on 2/9/2018.
 */
public class NavTransaction extends Fragment {


    private ListView transactionListView;
    private ArrayList<Transaction> transactionList;


    private Database database = new Database();

    private String currentUser = database.getUserID();
    private DatabaseReference transactionTbl;

    private String adminRefID;
    private Staff staff;
    private Role role;

    private ValueEventListener mListener;
    private String actionBarTitle;

    private Constraints constraints; //constraint for displaying transaction ex { received , dispatched , all   etc}
    private TextView emptyTV;

    private TransactionAdapter adapter;
    private String staffID;


    private double fineRate;

    public NavTransaction() {
        // Required empty public constructor
    }



    private OnTransactionSelectedListener mCallback;

    public interface OnTransactionSelectedListener{
        public void onTransactionSelected(Transaction transaction);
    }





    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnTransactionSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnTransactionSelectedListener");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            String callerName = getArguments().getString(KEY.CALLER_NAME);
            role = (Role) getArguments().getSerializable(KEY.ROLE);
            actionBarTitle = getArguments().getString(KEY.ACTION_BAR_TITLE);
            constraints = (Constraints) getArguments().getSerializable(KEY.CONSTRAINTS);


            //PAG GALING SA TRANSACTION DETAILS,
            if(callerName.equals(CALLER.TRANSACTION_DETAILS)){
                Transaction transaction = (Transaction) getArguments().getSerializable(KEY.TRANSACTION);
                adminRefID = transaction.getAdminref();

                if(constraints == Constraints.RECEIVED)
                    staffID = transaction.getReceivedby_ref();
                else if(constraints == Constraints.DISPATCHED)
                    staffID = transaction.getSettledby_ref();

            }


            // PAG GALING SA STAFF PROFILE,
            else if(callerName.equals(CALLER.STAFF_PROFILE)){

                staff = (Staff) getArguments().getSerializable(KEY.STAFF);
                if (role == Role.STAFF){
                    adminRefID = staff.getAdminref();
                    staffID = staff.getStaffref();
                }
                else
                    adminRefID = currentUser;

             }

            // PAG NORMAL CALL LANG
            else if(callerName.equals(CALLER.ADMIN_HOME) ){
                    adminRefID = currentUser;
            }
            else if(callerName.equals(CALLER.STAFF_HOME)){
                staff = (Staff) getArguments().getSerializable(KEY.STAFF);
                adminRefID = staff.getAdminref();
            }

        }

      getFineRate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.transaction_list_layout, container, false);
            setHasOptionsMenu(true);

        initViews(v);

        return v;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(actionBarTitle == null)
            getActivity().setTitle("Transactions");
        else
            getActivity().setTitle(actionBarTitle);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.transaction_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView)menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                SearchID(newText.toUpperCase());
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(Role.ADMIN == role){
            MenuItem menuItem = menu.findItem(R.id.new_transaction_action);
            menuItem.setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.new_transaction_action){
            Intent intent = new Intent(getActivity() , NewTransaction.class);
            if(staff!=null) Log.i("Di naman ah" , "Di null");
            intent.putExtra(KEY.STAFF , staff);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDestroy() {
        if(mListener != null){
            transactionTbl.removeEventListener(mListener);
        }

        super.onDestroy();
    }



    private void initViews(final View view){

        emptyTV = (TextView) view.findViewById(R.id.empty_transaction);
        transactionTbl = database.getTransaction_tbl().child(adminRefID);
        transactionListView = (ListView) view.findViewById(R.id.transactionListView);
        transactionList = new ArrayList<>();


        ShowData(constraints);


       transactionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               Transaction transaction = transactionList.get(position);
               mCallback.onTransactionSelected(transaction);
           }
       });



    }



    private void ApplyFine(){
      final  DatabaseReference dr = database.getTransaction_tbl().child(adminRefID);
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Transaction tr = ds.getValue(Transaction.class);
                    tr.ApplyFine(fineRate);
                    dr.child(tr.getTransaction_id()).setValue(tr);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void ShowData(final Constraints cons){

        ApplyFine();

        mListener = transactionTbl.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                transactionList.clear();
                for (DataSnapshot reqSnapshot : dataSnapshot.getChildren()) {
                    Transaction tr = reqSnapshot.getValue(Transaction.class);

                    if (cons == Constraints.NONE || cons == null || cons == Constraints.LOWEST || cons == Constraints.HIGHEST) {
                        transactionList.add(tr);
                    } else if (cons == Constraints.RECEIVED) {
                        if (staffID.equals(tr.getReceivedby_ref()))
                            transactionList.add(tr);
                    } else if (cons == Constraints.DISPATCHED) {
                        if (staffID.equals(tr.getSettledby_ref()))
                            transactionList.add(tr);
                    } else if (cons == Constraints.ALL) {
                        if (staffID.equals(tr.getSettledby_ref()) || staffID.equals(tr.getReceivedby_ref()))
                            transactionList.add(tr);
                    } else if (cons == Constraints.TODAY) {
                        if (tr.getReceived_date().equals(new SimpleDateFormat("MM/dd/yyyy").format(new Date())))
                            transactionList.add(tr);
                    }
                }



                if (transactionList.isEmpty()) {
                    emptyTV.setText("We've come up empty!");
                } else {
                    emptyTV.setText("");
                }




                if (getActivity() == null)return;

                adapter = new TransactionAdapter(getActivity(), 0, transactionList);
                transactionListView.setAdapter(adapter);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }


    private void getFineRate(){
        DatabaseReference dr = database.getLaundryRate_tbl().child(adminRefID);
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                fineRate = dataSnapshot.child("fine").getValue(Double.class);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




    private void SearchID(final String searchedString){

        transactionTbl.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                transactionList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Transaction tr = snapshot.getValue(Transaction.class);
                    String transactionID = tr.getTransaction_id().toUpperCase();
                    String strName = tr.getName().toUpperCase();
                    if (transactionID.contains(searchedString)||  strName.contains(searchedString) ) {
                        transactionList.add(tr);
                    }
                }

                if (getActivity() == null) return;
                adapter = new TransactionAdapter(getActivity(), 0, transactionList);
                transactionListView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

