package com.example.mykty.askhanacatering.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.activity.DayliReportList;
import com.example.mykty.askhanacatering.database.StoreDatabase;
import com.example.mykty.askhanacatering.fragments.report_fragments.BreakfastReportFragment;
import com.example.mykty.askhanacatering.fragments.report_fragments.DinnerReportFragment;
import com.example.mykty.askhanacatering.fragments.report_fragments.LunchReportFragment;
import com.example.mykty.askhanacatering.module.MultiSpinner;
import com.example.mykty.askhanacatering.module.Report;
import com.example.mykty.askhanacatering.module.ViewPagerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.timessquare.CalendarPickerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import static com.squareup.timessquare.CalendarPickerView.SelectionMode.MULTIPLE;
import static com.squareup.timessquare.CalendarPickerView.SelectionMode.RANGE;

public class DayliReportFragment extends Fragment implements View.OnClickListener {
    View view;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    Dialog repoDialog, repoDialogCalendar;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private MultiSpinner spinnerPersons, spinnerTime;
    private ArrayAdapter<String> personsStoreAdapter, foodTimeStoreAdapter;
    String[] pStore, fTimeStore1, fTimeStore2;
    Button selectDaysBtn, daysSelectedBtn, scrollToTodayBtn, btnReporSubmit;
    Date minDate, today;
    CalendarPickerView calendar;
    List<Date> dates;
    ArrayList<String> datesSelected;
    SimpleDateFormat formatStr, formatStr2;
    ProgressDialog reportLoadingPd, cleaningProgDialog;
    DatabaseReference mDatabaseRef;
    String fKz;
    int repoCount = 0;
    HashMap<String, String> foodInKaz;
    HashMap<String, Integer> reportCal;
    ArrayList<Report> reportList;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    String TABLE_COLLEGE_STUDENTS = "college_students_list";
    String TABLE_PERSONNEL = "personnel_store";
    String TABLE_LYCEUM_STUDENTS = "lyceum_students_list";
    int totalBreakfast = 0, totalLunch = 0, totalDinner = 0;
    String personStore[] = {"personnel", "college", "lyceum"};
    String foodStore1[] = {"breakfast", "lunch", "dinner"};
    String foodStore2[] = {"breakfast", "poldnik1", "lunch", "poldnik2", "dinner"};
    Dialog cleanDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_today, container, false);

        setupViews();

        return view;
    }

    public void setupViews() {
        getActivity().setTitle(getResources().getString(R.string.dayli_report));
        viewPager = view.findViewById(R.id.viewpager);

        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        reportLoadingPd = new ProgressDialog(getActivity());
        reportLoadingPd.setMessage("Loading");
        reportLoadingPd.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        cleaningProgDialog = new ProgressDialog(getActivity());
        cleaningProgDialog.setMessage("Loading");

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        reportList = new ArrayList<>();
        reportCal = new HashMap<>();
        foodInKaz = new HashMap<>();
        foodInKaz.put("breakfast", "Таңғы ас");
        foodInKaz.put("lunch", "Түскі ас");
        foodInKaz.put("dinner", "Кешкі ас");
        foodInKaz.put("poldnik1", "Полдник1");
        foodInKaz.put("poldnik2", "Полдник2");

        setupViewPager();
        createDateDialogs();

    }

    public void createDateDialogs() {

        repoDialog = new Dialog(getActivity());
        repoDialog.setContentView(R.layout.dialog_report);
        repoDialog.setTitle(getResources().getString(R.string.dayli_report_title));
        btnReporSubmit = repoDialog.findViewById(R.id.btnReporSubmit);

        repoDialogCalendar = new Dialog(getActivity());
        repoDialogCalendar.setContentView(R.layout.dialog_report_calendar);

        datesSelected = new ArrayList<>();
        daysSelectedBtn = repoDialogCalendar.findViewById(R.id.okBtn);
        scrollToTodayBtn = repoDialogCalendar.findViewById(R.id.btnToday);

        daysSelectedBtn.setOnClickListener(this);
        scrollToTodayBtn.setOnClickListener(this);
        btnReporSubmit.setOnClickListener(this);

        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        minDate = new Date(2018 - 1900, 7, 1);
        today = new Date();
        today.getTime();

        calendar = repoDialogCalendar.findViewById(R.id.calendar_view);
        calendar.init(minDate, nextYear.getTime()).inMode(MULTIPLE);

        selectDaysBtn = repoDialog.findViewById(R.id.selectDays);
        selectDaysBtn.setOnClickListener(this);

        pStore = getResources().getStringArray(R.array.personsStore);
        fTimeStore1 = getResources().getStringArray(R.array.foodTimeStore1);
        fTimeStore2 = getResources().getStringArray(R.array.foodTimeStore2);

        personsStoreAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, pStore);
        foodTimeStoreAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, fTimeStore1);

        spinnerPersons = repoDialog.findViewById(R.id.spinnerPersons);
        spinnerPersons.setAdapter(personsStoreAdapter, false, onPersonSelectedListener);
        spinnerPersons.setAllSelectedDisplayMode(MultiSpinner.AllSelectedDisplayMode.DisplayAllItems);

        spinnerTime = repoDialog.findViewById(R.id.spinnerTime);
        spinnerTime.setAdapter(foodTimeStoreAdapter, false, onTimeSelectedListener);
        spinnerTime.setAllSelectedDisplayMode(MultiSpinner.AllSelectedDisplayMode.DisplayAllItems);

        formatStr = new SimpleDateFormat("EEEE dd.MM");
        formatStr2 = new SimpleDateFormat("dd_MM_yyyy");

        cleanDialog = new Dialog(getActivity());
        cleanDialog.setContentView(R.layout.dialog_clean);



        /*
        boolean[] selectedItems = new boolean[adapter.getCount()];
        selectedItems[1] = true; // select second item
        spinner.setSelected(selectedItems);
        */

    }

    boolean[] personSelected, foodTimeSelected;
    private MultiSpinner.MultiSpinnerListener onPersonSelectedListener = new MultiSpinner.MultiSpinnerListener() {
        public void onItemsSelected(boolean[] selected) {

            if (selected[2]) {
                foodTimeStoreAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, fTimeStore2);
            } else {
                foodTimeStoreAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, fTimeStore1);
            }

            personSelected = selected;

            spinnerPersons.setSelected(selected);
            spinnerTime.setAdapter(foodTimeStoreAdapter, false, onTimeSelectedListener);

        }
    };

    private MultiSpinner.MultiSpinnerListener onTimeSelectedListener = new MultiSpinner.MultiSpinnerListener() {
        public void onItemsSelected(boolean[] selected) {
            spinnerTime.setSelected(selected);
            foodTimeSelected = selected;
        }
    };

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new BreakfastReportFragment(), getResources().getString(R.string.title_breakfast));
        adapter.addFragment(new LunchReportFragment(), getResources().getString(R.string.title_lunch));
        adapter.addFragment(new DinnerReportFragment(), getResources().getString(R.string.title_dinner));

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.report_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.reportMonth) {
//            File file = new File("");
//            shareFile(file);
            reportLoadingPd.show();
            loadReportMonthly();

//            Intent intent = new Intent(getActivity(),DayliReportList.class);
//            startActivity(intent);

            return true;
        }

        if (id == R.id.cleanMonth) {
            cleanDialog.show();
            Button btn = cleanDialog.findViewById(R.id.btnClean);
            final EditText mNumber = cleanDialog.findViewById(R.id.monthNumber);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mNumber.getText().length() > 0) {
                        cleanFirebaseMonth(mNumber.getText().toString());
//                        cleanDialog.dismiss();
                    } else {

                    }
                }
            });

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void cleanFirebaseMonth(final String mNumber) {
        cleaningProgDialog.show();

        mDatabaseRef.child("days").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.i("info", "days");
                for (DataSnapshot organization : dataSnapshot.getChildren()) {
                    String orgName = organization.getKey().toString();

                    for (DataSnapshot daysSnapshot : organization.getChildren()) {

                        String day = daysSnapshot.getKey();
                        if (day.substring(3, 5).equals(mNumber)) {
                            Log.i("info", "Deleted: Day: " + day);
                            mDatabaseRef.child("days").child(orgName).child(day).removeValue();

                        }

                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseRef.child("f_time").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("info", "f_time");

                for (DataSnapshot foodTime : dataSnapshot.getChildren()) {
                    String foodTimeName = foodTime.getKey().toString();
                    for (DataSnapshot orgSnapshot : foodTime.getChildren()) {
                        String orgName = orgSnapshot.getKey().toString();
                        for (DataSnapshot daysSnapshot : orgSnapshot.getChildren()) {

                            String day = daysSnapshot.getKey();
                            if (day.substring(3, 5).equals(mNumber)) {
                                Log.i("info", "Deleted: Day: " + day);
                                mDatabaseRef.child("f_time").child(foodTimeName).child(orgName).child(day).removeValue();

                            }

                        }
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseRef.child("payed").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("info", "payed");

                for (DataSnapshot orgSnapshot : dataSnapshot.getChildren()) {
                    String orgName = orgSnapshot.getKey().toString();

                    for (DataSnapshot userId : orgSnapshot.getChildren()) {
                        String uId = userId.getKey().toString();

                        for (DataSnapshot fTime : userId.getChildren()) {
                            String fTimeName = fTime.getKey().toString();

                            for (DataSnapshot days : fTime.getChildren()) {

                                String day = days.getKey();
                                if (day.substring(3, 5).equals(mNumber)) {
                                    Log.i("info", "Deleted: Day: " + day);

                                    mDatabaseRef.child("payed").child(orgName).child(uId).child(fTimeName).child(day).removeValue();

                                }

                            }

                        }

                    }


                }
                cleaningProgDialog.dismiss();
                cleanDialog.dismiss();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.selectDays:

                repoDialogCalendar.show();

                break;

            case R.id.btnToday:

                calendar.scrollToDate(today);

                break;

            case R.id.okBtn:
                dates = calendar.getSelectedDates();
                datesSelected.clear();

                String datesStr = "";
                for (Date d : dates) {
                    String date = formatStr.format(d);
                    datesStr += date + "\n";
                    datesSelected.add(formatStr2.format(d));
                }

                selectDaysBtn.setText(datesStr.substring(0, datesStr.length() - 1));
                repoDialogCalendar.dismiss();

                break;

            case R.id.btnReporSubmit:

                Intent intent = new Intent(getActivity(), DayliReportList.class);

                Bundle bundle = new Bundle();
                bundle.putBooleanArray("personSelected", personSelected);
                bundle.putStringArrayList("datesSelected", datesSelected);
                bundle.putBooleanArray("foodTimeSelected", foodTimeSelected);
                intent.putExtras(bundle);

                repoDialog.dismiss();
                startActivity(intent);

                break;
        }
    }

    public void loadReportMonthly() {
//        reportLoadingPd.show();
        reportCal.clear();
        mDatabaseRef.child("days").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    for (DataSnapshot orgTypes : dataSnapshot.getChildren()) {
                        String organizationName = orgTypes.getKey().toString();

                        for (DataSnapshot dates : orgTypes.getChildren()) {
                            String dateItem = dates.getKey().toString();

                            if (dateItem.charAt(4) == '1') {
                                for (DataSnapshot fTypes : dates.getChildren()) {
                                    String foodTime = fTypes.getKey().toString();
                                    fKz = foodInKaz.get(foodTime);

                                    for (DataSnapshot eaterItemData : fTypes.getChildren()) {

                                        String id_number = eaterItemData.getKey().toString();
                                        String time = eaterItemData.getValue().toString();
                                        String personName = "";
                                        String personType = "";


                                        if (organizationName.equals("college")) {
                                            personName = getStudentsByIdNumber(id_number);
                                            personType = "Колледж студент";

                                        } else if (organizationName.equals("personnel")) {

                                            personName = getPersonnelsByIdNumber(id_number);
                                            personType = "Персонал";

                                            if (foodTime.equals(foodStore1[0])) {
                                                totalBreakfast++;
                                            } else if (foodTime.equals(foodStore1[1])) {
                                                totalLunch++;
                                            } else if (foodTime.equals(foodStore1[2])) {
                                                totalDinner++;
                                            }

                                        } else if (organizationName.equals("lyceum")) {

                                            personName = getPupilsByIdNumber(id_number);
                                            personType = "Лицей оқушы";

                                        }

                                        if (reportCal.containsKey(id_number + "_" + fKz)) {
                                            repoCount = reportCal.get(id_number + "_" + fKz);
                                            repoCount++;
                                            reportCal.put(id_number + "_" + fKz, repoCount);

                                        } else {
                                            reportCal.put(id_number + "_" + fKz, 1);
                                        }

                                        Report report = new Report("" + personType, personName, fKz, dateItem, time);
                                        reportList.add(report);

                                        Log.i("fire", personType + " " + personName + " " + fKz + " " + dateItem + " " + time);
                                    }
                                }
                            }
                        }

                    }

                    exportToExcel(reportCal);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void exportToExcel(HashMap<String, Integer> repo) {
        HashMap<String, Integer> readyReportList = repo;
        verifyStoragePermissions(getActivity());

        final String fileName = "Askhana_ноябрь.xls";

        String path = Environment.getExternalStorageDirectory() + "/" + "Askhana_report/";

        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, fileName);

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook;

        try {
            file.createNewFile();
            workbook = Workbook.createWorkbook(file, wbSettings);
            WritableSheet sheet = workbook.createSheet("Report", 0);

            try {
                sheet.addCell(new Label(0, 0, "Тип"));
                sheet.addCell(new Label(1, 0, "Аты-жөні"));
                sheet.addCell(new Label(2, 0, "Тамақ кезеңі"));
                sheet.addCell(new Label(3, 0, "Ішкен саны"));

                int i = 1;
                for (String key : readyReportList.keySet()) {

                    String id_number = key.substring(0, key.indexOf("_"));
                    String foodTime = key.substring(key.indexOf("_") + 1, key.length());
                    String type = "";
                    int eatCount = readyReportList.get(key);


                    String pName = getStudentsByIdNumber(id_number);
                    if (pName != null) {
                        type = "Студент";
                        //Log.i("info", "Студент: " + pName + " Тамақ кезеңі: " + foodTime + " " + eatCount);

                    } else {
                        pName = getPupilsByIdNumber(id_number);

                        if (pName != null) {
                            type = "Оқушы";

                        }else {
                            type = "Персонал";
                            pName = getPersonnelsByIdNumber(id_number);
                            //Log.i("info", "Персонал: " + pName + " Тамақ кезеңі: " + foodTime + " " + eatCount);
                        }
                    }

                    sheet.addCell(new Label(0, i, type));
                    sheet.addCell(new Label(1, i, pName));
                    sheet.addCell(new Label(2, i, foodTime));
                    sheet.addCell(new Label(3, i, "" + eatCount));
                    i++;
                }

                sheet.addCell(new Label(0, i, "Персонал"));
                sheet.addCell(new Label(1, i, "Total"));
                sheet.addCell(new Label(2, i, getString(R.string.title_breakfast)));
                sheet.addCell(new Label(3, i, "" + totalBreakfast));

                i++;
                sheet.addCell(new Label(0, i, "Персонал"));
                sheet.addCell(new Label(1, i, "Total"));
                sheet.addCell(new Label(2, i, getString(R.string.title_lunch)));
                sheet.addCell(new Label(3, i, "" + totalLunch));

                i++;
                sheet.addCell(new Label(0, i, "Персонал"));
                sheet.addCell(new Label(1, i, "Total"));
                sheet.addCell(new Label(2, i, getString(R.string.title_dinner)));
                sheet.addCell(new Label(3, i, "" + totalDinner));

                reportLoadingPd.dismiss();
//                shareFile(file);

            } catch (RowsExceededException e) {
                e.printStackTrace();
            } catch (WriteException e) {
                e.printStackTrace();
            }
            workbook.write();
            try {
                workbook.close();
            } catch (WriteException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void shareFile(File filePath) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(filePath));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);

    }

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public String getPupilsByIdNumber(String idNumber) {
        String studentName = null;
        Cursor cursorStd = sqdb.rawQuery("SELECT info FROM " + TABLE_LYCEUM_STUDENTS + " WHERE id_number=?", new String[]{idNumber});
        if (((cursorStd != null) && (cursorStd.getCount() > 0))) {
            cursorStd.moveToNext();
            studentName = cursorStd.getString(0);
        }
        cursorStd.close();
        return studentName;
    }

    public String getStudentsByIdNumber(String idNumber) {
        String studentName = null;
        Cursor cursorStd = sqdb.rawQuery("SELECT info FROM " + TABLE_COLLEGE_STUDENTS + " WHERE id_number=?", new String[]{idNumber});
        if (((cursorStd != null) && (cursorStd.getCount() > 0))) {
            cursorStd.moveToNext();
            studentName = cursorStd.getString(0);
        }
        cursorStd.close();
        return studentName;
    }

    public String getPersonnelsByIdNumber(String idNumber) {
        String personnelName = null;
        Cursor cursorStd = sqdb.rawQuery("SELECT info FROM " + TABLE_PERSONNEL + " WHERE id_number=?", new String[]{idNumber});
        if (((cursorStd != null) && (cursorStd.getCount() > 0))) {
            cursorStd.moveToNext();
            personnelName = cursorStd.getString(0);
        }
        cursorStd.close();
        return personnelName;
    }

}
