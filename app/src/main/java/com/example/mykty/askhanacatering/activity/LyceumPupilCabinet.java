package com.example.mykty.askhanacatering.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.database.StoreDatabase;
import com.example.mykty.askhanacatering.fragments.eaters_fragments.BreakfastFragment;
import com.example.mykty.askhanacatering.fragments.eaters_fragments.DinnerFragment;
import com.example.mykty.askhanacatering.fragments.eaters_fragments.LunchFragment;
import com.example.mykty.askhanacatering.module.Personnel;
import com.example.mykty.askhanacatering.module.Student;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_INFO;
import static com.example.mykty.askhanacatering.database.StoreDatabase.TABLE_COLLEGE_STUDENTS;
import static com.squareup.timessquare.CalendarPickerView.SelectionMode.MULTIPLE;

public class LyceumPupilCabinet extends AppCompatActivity implements View.OnClickListener {

    ImageView teacherPhoto;
    TextView teacherInfo;
    FloatingActionButton fab;
    Dialog foodBuyDialog, calendarDetailDialog, summaryDialog;
    CalendarPickerView calendar, calendarDetail;
    RadioButton checkboxBreakfast, checkboxLunch, checkboxDinner;
    Date today;
    List<Date> dates, bDates, lDates, dDates;
    Student student;
    String sName;
    Button btnBacket, btnCalc, btnClearDates, btnBack;
    boolean breakfastChecked = false, lunchChecked = false, dinnerChecked = false;
    ImageView breakfastDetail, lunchDetail, dinnerDetail;
    TextView foodTitle, oneFoodPrice, allRes;
    String bTitle, lTitle, dTitle;
    int bPrice = 550, lPrice = 750, dPrice = 700;
    BreakfastFragment breakfastFragment;
    LunchFragment lunchFragment;
    TextView tvBPrice, tvLPrice, tvDPrice, sumPrice;
    Button btnPay;
    DatabaseReference mDatabaseRef, foodDatabaseRef, studentRef ;
    DateFormat dateF;
    String idNumber, imgUrl;
    int bRes = 0, lRes = 0, dRes = 0;
    String type;
    int selectedFragment = 0;
    SQLiteDatabase sqdb;
    StoreDatabase storeDb;
    String sCardNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_cab);
        setupViews();
        changeFragment(breakfastFragment);
    }

    public void setupViews() {
        teacherPhoto = findViewById(R.id.teacherPhoto);
        teacherInfo = findViewById(R.id.teacherInfo);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

        bTitle = getResources().getString(R.string.oneB);
        lTitle = getResources().getString(R.string.oneL);
        dTitle = getResources().getString(R.string.oneD);
        breakfastFragment = new BreakfastFragment();
        lunchFragment = new LunchFragment();

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        student = (Student) bundle.getSerializable("sClass");
        sName = student.getName();
        imgUrl = student.getPhoto();
        idNumber = student.getId_number();
        sCardNumber = student.getCard_number();

        type = bundle.getString("type");

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        studentRef = FirebaseDatabase.getInstance().getReference();
        foodDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef = mDatabaseRef.child("payed").child(type).child(idNumber);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();

        changeUserDesc();
        createDialogs();
    }

    public void changeUserDesc(){
        Glide.with(this)
                .load(imgUrl)
                .placeholder(R.drawable.s_icon)
                .into(teacherPhoto);

        teacherInfo.setText(sName);
    }

    public void createDialogs() {
        foodBuyDialog = new Dialog(this);
        foodBuyDialog.setContentView(R.layout.dialog_buy_food2);

        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        today = new Date();
        today.getTime();

        calendar = foodBuyDialog.findViewById(R.id.calendar_view);
        calendar.init(today, nextYear.getTime()).inMode(MULTIPLE);

        checkboxBreakfast = foodBuyDialog.findViewById(R.id.checkboxBreakfast);
        checkboxLunch = foodBuyDialog.findViewById(R.id.checkboxLunch);
        checkboxDinner = foodBuyDialog.findViewById(R.id.checkboxDinner);

        breakfastDetail = foodBuyDialog.findViewById(R.id.breakfastDetail);
        lunchDetail = foodBuyDialog.findViewById(R.id.lunchDetail);
        dinnerDetail = foodBuyDialog.findViewById(R.id.dinnerDetail);

        btnBacket = foodBuyDialog.findViewById(R.id.btnBacket);
        btnCalc = foodBuyDialog.findViewById(R.id.btnCalc);

        btnBacket.setOnClickListener(this);
        btnCalc.setOnClickListener(this);
        breakfastDetail.setOnClickListener(this);
        lunchDetail.setOnClickListener(this);
        dinnerDetail.setOnClickListener(this);

        calendarDetailDialog = new Dialog(this);
        calendarDetailDialog.setContentView(R.layout.dialog_calendar);
        calendarDetail = calendarDetailDialog.findViewById(R.id.calendar_view_detail);
        calendarDetail.init(today, nextYear.getTime());

        foodTitle = calendarDetailDialog.findViewById(R.id.foodTitle);
        oneFoodPrice = calendarDetailDialog.findViewById(R.id.oneFoodPrice);
        allRes = calendarDetailDialog.findViewById(R.id.allRes);
        btnClearDates = calendarDetailDialog.findViewById(R.id.btnClearDates);
        btnBack = calendarDetailDialog.findViewById(R.id.btnBack);

        btnClearDates.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        summaryDialog  = new Dialog(this);
        summaryDialog.setContentView(R.layout.dialog_summary);
        summaryDialog.setTitle(getResources().getString(R.string.payment));

        tvBPrice = summaryDialog.findViewById(R.id.bPrice);
        tvLPrice = summaryDialog.findViewById(R.id.lPrice);
        tvDPrice = summaryDialog.findViewById(R.id.dPrice);
        sumPrice = summaryDialog.findViewById(R.id.sumPrice);

        btnPay = summaryDialog.findViewById(R.id.btnPay);
        btnPay.setOnClickListener(this);

        checkboxBreakfast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkboxBreakfast.setTextSize(14.0f);
                checkboxLunch.setTextSize(14.0f);
                checkboxDinner.setTextSize(14.0f);

                if(isChecked){
                    checkboxBreakfast.setTextSize(20.0f);
                }
            }
        });
        checkboxLunch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkboxBreakfast.setTextSize(14.0f);
                checkboxLunch.setTextSize(14.0f);
                checkboxDinner.setTextSize(14.0f);

                if(isChecked){
                    checkboxLunch.setTextSize(20.0f);
                }
            }
        });
        checkboxDinner.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                checkboxBreakfast.setTextSize(14.0f);
                checkboxLunch.setTextSize(14.0f);
                checkboxDinner.setTextSize(14.0f);

                if(isChecked){
                    checkboxDinner.setTextSize(20.0f);
                }
            }
        });

    }

    public String getIdNumber(){
        return idNumber;
    }

    public String getType(){
        return type;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if(selectedFragment == 0) checkboxBreakfast.setChecked(true);
                else if(selectedFragment == 1) checkboxLunch.setChecked(true);
                else if(selectedFragment == 2) checkboxDinner.setChecked(true);

                foodBuyDialog.setTitle(sName);
                foodBuyDialog.show();

                break;

            case R.id.breakfastDetail:
                calendarDetailDialog.setTitle(getResources().getString(R.string.title_breakfast));
                calendarDetail.clearHighlightedDates();
                calendarDetail.highlightDates(bDates);
                calendarDetail.scrollToDate(bDates.get(0));

                foodTitle.setText(bTitle);
                oneFoodPrice.setText(bPrice+" тенге");

                allRes.setText(bDates.size()+" күн * "+bPrice+" = "+bRes+ " тенге");
                calendarDetailDialog.show();


                break;

            case R.id.lunchDetail:
                calendarDetailDialog.setTitle(getResources().getString(R.string.title_lunch));
                calendarDetail.clearHighlightedDates();
                calendarDetail.highlightDates(lDates);
                calendarDetail.scrollToDate(lDates.get(0));
                foodTitle.setText(lTitle);
                oneFoodPrice.setText(lPrice+" тенге");

                allRes.setText(lDates.size()+" күн * "+lPrice+" = "+lRes+ " тенге");

                calendarDetailDialog.show();

                break;

            case R.id.dinnerDetail:
                calendarDetailDialog.setTitle(getResources().getString(R.string.title_dinner));
                calendarDetail.clearHighlightedDates();
                calendarDetail.highlightDates(dDates);
                calendarDetail.scrollToDate(dDates.get(0));

                foodTitle.setText(dTitle);
                oneFoodPrice.setText(dPrice+" тенге");

                allRes.setText(dDates.size()+" күн * "+dPrice+" = "+dRes+ " тенге");

                calendarDetailDialog.show();

                break;
            case R.id.btnBacket:
                if (calendar.getSelectedDates().size() != 0) {
                    if (checkboxBreakfast.isChecked()) {
                        checkboxBreakfast.setTextColor(Color.GREEN);
                        bDates = calendar.getSelectedDates();
                        breakfastChecked = true;
                        breakfastDetail.setVisibility(View.VISIBLE);

                        bRes = bDates.size() * bPrice;
                    }

                    else if (checkboxLunch.isChecked()) {
                        checkboxLunch.setTextColor(Color.GREEN);
                        lDates = calendar.getSelectedDates();
                        lunchChecked = true;
                        lunchDetail.setVisibility(View.VISIBLE);

                        lRes = lDates.size() * lPrice;
                    }

                    else if (checkboxDinner.isChecked()) {
                        checkboxDinner.setTextColor(Color.GREEN);
                        dDates = calendar.getSelectedDates();
                        dinnerChecked = true;
                        dinnerDetail.setVisibility(View.VISIBLE);

                        dRes = dDates.size() * dPrice;
                    }
                    else{
                        Toast.makeText(this, getResources().getString(R.string.foodSelectMistake), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getResources().getString(R.string.daySelectMistake), Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.btnCalc:

                tvBPrice.setText(bRes+" тенге");
                tvLPrice.setText(lRes+" тенге");
                tvDPrice.setText(dRes+" тенге");
                sumPrice.setText((bRes+lRes+dRes)+" тенге");
                summaryDialog.show();
                break;
            case R.id.btnPay:

                dateF = new SimpleDateFormat("dd_MM_yyyy");
                if (bDates!=null && bDates.size() !=0 ){
                    for(Date date: bDates){
                        String cDate = dateF.format(date);

                        mDatabaseRef.child("breakfast").child(cDate).setValue(1);
                        foodDatabaseRef.child("f_time").child("breakfast").child(type).child(cDate).child(idNumber).push().setValue(1);

                        Log.i("cal", "Breakfast: "+cDate);
                    }
                }

                if (lDates!=null &&  lDates.size() !=0 ){
                    for(Date date: lDates){
                        String cDate = dateF.format(date);
                        mDatabaseRef.child("lunch").child(cDate).setValue(1);
                        foodDatabaseRef.child("f_time").child("lunch").child(type).child(cDate).child(idNumber).push().setValue(1);

                        Log.i("cal", "Lunch: "+cDate);
                    }
                }

                if (dDates!=null &&  dDates.size() !=0 ){
                    for(Date date: dDates){
                        String cDate = dateF.format(date);
                        mDatabaseRef.child("dinner").child(cDate).setValue(1);
                        foodDatabaseRef.child("f_time").child("dinner").child(type).child(cDate).child(idNumber).push().setValue(1);
                    }
                }

                summaryDialog.dismiss();
                foodBuyDialog.dismiss();

                break;

            case R.id.btnClearDates:
                String sTitle = foodTitle.getText().toString();

                if(sTitle.equals(bTitle)){
                    bDates.clear();
                    breakfastChecked = false;
                    breakfastDetail.setVisibility(View.INVISIBLE);
                    checkboxBreakfast.setTextColor(Color.BLACK);
                    bRes = 0;

                }else if(sTitle.equals(lTitle)){
                    lDates.clear();
                    lunchChecked = false;
                    lunchDetail.setVisibility(View.INVISIBLE);
                    checkboxLunch.setTextColor(Color.BLACK);
                    lRes = 0;

                }else if(sTitle.equals(dTitle)){
                    dDates.clear();
                    dinnerChecked = false;
                    dinnerDetail.setVisibility(View.INVISIBLE);
                    checkboxDinner.setTextColor(Color.BLACK);
                    dRes = 0;

                }
                calendarDetailDialog.dismiss();

                break;
            case R.id.btnBack:
                calendarDetailDialog.dismiss();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu2, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu1) {//Өзгерту
            showDialog(sName, student.getId_number());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    TextView oIdNumber;

    public void showDialog(final String tName, final String id_number) {

        final Dialog alert = new Dialog(this, R.style.AlertDialogTheme);
        alert.setContentView(R.layout.dialog_edit);

        final TextView tvName = alert.findViewById(R.id.textViewName);
        oIdNumber = alert.findViewById(R.id.oldIdNUmber);
        final EditText nIdNumber = alert.findViewById(R.id.newIdNUmber);

        Button ok = alert.findViewById(R.id.btnOk);
        Button cancel = alert.findViewById(R.id.btnCancel);
        Button del = alert.findViewById(R.id.btnDel);

        tvName.setText(tName);
        oIdNumber.setText("CARD Number: " + sCardNumber);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnOk:
                        if (isNetworkAvailable(LyceumPupilCabinet.this)) {
                            updateCardNumber(nIdNumber.getText().toString(), id_number);
                            Toast.makeText(LyceumPupilCabinet.this, tName + " CARD Number өзгерді", Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case R.id.btnDel:
                        if (isNetworkAvailable(LyceumPupilCabinet.this)) {
//                            deleteStudent(tName);
                        }

                        break;


                    case R.id.btnCancel:

                        nIdNumber.setText("");
                        break;
                }

                alert.dismiss();
            }
        };

        ok.setOnClickListener(listener);
        cancel.setOnClickListener(listener);
        del.setOnClickListener(listener);

        alert.show();
    }

    public void updateCardNumber(final String cardNumber, String id_number) {

//        ContentValues versionValues = new ContentValues();
//        versionValues.put(COLUMN_CARD_NUMBER, cardNumber.toLowerCase());
//        sqdb.update(TABLE_COLLEGE_STUDENTS, versionValues, COLUMN_ID_NUMBER + "='" + id_number + "'", null);

        oIdNumber.setText("CARD Number: " + sCardNumber);
        sCardNumber = cardNumber;
        studentRef.child("classes").child(student.getGroup()).child(student.getFirebaseKey()).child("card_number").setValue(cardNumber.toLowerCase());
        incrementVersion();
    }

    public void incrementVersion() {
        Query myTopPostsQuery = studentRef.child("lyceum_student_list_ver");
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    long version = (long) dataSnapshot.getValue();
                    version++;
                    studentRef.child("lyceum_student_list_ver").setValue(version);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    selectedFragment = 0;
                    changeFragment(new BreakfastFragment());
                    return true;
                case R.id.navigation_dashboard:
                    selectedFragment = 1;
                    changeFragment(new LunchFragment());
                    return true;
                case R.id.navigation_notifications:
                    selectedFragment = 2;
                    changeFragment(new DinnerFragment());
                    return true;
            }
            return false;
        }
    };

    public void changeFragment(Fragment current_fragment) {
        Fragment fragment = current_fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();

    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}
