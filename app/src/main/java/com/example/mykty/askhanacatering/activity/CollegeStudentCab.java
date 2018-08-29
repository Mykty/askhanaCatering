package com.example.mykty.askhanacatering.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.fragments.eaters_fragments.BreakfastFragment;
import com.example.mykty.askhanacatering.fragments.eaters_fragments.DinnerFragment;
import com.example.mykty.askhanacatering.fragments.eaters_fragments.LunchFragment;
import com.example.mykty.askhanacatering.fragments.report_fragments.LunchReportFragment;
import com.example.mykty.askhanacatering.module.Student;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarPickerView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.squareup.timessquare.CalendarPickerView.SelectionMode.MULTIPLE;

public class CollegeStudentCab extends AppCompatActivity implements View.OnClickListener {

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
    int bPrice = 500, lPrice = 600, dPrice = 650;
    BreakfastFragment breakfastFragment;
    LunchFragment lunchFragment;
    TextView tvBPrice, tvLPrice, tvDPrice, sumPrice;
    Button btnPay;
    DatabaseReference mDatabaseRef;
    DateFormat dateF;
    String idNumber, imgUrl;
    int bRes = 0, lRes = 0, dRes = 0;

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

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef = mDatabaseRef.child("payed").child("college").child(idNumber);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

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
    }

    public String getIdNumber(){
        return idNumber;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
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
                        Log.i("cal", "Breakfast: "+cDate);
                    }
                }
                if (lDates!=null &&  lDates.size() !=0 ){
                    for(Date date: lDates){
                        String cDate = dateF.format(date);
                        mDatabaseRef.child("lunch").child(cDate).setValue(1);
                        Log.i("cal", "Lunch: "+cDate);
                    }
                }
                if (dDates!=null &&  dDates.size() !=0 ){
                    for(Date date: dDates){
                        String cDate = dateF.format(date);
                        mDatabaseRef.child("dinner").child(cDate).setValue(1);
                        Log.i("cal", "Dinner: "+cDate);
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

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    changeFragment(new BreakfastFragment());
                    return true;
                case R.id.navigation_dashboard:
                    changeFragment(new LunchFragment());
                    return true;
                case R.id.navigation_notifications:
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
}
