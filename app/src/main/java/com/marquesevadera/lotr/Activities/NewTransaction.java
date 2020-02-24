package com.marquesevadera.lotr.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ValueEventListener;
import com.marquesevadera.lotr.Enums.FabricType;
import com.marquesevadera.lotr.Enums.KEY;
import com.marquesevadera.lotr.Enums.Role;
import com.marquesevadera.lotr.Enums.WashType;
import com.marquesevadera.lotr.Model.Database;
import com.marquesevadera.lotr.Model.LaundryRate;
import com.marquesevadera.lotr.Model.Staff;
import com.marquesevadera.lotr.Model.Transaction;
import com.marquesevadera.lotr.R;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.DecimalFormat;


public class NewTransaction extends AppCompatActivity {


    private Staff staffObj;

    Spinner spinnerReg , spinnerHeavy;
    MaterialEditText edtRegular , edtHeavy , edtName;
    Button btnuSubmit;
    TextView receivedTV , dispatchedTV, transactionIdTV, regPriceTV , heavyPriceTV , totWeightTV, totPriceTV ;
    LaundryRate laundryRate;
    Transaction transaction;


    double p_reg = 0 , p_heavy = 0 , w_reg = 0 , w_heavy = 0 , totWeight , totPrice;

    String currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
    String adminRef;



    public NewTransaction() {
        // Required empty public constructor
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.staff_new_transaction);


        if(getIntent().getExtras()!= null){
            staffObj = (Staff) getIntent().getSerializableExtra(KEY.STAFF);
            adminRef = staffObj.getAdminref();
        }else{
            adminRef = currentUser;

        }

        Database db = new Database();



        initViews();

        db.getLaundryRate_tbl().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                laundryRate = dataSnapshot.child(staffObj.getAdminref()).getValue(LaundryRate.class);
                transaction = new Transaction(laundryRate.getDays());
                transactionIdTV.setText(transaction.getTransaction_id());
                receivedTV.setText(transaction.getReceived_date());
                dispatchedTV.setText(transaction.getDispatchment_date());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void initViews(){

        //Spinners
        spinnerReg = (Spinner) findViewById(R.id.spinnerReg);
        spinnerHeavy = (Spinner) findViewById(R.id.spinnerHeavy);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.wash_type, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item_down);
        spinnerReg.setAdapter(adapter);
        spinnerHeavy.setAdapter(adapter);
        AddSpinnerListener();

        //TextViews
        receivedTV = (TextView) findViewById(R.id.txtReceived);
        dispatchedTV = (TextView)findViewById(R.id.txtDispatch);
        transactionIdTV = (TextView) findViewById(R.id.txtTransactionId);
        regPriceTV = (TextView) findViewById(R.id.txtRegularPrice);
        heavyPriceTV = (TextView)findViewById(R.id.txtHeavyPrice);
        totWeightTV = (TextView) findViewById(R.id.totalWeight);
        totPriceTV = (TextView) findViewById(R.id.totalPrice);

        //Edit Texts
        edtHeavy = (MaterialEditText) findViewById(R.id.heavyFabric);
        edtRegular = (MaterialEditText)findViewById(R.id.regularFabric);
        edtName = (MaterialEditText) findViewById(R.id.customerName);
        AddTextChangedListeners();


        //Button
        btnuSubmit = (Button) findViewById(R.id.btnSubmit);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(edtName.getText())){
                    edtName.setError("Name must be filled out!");
                    return;
                }

               else if(totPrice <= 0){
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(NewTransaction.this);
                    mBuilder.setTitle("Submit Failed!");
                    mBuilder.setMessage("Transaction balance cannot be 0!");
                    mBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    mBuilder.show();
                    return;
                }else{
                    AlertSubmit();
                }

            }
        };

        btnuSubmit.setOnClickListener(listener);


    }




    private void AddTextChangedListeners(){

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
                totPrice = p_heavy + p_reg;
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
                w_heavy= parseDouble(edtHeavy);
                p_heavy= Transaction.calcPrice(laundryRate, spinnerHeavy.getSelectedItem().toString(), FabricType.HEAVY_FABRIC, w_heavy);
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
        transaction.setReceivedby(staffObj.getName());
        transaction.setReceivedby_ref(staffObj.getStaffref());

        transaction.setAdminref(adminRef);

    }

    private void AlertSubmit(){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle("Submit Transaction?");
        mBuilder.setMessage("This action will finalize the transaction.");
        mBuilder.setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();


                //Set the transaction's values
                SetTransaction();

                //Add the transaction to Transaction Database
                DatabaseReference dr = FirebaseDatabase.getInstance().getReference("Transaction").child(staffObj.getAdminref());
                dr.child(transaction.getTransaction_id()).setValue(transaction);

                //Increase Staff's Received Stats
                DatabaseReference countRef = FirebaseDatabase.getInstance().getReference("Staff").child(currentUser);
                staffObj.increaseReceived();
                countRef.setValue(staffObj);

                //Successfull prompt , go back to previous fragment
                Toast.makeText(NewTransaction.this, "Transaction: " + transaction.getTransaction_id() + " was successfully submitted!" , Toast.LENGTH_SHORT ).show();
                onBackPressed();

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



}
