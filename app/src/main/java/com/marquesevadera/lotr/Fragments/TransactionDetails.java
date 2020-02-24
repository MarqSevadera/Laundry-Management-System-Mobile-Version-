package com.marquesevadera.lotr.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marquesevadera.lotr.Enums.Constraints;
import com.marquesevadera.lotr.Enums.FabricType;
import com.marquesevadera.lotr.Enums.KEY;
import com.marquesevadera.lotr.Enums.Role;
import com.marquesevadera.lotr.Fragments.Staff.StaffNavProfile;
import com.marquesevadera.lotr.Model.Database;
import com.marquesevadera.lotr.Model.LaundryRate;
import com.marquesevadera.lotr.Model.Staff;
import com.marquesevadera.lotr.Model.Transaction;
import com.marquesevadera.lotr.R;
import com.rengwuxian.materialedittext.MaterialEditText;
;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by ASUS on 2/23/2018.
 */
public class TransactionDetails extends Fragment {



    //Views
    Spinner spinnerReg , spinnerHeavy;
    MaterialEditText edtRegular , edtHeavy , edtName;
    Button btnSettle;
    TextView receivedTV , dispatchedTV, transactionIdTV, regPriceTV , heavyPriceTV , totWeightTV, totPriceTV ,receivedByTV , settledByTV , dateTV , dateSettledTV, totalDaysTV , fineTV;

    //Models
    private Transaction transaction;
    private Staff staff;
    private Database database = new Database();

    //Flag
    private boolean readyToSettle = false;


    double p_reg = 0 , p_heavy = 0 , w_reg = 0 , w_heavy = 0 , totWeight , totPrice , fine;


    private Menu menu;
    private MenuItem edit , done , cancel;
    private Role role;


    private LaundryRate laundryRate;



    private OnSelectedProcessedByListener mCallback;

    public interface OnSelectedProcessedByListener{
        public void onSelectedProcessedBy(Transaction transaction, Constraints cons);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        try {
            mCallback = (OnSelectedProcessedByListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSelectedProcessedByListener");
        }
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments() !=  null){
            role = (Role) getArguments().getSerializable(KEY.ROLE);
            transaction = (Transaction) getArguments().getSerializable(KEY.TRANSACTION);
            staff = (Staff) getArguments().getSerializable(KEY.STAFF);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.transaction_description, container, false);

        if(!database.getUser().isEmailVerified() && !transaction.isSettled()){ //if it is staff and transaction is not yet settled transaction can be edited
            setHasOptionsMenu(true);

            database.getLaundryRate_tbl().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    laundryRate = dataSnapshot.child(staff.getAdminref()).getValue(LaundryRate.class);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }else{
            setHasOptionsMenu(false);
        }

        initView(v);
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
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("Transaction Details");

        //Button
        setSpinners();
        setEditTexts();
        setTextViews();
        setButton();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        edit = menu.findItem(R.id.edit_action);
        done = menu.findItem(R.id.done_action);
        cancel = menu.findItem(R.id.cancel_action);

        if(item.getItemId() == R.id.edit_action){
            edtName.setEnabled(true);
            edtHeavy.setEnabled(true);
            edtRegular.setEnabled(true);

            spinnerHeavy.setEnabled(true);
            spinnerReg.setEnabled(true);


            edit.setVisible(false);
            done.setVisible(true);
            cancel.setVisible(true);

            AddTextChangedListener();
            AddSpinnerListener();
        }
        if(item.getItemId() == R.id.done_action){

            if(totPrice <= 0){
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                mBuilder.setTitle("Update Failed!");
                mBuilder.setMessage("Transaction balance cannot be 0!");
                mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        setSpinners();
                        setEditTexts();
                        setTextViews();
                        setButton();
                        edtName.setEnabled(false);
                        edtHeavy.setEnabled(false);
                        edtRegular.setEnabled(false);

                        spinnerHeavy.setEnabled(false);
                        spinnerReg.setEnabled(false);

                        edit.setVisible(true);
                        done.setVisible(false);
                        cancel.setVisible(false);

                    }
                });
                mBuilder.show();
            }else {
                AlertUpdate();
                edit.setVisible(true);
                done.setVisible(false);
                cancel.setVisible(false);

                edtName.setEnabled(false);
                edtHeavy.setEnabled(false);
                edtRegular.setEnabled(false);

                spinnerHeavy.setEnabled(false);
                spinnerReg.setEnabled(false);
            }




        }
        if(item.getItemId() == R.id.cancel_action){

            setSpinners();
            setEditTexts();
            setTextViews();
            setButton();

            edit.setVisible(true);
            done.setVisible(false);
            cancel.setVisible(false);

            edtName.setEnabled(false);
            edtHeavy.setEnabled(false);
            edtRegular.setEnabled(false);

            spinnerHeavy.setEnabled(false);
            spinnerReg.setEnabled(false);

        }

        return super.onOptionsItemSelected(item);
    }


    private void initView(View v){

        //Spinners
        spinnerReg = (Spinner) v.findViewById(R.id.spinnerRegular);
        spinnerHeavy = (Spinner) v.findViewById(R.id.spinnerHeavy);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.wash_type, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item_down);
        spinnerReg.setAdapter(adapter);
        spinnerHeavy.setAdapter(adapter);


        //TextViews
        receivedTV = (TextView) v.findViewById(R.id.txtReceived);
        dispatchedTV = (TextView)v.findViewById(R.id.txtDispatch);
        receivedByTV = (TextView) v.findViewById(R.id.txtReceivedBy);
        settledByTV = (TextView)v.findViewById(R.id.txtSettledBy);
        transactionIdTV = (TextView) v.findViewById(R.id.txtTransactionId);
        regPriceTV = (TextView) v.findViewById(R.id.txtRegularPrice);
        totalDaysTV = (TextView) v.findViewById(R.id.totalDays);
        fineTV = (TextView) v.findViewById(R.id.lateFine);

        heavyPriceTV = (TextView)v.findViewById(R.id.txtHeavyPrice);
        totWeightTV = (TextView) v.findViewById(R.id.totalWeight);
        totPriceTV = (TextView) v.findViewById(R.id.totalPrice);

        dateTV = (TextView) v.findViewById(R.id.txtdate);
        dateSettledTV = (TextView) v.findViewById(R.id.txtDateSettled);

        //Edit Texts
        edtHeavy = (MaterialEditText) v.findViewById(R.id.heavyFabric);
        edtRegular = (MaterialEditText)v.findViewById(R.id.regularFabric);
        edtName = (MaterialEditText) v.findViewById(R.id.customerName);


        btnSettle = (Button) v.findViewById(R.id.btnSettle);
        AddButtonListener();
    }

    private void setButton(){

        if(role == Role.STAFF  ){
            PrepareButton();
        }else{
            btnSettle.setVisibility(View.INVISIBLE);
        }

    }

    private void PrepareButton(){

        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
        Date today = new Date();
        Date dispatchmentDate = null;

        try{
            dispatchmentDate = df.parse(transaction.getDispatchment_date());
        }catch (Exception ex){ex.printStackTrace();}

        if(transaction.isSettled() ){
            btnSettle.setEnabled(false);
            btnSettle.setVisibility(View.INVISIBLE);
            dateSettledTV.setVisibility(View.VISIBLE);
            dateTV.setVisibility(View.VISIBLE);
        } else {
            if(today.after(dispatchmentDate) || today.equals(dispatchmentDate)){ //if it is to be settled in the date of dispatchment or after deadline
                btnSettle.setEnabled(true);
                btnSettle.setVisibility(View.VISIBLE);
                readyToSettle = true;
            }else {
                btnSettle.setEnabled(true);
                btnSettle.setText("CANCEL");
                btnSettle.setVisibility(View.VISIBLE);
                readyToSettle = false;
            }

        }
    }

    private void AddButtonListener(){
        btnSettle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btnSettle) {

                    if (readyToSettle) {
                        AlertSettle();
                    } else {
                        AlertCancel();
                    }
                }
            }
        });
    }

    private void AlertSettle(){
       final DatabaseReference transactionRef = database.getTransaction_tbl().child(transaction.getAdminref()).child(transaction.getTransaction_id());
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setTitle("Confirm Transaction Settlement");
        mBuilder.setMessage("This action will settle the current transaction.");
        mBuilder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                transaction.SettleTransaction(transactionRef, staff.getName(), staff.getStaffref());

                DatabaseReference staffReff = FirebaseDatabase.getInstance().getReference("Staff").child(staff.getStaffref());
                staff.increaseSettled();
                staffReff.setValue(staff);
                Toast.makeText(getActivity(), "Transaction: "  + transaction.getTransaction_id() + " was successfully settled!", Toast.LENGTH_SHORT).show();
                btnSettle.setVisibility(View.INVISIBLE);
                btnSettle.setEnabled(false);
                dateSettledTV.setText(transaction.getSettled_date());
                dateSettledTV.setVisibility(View.VISIBLE);
                dateTV.setVisibility(View.VISIBLE);


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

    private void AlertCancel(){
        final DatabaseReference transactionRef = database.getTransaction_tbl().child(transaction.getAdminref()).child(transaction.getTransaction_id());

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setTitle("Confirm Transaction Cancellation");
        mBuilder.setMessage("This action will cancel the current transaction.");
        mBuilder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //Cancel Transaction
                DecreaseReceived(transaction.getReceivedby_ref());
                transaction.CancelTransaction(transactionRef);
                Toast.makeText(getActivity() , "Transaction: " + transaction.getTransaction_id() + " was successfully cancelled!" , Toast.LENGTH_SHORT ).show();

                //Decrease Received

                getActivity().onBackPressed();
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

    private void setSpinners(){

        spinnerReg.setEnabled(false);
        spinnerHeavy.setEnabled(false);

        if(transaction.getRegular_washtype().equals("Machine Washed"))
            spinnerReg.setSelection(0);
        else
            spinnerReg.setSelection(1);

        if(transaction.getHeavy_washtype().equals("Machine Washed"))
            spinnerHeavy.setSelection(0);
        else
            spinnerHeavy.setSelection(1);
    }

    private void setTextViews(){
        DecimalFormat df = new DecimalFormat("#.##");


        p_reg = transaction.getP_regular();
        p_heavy = transaction.getP_heavy();
        w_reg = transaction.getW_regular();
        w_heavy = transaction.getW_heavy();
        totPrice = transaction.getTotprice() + transaction.getFine();
        totWeight = transaction.getTotweight();



        Date today = new Date();
        Date dispatch = parseDate(transaction.getDispatchment_date());
        Calendar cal1 = new GregorianCalendar();
        Calendar cal2 = new GregorianCalendar();
        cal1.setTime(today);
        cal2.setTime(dispatch);



        receivedTV.setText(transaction.getReceived_date());
        receivedByTV.setText(transaction.getReceivedby());
        settledByTV.setText(transaction.getSettledby());
        dispatchedTV.setText(transaction.getDispatchment_date());
        transactionIdTV.setText(transaction.getTransaction_id());

        regPriceTV.setText(df.format(p_reg));
        heavyPriceTV.setText(df.format(p_heavy));
        totWeightTV.setText(df.format(totWeight));
        totPriceTV.setText(df.format(totPrice));


        if(today.after(dispatch)){
            int daysBetween = daysBetween(cal2.getTime() , cal1.getTime());
            fine = transaction.getFine();
            totalDaysTV.setText(Integer.toString(daysBetween));
            fineTV.setText(df.format(fine));
        }

        dateSettledTV.setText(transaction.getSettled_date());

        receivedByTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onSelectedProcessedBy(transaction , Constraints.RECEIVED);

            }
        });


        if(settledByTV.getText().equals(""))return;

        settledByTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onSelectedProcessedBy(transaction , Constraints.DISPATCHED);
            }
        });

    }




    private void setEditTexts(){
        edtName.setText(transaction.getName());
        edtRegular.setText(Double.toString(transaction.getW_regular()));
        edtHeavy.setText(Double.toString(transaction.getW_heavy()));


    }

    private void AddSpinnerListener(){

        spinnerReg.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                w_reg = parseDouble(edtRegular);
                p_reg = Transaction.calcPrice(laundryRate, spinnerReg.getSelectedItem().toString(), FabricType.REGULAR_FABRIC, w_reg);
                DecimalFormat df = new DecimalFormat("#.##");
                regPriceTV.setText(df.format(p_reg));
                totPrice = p_heavy + p_reg;
                totWeight = w_heavy + w_reg;
                totWeightTV.setText(df.format(totWeight));
                totPriceTV.setText(df.format(totPrice));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerHeavy.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                w_heavy = parseDouble(edtHeavy);
                p_heavy = Transaction.calcPrice(laundryRate, spinnerHeavy.getSelectedItem().toString(), FabricType.HEAVY_FABRIC, w_heavy);
                DecimalFormat df = new DecimalFormat("#.##");
                heavyPriceTV.setText(df.format(p_heavy));
                totPrice = p_heavy + p_reg;
                totWeight = w_heavy + w_reg;
                totWeightTV.setText(df.format(totWeight));
                totPriceTV.setText(df.format(totPrice));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void AddTextChangedListener(){

        edtRegular.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                w_reg = parseDouble(edtRegular);
                p_reg = Transaction.calcPrice(laundryRate, spinnerReg.getSelectedItem().toString(), FabricType.REGULAR_FABRIC, w_reg);
                DecimalFormat df = new DecimalFormat("#.##");
                regPriceTV.setText(df.format(p_reg));
                totPrice = p_heavy + p_reg + fine;
                totWeight = w_heavy + w_reg;
                totWeightTV.setText(df.format(totWeight));
                totPriceTV.setText(df.format(totPrice));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        edtHeavy.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                w_heavy = parseDouble(edtHeavy);
                p_heavy = Transaction.calcPrice(laundryRate, spinnerHeavy.getSelectedItem().toString(), FabricType.HEAVY_FABRIC, w_heavy);
                DecimalFormat df = new DecimalFormat("#.##");
                heavyPriceTV.setText(df.format(p_heavy));
                totPrice = p_heavy + p_reg;
                totWeight = w_heavy + w_reg;
                totWeightTV.setText(df.format(totWeight));
                totPriceTV.setText(df.format(totPrice));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private double parseDouble(MaterialEditText txtSrc){

        double result = 0;

        if(!txtSrc.getText().equals("")){
            try{
                result = Double.parseDouble(txtSrc.getText().toString());
                return result;
            }catch(NumberFormatException ex){
                return  0;
            }
        }else{
            return result;
        }



    }

    private void AlertUpdate(){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setTitle("Update Transaction?");
        mBuilder.setMessage("This action will update the transaction.");
        mBuilder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //Set the transaction's values
                SetTransaction();

                //Add the transaction to Transaction Database
                DatabaseReference dr = FirebaseDatabase.getInstance().getReference("Transaction").child(staff.getAdminref());
                dr.child(transaction.getTransaction_id()).setValue(transaction);

                //Successfull prompt , go back to previous fragment
                Toast.makeText(getActivity(), "Transaction: " + transaction.getTransaction_id() + " was successfully updated!" , Toast.LENGTH_SHORT ).show();
                getActivity().onBackPressed();

            }
        });

        mBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setSpinners();
                setEditTexts();
                setTextViews();
                setButton();

                edtName.setEnabled(false);
                edtHeavy.setEnabled(false);
                edtRegular.setEnabled(false);

                spinnerHeavy.setEnabled(false);
                spinnerReg.setEnabled(false);
            }
        });

        AlertDialog alertDialog = mBuilder.create();
        alertDialog.show();


    }


    private void SetTransaction(){

        //customer name
        transaction.setName(edtName.getText().toString());

        //Regular Fabric
        transaction.setW_regular(w_reg);
        transaction.setP_regular(p_reg);
        transaction.setRegular_washtype(spinnerReg.getSelectedItem().toString());

        //Heavy Fabric
        transaction.setW_heavy(w_heavy);
        transaction.setP_heavy(p_heavy);
        transaction.setHeavy_washtype(spinnerHeavy.getSelectedItem().toString());


        //Totals
        transaction.setTotprice(totPrice);
        transaction.setTotweight(totWeight);

        //Received By
        transaction.setReceivedby(staff.getName());
        transaction.setReceivedby_ref(staff.getStaffref());

        transaction.setAdminref(staff.getAdminref());

    }

    private void DecreaseReceived(final String staffRef){

        DatabaseReference dr = database.getStaff_tbl();
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Staff staff = dataSnapshot.child(staffRef).getValue(Staff.class);
                staff.decreaseReceived();
                DatabaseReference databaseReference = database.getStaff_tbl().child(staffRef);
                databaseReference.setValue(staff);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private Date parseDate(String strDate){
        SimpleDateFormat sf = new SimpleDateFormat("MM/dd/yyyy");
        Date date = null;
        try {
            date =  sf.parse(strDate);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }


    private int daysBetween(Date d1, Date d2) {
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }



}
