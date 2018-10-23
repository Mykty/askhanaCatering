package com.example.mykty.askhanacatering.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.database.StoreDatabase;
import com.example.mykty.askhanacatering.module.EaterItem;
import com.example.mykty.askhanacatering.module.Personnel;
import com.example.mykty.askhanacatering.module.Report;
import com.example.mykty.askhanacatering.module.Student;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.example.mykty.askhanacatering.database.StoreDatabase.TABLE_COLLEGE_STUDENTS;

public class DayliReportList extends AppCompatActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    boolean[] personSelected, foodTimeSelected;
    ArrayList<String> datesSelected;
    ArrayList<Report> reportList;
    DatabaseReference mDatabaseRef;
    String personStore[] = {"personnel", "college", "lyceum"};
    String foodStore1[] = {"breakfast", "lunch", "dinner"};
    String foodStore2[] = {"breakfast", "poldnik1", "lunch", "poldnik2", "dinner"};
    HashMap<String, String> foodInKaz;
    HashMap<String, Integer> reportCal;

    String TABLE_COLLEGE_STUDENTS = "college_students_list";
    String TABLE_PERSONNEL = "personnel_store";
    StoreDatabase storeDb;

    SQLiteDatabase sqdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dayli_report_list);
        datesSelected = new ArrayList<>();
        reportList = new ArrayList<>();

//        Bundle bundle = getIntent().getExtras();
//        personSelected = bundle.getBooleanArray("personSelected");
//        foodTimeSelected = bundle.getBooleanArray("foodTimeSelected");
//        datesSelected = bundle.getStringArrayList("datesSelected");

        reportCal = new HashMap<>();

        foodInKaz = new HashMap<>();
        foodInKaz.put("breakfast", "Таңғы ас");
        foodInKaz.put("lunch", "Түскі ас");
        foodInKaz.put("dinner", "Кешкі ас");
        foodInKaz.put("poldnik1", "Полдник1");
        foodInKaz.put("poldnik2", "Полдник2");

        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        getDataFromFirebase2();
    }

    public void getData() {
        for (int i = 0; i < personSelected.length; i++) {
            if (personSelected[i]) {
                String pItem = personStore[i];

                for (int j = 0; j < datesSelected.size(); j++) {
                    String dItem = datesSelected.get(j);

                    for (int k = 0; k < foodTimeSelected.length; k++) {
                        String fItem = "";
                        if (foodTimeSelected[k]) {
                            if (personSelected[2]) fItem = foodStore2[k];
                            else fItem = foodStore1[k];

                            getDataFromFirebase(pItem, dItem, fItem);

                        }

                    }
                }

            }
        }
    }

    String fKz;
    int repoCount = 0;

    public void getDataFromFirebase(final String personItem, final String dateItem, final String foodItem) {
        mDatabaseRef.child("days").child(personItem).child(dateItem).child(foodItem).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    fKz = foodInKaz.get(foodItem);
                    for (DataSnapshot eaterItemData : dataSnapshot.getChildren()) {
                        String id_number = eaterItemData.getKey().toString();
                        String time = eaterItemData.getValue().toString();

                        String personnelName = getPersonnelsByIdNumber(id_number);
                        if (personnelName != null) {
                            Report report = new Report("Персонал", personnelName, fKz, dateItem, time);
                            reportList.add(report);

                        } else {

                            String studentName = getStudentsByIdNumber(id_number);

                            if (studentName != null) {

                                Report report = new Report("Колледж студент", studentName, fKz, dateItem, time);
                                reportList.add(report);

//                                Log.i("firebase", "Type: Student | Name: " + student.getName() + " | Ішкен күні: " + dateItem + " | Уақыты: " + time);
                            } else {
//                                Log.i("firebase", "Type: Lyceum");
                            }
                        }
                    }

                } else {
                    Report report = new Report(personItem, " ", fKz, dateItem, "ешкім ішпеген");
                    reportList.add(report);

                    Log.i("firebase", "" + personItem + " " + dateItem + " " + foodItem + " is not exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void getDataFromFirebase2() {
        mDatabaseRef.child("days").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    for (DataSnapshot orgTypes : dataSnapshot.getChildren()) {
                        String organizationName = orgTypes.getKey().toString();

                        for (DataSnapshot dates : orgTypes.getChildren()) {
                            String dateItem = dates.getKey().toString();

                            if (dateItem.charAt(3) == '1') {
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

                                            if(foodTime.equals(foodStore1[0])){
                                                totalBreakfast++;
                                            }else if(foodTime.equals(foodStore1[1])){
                                                totalLunch++;
                                            }else if(foodTime.equals(foodStore1[2])){
                                                totalDinner++;
                                            }

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

//                    exportToExcel2(reportCal);
//                    Log.i("fire", reportCal.toString());
                    exportToExcel2(reportCal);

//                    exportToExcel(reportList);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    int totalBreakfast = 0, totalLunch = 0, totalDinner = 0;
    private void exportToExcel2(HashMap<String, Integer> repo) {
        HashMap<String, Integer> readyReportList = repo;

        verifyStoragePermissions(this);
        final String fileName = "Askhana_октябрь.xls";

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

                        type = "Персонал";
                        pName = getPersonnelsByIdNumber(id_number);
                        //Log.i("info", "Персонал: " + pName + " Тамақ кезеңі: " + foodTime + " " + eatCount);
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

    private void exportToExcel(ArrayList<Report> repo) {
        ArrayList<Report> readyReportList = repo;

        verifyStoragePermissions(this);
        final String fileName = "Askhana_" + getRandom() + ".xls";

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
                sheet.addCell(new Label(2, 0, "Ішкен күні"));
                sheet.addCell(new Label(3, 0, "Тамақ кезеңі"));
                sheet.addCell(new Label(4, 0, "Уақыты"));

                for (int i = 1; i < readyReportList.size(); i++) {
                    String type = readyReportList.get(i).getType();
                    String name = readyReportList.get(i).getName();
                    String fKezen = readyReportList.get(i).getfKezen();
                    String date = readyReportList.get(i).getDate();
                    String time = readyReportList.get(i).getTime();

                    sheet.addCell(new Label(0, i, type));
                    sheet.addCell(new Label(1, i, name));
                    sheet.addCell(new Label(2, i, date));
                    sheet.addCell(new Label(3, i, fKezen));
                    sheet.addCell(new Label(4, i, time));
                }

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

    public String getRandom() {
        Date date = new Date();
        String idN = "i" + date.getTime();
        return idN;
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

}


    /*public void getDataFromFirebase(final String personItem, final String dateItem, final String foodItem) {
        mDatabaseRef.child("days").child(personItem).child(dateItem).child(foodItem).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (DataSnapshot eaterItemData : dataSnapshot.getChildren()) {
                        String id_number = eaterItemData.getKey().toString();
                        String time = eaterItemData.getValue().toString();
                        String fKz = foodInKaz.get(foodItem);

                        String personnelName = getPersonnelsByIdNumber(id_number);
                        if (personnelName != null) {
                            Report report = new Report("Персонал", personnelName, fKz, dateItem, time);
                            reportList.add(report);

                        } else {

                            String studentName = getStudentsByIdNumber(id_number);

                            if (studentName != null) {

                                Report report = new Report("Колледж студент", studentName, fKz, dateItem, time);
                                reportList.add(report);

//                                Log.i("firebase", "Type: Student | Name: " + student.getName() + " | Ішкен күні: " + dateItem + " | Уақыты: " + time);
                            } else {
//                                Log.i("firebase", "Type: Lyceum");
                            }
                        }
                    }
                    exportToExcel(reportList);

                } else {
                    Log.i("firebase", "" + personItem + " " + dateItem + " " + foodItem + " is not exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }*/