package com.example.mykty.askhanacatering.fragments.eaters_fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.activity.CollegeStudentCab;
import com.example.mykty.askhanacatering.module.GroupDataItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarPickerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import sun.bob.mcalendarview.CellConfig;
import sun.bob.mcalendarview.MCalendarView;
import sun.bob.mcalendarview.MarkStyle;
import sun.bob.mcalendarview.listeners.OnDateClickListener;
import sun.bob.mcalendarview.listeners.OnExpDateClickListener;
import sun.bob.mcalendarview.listeners.OnMonthScrollListener;
import sun.bob.mcalendarview.views.ExpCalendarView;
import sun.bob.mcalendarview.views.WeekColumnView;
import sun.bob.mcalendarview.vo.DateData;

import static com.squareup.timessquare.CalendarPickerView.SelectionMode.MULTIPLE;
import static com.squareup.timessquare.CalendarPickerView.SelectionMode.SINGLE;

public class BreakfastFragment extends Fragment implements View.OnClickListener {

    View view;
    DatabaseReference mDatabaseRef;
    ArrayList<String> payedDates, markedDatesStore;
    List<Date> highlightDatesList;
    private TextView YearMonthTv, textViewDayLeft;
    String[] monthStore;
    private ExpCalendarView expCalendarView;
    private DateData selectedDate;
    WeekColumnView weekColumnView;
    private boolean ifExpand = false;
    String idNumber;
    int tYear, tMonth, tDay;
    int countDaysLeft = 0;
    Calendar calendar;
    Dialog editPayedDialog;
    long datesCount = 0;
    DateFormat dateF;
    AlertDialog.Builder dialogBuilder;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_breakfast, container, false);
        setupViews();
        createDialogs();
        return view;
    }

    public void setupViews() {
        monthStore = getResources().getStringArray(R.array.monthStore);
        expCalendarView = view.findViewById(R.id.calendar_exp);
        YearMonthTv = view.findViewById(R.id.main_YYMM_Tv);
        weekColumnView = view.findViewById(R.id.weekColumn);
        textViewDayLeft = view.findViewById(R.id.textViewTitle);
        payedDates = new ArrayList<>();
        markedDatesStore = new ArrayList<>();
        highlightDatesList = new ArrayList<>();

        calendar = Calendar.getInstance();
        String monthName = monthStore[(Calendar.getInstance().get(Calendar.MONTH))];
        YearMonthTv.setText(monthName + " " + Calendar.getInstance().get(Calendar.YEAR));

        expCalendarView.unMarkDates();
        idNumber = ((CollegeStudentCab) getActivity()).getIdNumber();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mDatabaseRef = mDatabaseRef.child("payed").child("college").child(idNumber).child("breakfast");

        dateF = new SimpleDateFormat("dd_MM_yyyy");

        travelToToday();
        expCalendarListeners();
        shrinkCalendar();
        getPayedDates();

        weekColumnView.setOnClickListener(this);
        YearMonthTv.setOnClickListener(this);

        tYear = calendar.get(Calendar.YEAR);
        tMonth = calendar.get(Calendar.MONTH) + 1;
        tDay = calendar.get(Calendar.DAY_OF_MONTH);
        textViewDayLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                travelToToday();
            }
        });
    }

    Button btnReplace, btnDel;
    CalendarPickerView calendarPicker;
    TextView selectDayTv;
    AlertDialog alertDialogDeleting, alertDialogEditing;
    boolean newDaySelected = false;
    public void createDialogs() {
        editPayedDialog = new Dialog(getActivity());
        editPayedDialog.setContentView(R.layout.dialog_payed_edit);
        editPayedDialog.setTitle(getResources().getString(R.string.oneD));

        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        Date today = new Date();
        today.getTime();

        selectDayTv = editPayedDialog.findViewById(R.id.selectDay);

        calendarPicker = editPayedDialog.findViewById(R.id.calendar_view_detail);
        calendarPicker.init(today, nextYear.getTime()).inMode(SINGLE);

        calendarPicker.setOnDateSelectedListener(new CalendarPickerView.OnDateSelectedListener() {
            @Override
            public void onDateSelected(Date date) {
                if(!highlightDatesList.contains(date)){

                    selectDayTv.setTextColor(getResources().getColor(R.color.blue_light));
                    selectDayTv.setText(dateF.format(date).replace("_","."));
                    newDaySelected = true;
                }else{
                    selectDayTv.setTextColor(Color.RED);
                    selectDayTv.setText(getResources().getString(R.string.selectAnotherDay));
                    Toast.makeText(getActivity(), getResources().getString(R.string.breakfastSelected), Toast.LENGTH_LONG).show();
                    newDaySelected = false;
                }
            }

            @Override
            public void onDateUnselected(Date date) {

            }
        });

        btnReplace = editPayedDialog.findViewById(R.id.btnReplace);
        btnDel = editPayedDialog.findViewById(R.id.btnDel);
        btnReplace.setOnClickListener(this);
        btnDel.setOnClickListener(this);

        alertDialogDeleting = new AlertDialog.Builder(getActivity()).create();
        alertDialogDeleting.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabaseRef.child(pressedDate.replace('.','_')).removeValue();
                        Toast.makeText(getActivity(), selectedDateStr+" "+getString(R.string.deleted), Toast.LENGTH_SHORT).show();
                        editPayedDialog.dismiss();

                    }
                });
        alertDialogDeleting.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialogEditing = new AlertDialog.Builder(getActivity()).create();
        alertDialogEditing.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mDatabaseRef.child(pressedDate.replace('.','_')).removeValue();
                        mDatabaseRef.child(newDayStr.replace('.','_')).setValue(1);
                        editPayedDialog.dismiss();

                    }
                });
        alertDialogEditing.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

    }

    public void getPayedDates() {
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                payedDates.clear();
                markedDatesStore.clear();
                highlightDatesList.clear();

                datesCount = dataSnapshot.getChildrenCount();
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    payedDates.add(dateSnapshot.getKey().toString());
                }

                if (payedDates.size() == datesCount) {
                    markDates();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void markDates() {
        expCalendarView.unMarkDates();
        int markColor = Color.rgb(0, 148, 243);
        expCalendarView.setMarkedStyle(MarkStyle.BACKGROUND, markColor);
        countDaysLeft = 0;

        for (String date : payedDates) {//24_08_2018
            int year = Integer.parseInt(""+date.charAt(6)+ date.charAt(7)+ date.charAt(8) + date.charAt(9));
            int month = Integer.parseInt(date.charAt(3) + "" + date.charAt(4));
            int day = Integer.parseInt(date.charAt(0) + "" + date.charAt(1));

            boolean doMark;

            Log.i("info", "Today: " + tYear + " " + tMonth + " " + tDay);

            if (tYear < year) {
                doMark = true;
            } else if (tYear == year && tMonth < month) {
                doMark = true;

            } else if (tYear == year && tMonth == month && tDay <= day) {
                doMark = true;

            } else {
                doMark = false;
            }

            if (doMark) {
                DateData dateData = new DateData(year, month, day);
                expCalendarView.markDate(dateData);
                countDaysLeft++;
                markedDatesStore.add(date);

                Date dateD = new Date(year-1900, month-1, day);
                highlightDatesList.add(dateD);
            }
        }

        textViewDayLeft.setText(getResources().getString(R.string.daysLeft)+" "+countDaysLeft+" "+getResources().getString(R.string.day));
    }

    public void travelToToday() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        DateData dateData = new DateData(year, month, calendar.get(Calendar.DAY_OF_MONTH));

        YearMonthTv.setText(String.format("%s %d", monthStore[month - 1], year));
        expCalendarView.travelTo(dateData);
    }

    String pressedDate, selectedDateStr;
    public void expCalendarListeners() {
        expCalendarView.setOnDateClickListener(new OnExpDateClickListener()).setOnMonthScrollListener(new OnMonthScrollListener() {
            @Override
            public void onMonthChange(int year, int month) {
                YearMonthTv.setText(String.format("%s %d", monthStore[month - 1], year));
            }

            @Override
            public void onMonthScroll(float positionOffset) {
            }
        });

        expCalendarView.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onDateClick(View view, DateData date) {
//                onClick(view);
//                expCalendarView.getMarkedDates().removeAdd();
//                expCalendarView.markDate(date);


                String year = ""+date.getYear();
                String month = date.getMonthString();
                String day = date.getDayString();

                String conDate = day+"_"+month+"_"+year;
                if(markedDatesStore.contains(conDate)) {
                    pressedDate = conDate.replace("_",".");
                    selectedDateStr = getResources().getString(R.string.title_breakfast)+": "+pressedDate;
                    editPayedDialog.setTitle(selectedDateStr);

                    calendarPicker.clearHighlightedDates();
                    calendarPicker.highlightDates(highlightDatesList);

                    editPayedDialog.show();
                }else{
                    Toast.makeText(getActivity(), getResources().getString(R.string.selectDay), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void shrinkCalendar() {
        CellConfig.Month2WeekPos = CellConfig.middlePosition;
        CellConfig.ifMonth = false;
        CellConfig.weekAnchorPointDate = selectedDate;
        expCalendarView.shrink();
    }
    String newDayStr;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnReplace:
                //.replace("_",".")
                if(calendarPicker.getSelectedDate() != null && newDaySelected) {
                    newDayStr = dateF.format(calendarPicker.getSelectedDate()).replace("_",".");
                    alertDialogEditing.setTitle(getString(R.string.title_breakfast)+" "+getString(R.string.replace2));
                    alertDialogEditing.setMessage("Ескі күн:    " + pressedDate + "\n\n" + "Жаңа күн: " + newDayStr);
                    alertDialogEditing.show();
                }else{
                    Toast.makeText(getActivity(), getString(R.string.newDaySelectMistake), Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btnDel:
                alertDialogDeleting.setTitle(getResources().getString(R.string.delete2));
                alertDialogDeleting.setMessage(selectedDateStr+" өшіргіңіз келеді ме?");
                alertDialogDeleting.show();
                break;

            default:
                if (ifExpand) {
                    shrinkCalendar();
                } else {
                    CellConfig.Week2MonthPos = CellConfig.middlePosition;
                    CellConfig.ifMonth = true;
                    expCalendarView.expand();
                }
                ifExpand = !ifExpand;

        }

    }
}
