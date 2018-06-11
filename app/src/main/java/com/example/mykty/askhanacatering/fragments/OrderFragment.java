package com.example.mykty.askhanacatering.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.activity.AddMenuInOrderActivity;
import com.example.mykty.askhanacatering.adapter.OrderListAdapter;
import com.example.mykty.askhanacatering.module.Order;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class OrderFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener {


    View view;
    RecyclerView orderListView;
    private RecyclerView.LayoutManager linearLayoutManager;
    OrderListAdapter orderListAdapter;
    ArrayList<Order> orderList;
    ArrayList<String> menu;
    Button btnAdd, menuAdd, btnCancel;
    Dialog addNewOrderDialog;
    TextInputEditText title, orderPersonName , dateEditText, time, personCount, phoneNumber;
    int mYear, mMonth, mDay, mHour, mMinute;
    DatabaseReference mDatabaseRef;
    String monthNames [] = {"0", "январь","февраль","март","апрель","май","июнь","июль","август","сентябрь","октябрь","ноябрь","декабрь"};
    RelativeLayout relativeLayout;
    Intent menuIntent;

    CalendarDatePickerDialogFragment cdp;
    RadialTimePickerDialogFragment rtpd;
    String FRAG_TAG_DATE_PICKER = "date_picker";
    CalendarDatePickerDialogFragment.OnDateSetListener onDateSetListener2;
    RadialTimePickerDialogFragment.OnTimeSetListener onTimeSetListener2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_order, container, false);
        getActivity().setTitle("Тапсырыстар");
        setupViews();
        refreshOrders();

        return view;
    }

    public void refreshOrders() {
        Query query = mDatabaseRef.child("orders").orderByChild("date");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                orderList.clear();

                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    orderList.add(order);
                }

                orderListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setupViews() {

        orderListView = view.findViewById(R.id.orderList);
        btnAdd = view.findViewById(R.id.buttonAdd);

        orderListView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());

        orderListView.setLayoutManager(linearLayoutManager);
        orderListView.setItemAnimator(new DefaultItemAnimator());

        addNewOrderDialog = new Dialog(getActivity());
        addNewOrderDialog.setTitle("Жаңа тапсырыс енгізу");
        addNewOrderDialog.setContentView(R.layout.dialog_new_order);

        title = addNewOrderDialog.findViewById(R.id.oTitle);
        orderPersonName = addNewOrderDialog.findViewById(R.id.oPersonName);
        dateEditText = addNewOrderDialog.findViewById(R.id.oDate);
        time = addNewOrderDialog.findViewById(R.id.oTime);
        personCount = addNewOrderDialog.findViewById(R.id.oPersonCount);
        phoneNumber = addNewOrderDialog.findViewById(R.id.oPhoneNumber);
        menuAdd = addNewOrderDialog.findViewById(R.id.btnMenu);
        btnCancel = addNewOrderDialog.findViewById(R.id.btnCancel);

        FirebaseApp.initializeApp(getActivity());
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        menu = new ArrayList<>();

        orderList = new ArrayList<>();
        relativeLayout = view.findViewById(R.id.relaviteL);

        orderListAdapter = new OrderListAdapter(getActivity(), orderList);
        orderListView.setAdapter(orderListAdapter);

        btnAdd.setOnClickListener(this);
        menuAdd.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        createDateAndTimeDialogs();
        menuIntent = new Intent(getActivity(), AddMenuInOrderActivity.class);

        dateEditText.setOnFocusChangeListener(this);
        time.setOnFocusChangeListener(this);


    }


    public void createDateAndTimeDialogs() {
        Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);


        onDateSetListener2 = new CalendarDatePickerDialogFragment.OnDateSetListener() {
            @Override
            public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {

                String dayText, monthText;

                if (dayOfMonth <= 9) {
                    dayText = "0" + dayOfMonth;
                } else
                    dayText = "" + dayOfMonth;

                monthText = monthNames[(monthOfYear + 1)];


                dateEditText.setText(dayText + " " + monthText);
            }
        };

        cdp = new CalendarDatePickerDialogFragment()
                .setOnDateSetListener(onDateSetListener2)
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setPreselectedDate(mYear, mMonth, mDay)
                .setThemeLight()
                .setDoneText("OK")
                .setCancelText("CANCEL");

        onTimeSetListener2 = new RadialTimePickerDialogFragment.OnTimeSetListener(){
            @Override
            public void onTimeSet(RadialTimePickerDialogFragment view, int hourOfDay, int minute) {
                String hourText, minuteText;

                if(hourOfDay <= 9){
                    hourText = "0"+hourOfDay;
                }else{
                    hourText = ""+hourOfDay;
                }

                if(minute <= 9){
                    minuteText = "0"+minute;
                }else{
                    minuteText = ""+minute;
                }
                time.setText(hourText + ":" + minuteText);
            }
        };

        rtpd = new RadialTimePickerDialogFragment()
                .setOnTimeSetListener(onTimeSetListener2)
                .setFutureMinutesLimit(60)
                .setPastMinutesLimit(60)
                .setForced24hFormat()
                .setThemeLight()
                .setDoneText("OK")
                .setCancelText("Cancel");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.order_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus)
            switch (v.getId()) {
                case R.id.oDate:
                    cdp.show(getFragmentManager(), FRAG_TAG_DATE_PICKER);
                    break;

                case R.id.oTime:
                    rtpd.show(getFragmentManager(), FRAG_TAG_DATE_PICKER);
                    break;
            }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonAdd:

                if(checkInetConnection()) {
                    addNewOrderDialog.setCancelable(false);
                    addNewOrderDialog.setCanceledOnTouchOutside(false);
                    addNewOrderDialog.show();
                }

                break;

            case R.id.btnCancel:

                title.getText().clear();
                orderPersonName.getText().clear();
                dateEditText.getText().clear();
                time.getText().clear();
                personCount.getText().clear();
                phoneNumber.getText().clear();
                addNewOrderDialog.dismiss();
                break;

            case R.id.btnMenu:

                String titleText = title.getText().toString();
                String orderPersonNameText = orderPersonName.getText().toString();
                String dateEditTextText = dateEditText.getText().toString();
                String timeText = time.getText().toString();
                String personCountText = personCount.getText().toString();
                String phoneNumberText = phoneNumber.getText().toString();

                boolean hasEmpty = true;

                if (titleText.length() == 0) {
                    title.setError("толтыруды ұмытпаңыз");
                    hasEmpty = false;
                }
                if (orderPersonNameText.length() == 0) {
                    orderPersonName.setError("толтыруды ұмытпаңыз");
                    hasEmpty = false;
                }
                if (dateEditTextText.length() == 0) {
                    dateEditText.setError("толтыруды ұмытпаңыз");
                    hasEmpty = false;
                }
                if (timeText.length() == 0) {
                    time.setError("толтыруды ұмытпаңыз");
                    hasEmpty = false;
                }
                if (personCountText.length() == 0) {
                    personCount.setError("толтыруды ұмытпаңыз");
                    hasEmpty = false;
                }
                if (phoneNumberText.length() == 0) {
                    phoneNumber.setError("толтыруды ұмытпаңыз");
                    hasEmpty = false;
                }

                if (hasEmpty) {
                    menuIntent.putExtra("title", titleText);
                    menuIntent.putExtra("orderPersonName", orderPersonNameText);
                    menuIntent.putExtra("dateEditText", dateEditTextText);
                    menuIntent.putExtra("time", timeText);
                    menuIntent.putExtra("personCount", personCountText);
                    menuIntent.putExtra("phoneNumber", phoneNumberText);

                    title.getText().clear();
                    orderPersonName.getText().clear();
                    dateEditText.getText().clear();
                    time.getText().clear();
                    personCount.getText().clear();
                    phoneNumber.getText().clear();
                    addNewOrderDialog.dismiss();

                    startActivity(menuIntent);
                }

                break;

        }
    }

    public boolean checkInetConnection(){
        if(isNetworkAvailable(getActivity())){
            return true;
        }else{
            Toast.makeText(getActivity(), "Интернет байланысыңызды тексеріңіз", Toast.LENGTH_SHORT).show();
            return false;
        }
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
