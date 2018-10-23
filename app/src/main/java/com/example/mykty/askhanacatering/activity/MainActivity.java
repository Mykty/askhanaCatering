package com.example.mykty.askhanacatering.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.database.StoreDatabase;
import com.example.mykty.askhanacatering.fragments.CollegeListFragment;
import com.example.mykty.askhanacatering.fragments.DayliReportFragment;
import com.example.mykty.askhanacatering.fragments.LyceumListFragment;
import com.example.mykty.askhanacatering.fragments.OrderFragment;
import com.example.mykty.askhanacatering.fragments.PersonnelListFragment;
import com.example.mykty.askhanacatering.fragments.TodayFragment;
import com.example.mykty.askhanacatering.module.Student;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_CARD_NUMBER;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_FKEY;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_GROUP;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_ID_NUMBER;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_INFO;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_LYCEUM_VER;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_PHOTO;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_QR_CODE;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_STUDENTS_VER;
import static com.example.mykty.askhanacatering.database.StoreDatabase.TABLE_COLLEGE_STUDENTS;
import static com.example.mykty.askhanacatering.database.StoreDatabase.TABLE_LYCEUM_STUDENTS;
import static com.example.mykty.askhanacatering.database.StoreDatabase.TABLE_VER;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DatabaseReference mDatabase;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    Query studentsQuery;
    TodayFragment todayFragment;
    PersonnelListFragment personnelListFragment;
    OrderFragment orderFragment;
    DayliReportFragment dayliReportFragment;
    CollegeListFragment collegeListFragment;
    LyceumListFragment lyceumListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
//        navigationView.getMenu().getItem(3).setChecked(true);

        FirebaseApp.initializeApp(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();

        if(checkInetConnection()){
            checkCollegeVersion();
            checkLyceumVersion();

        }

        todayFragment = new TodayFragment();
        personnelListFragment = new PersonnelListFragment();
        orderFragment = new OrderFragment();
        dayliReportFragment = new DayliReportFragment();
        collegeListFragment = new CollegeListFragment();
        lyceumListFragment = new LyceumListFragment();

        changeFragment(todayFragment);
    }

    public void checkCollegeVersion(){
        Query myTopPostsQuery = mDatabase.child("college_student_list_ver");
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    String newVersion = dataSnapshot.getValue().toString();
                    if (!getCollegeStudentCurVer().equals(newVersion)) {
                        updateCollegeStudentCurrentVersion(newVersion);
                        getCollegeStudents();

                        //Toast.makeText(MainActivity.this, "Колледж студенттері туралы ақпарат жаңаланды!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkLyceumVersion(){
        Query myTopPostsQuery = mDatabase.child("lyceum_student_list_ver");
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    String newVersion = dataSnapshot.getValue().toString();
                    if (!getLyceumStudentCurVer().equals(newVersion)) {
                        updateLyceumStudentCurrentVersion(newVersion);
                        getLyceumStudents();

                        //Toast.makeText(MainActivity.this, "Лицей студенттері туралы ақпарат жаңаланды!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getCollegeStudents(){
        storeDb.cleanCollegeStudentsTable(sqdb);

        studentsQuery = mDatabase.child("groups").orderByKey();
        studentsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    for (DataSnapshot groups : dataSnapshot.getChildren()) {

                        String group = groups.getKey();
                        for (DataSnapshot student : groups.getChildren()) {
                            Student student1 = student.getValue(Student.class);

                            ContentValues sValues = new ContentValues();
                            sValues.put(COLUMN_FKEY, student.getKey());
                            sValues.put(COLUMN_QR_CODE, student1.getQr_code());
                            sValues.put(COLUMN_INFO, student1.getName());
                            sValues.put(COLUMN_ID_NUMBER, student1.getId_number());
                            sValues.put(COLUMN_CARD_NUMBER, student1.getCard_number());
                            sValues.put(COLUMN_GROUP, group);
                            sValues.put(COLUMN_PHOTO, student1.getPhoto());

                            sqdb.insert(TABLE_COLLEGE_STUDENTS, null, sValues);

                        }

                    }
                    collegeListFragment.getStudents();
                    collegeListFragment.modifyAdapter();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void getLyceumStudents(){
        storeDb.cleanLyceumStudentsTable(sqdb);
        studentsQuery = mDatabase.child("classes").orderByKey();
        studentsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    for (DataSnapshot classes : dataSnapshot.getChildren()) {

                        String lclass = classes.getKey();
                        for (DataSnapshot student : classes.getChildren()) {
                            Student student1 = student.getValue(Student.class);

                            ContentValues sValues = new ContentValues();
                            sValues.put(COLUMN_INFO, student1.getName());
                            sValues.put(COLUMN_ID_NUMBER, student1.getId_number());
                            sValues.put(COLUMN_CARD_NUMBER, student1.getCard_number());
                            sValues.put(COLUMN_PHOTO, student1.getPhoto());
                            sValues.put(COLUMN_QR_CODE, student1.getQr_code());
                            sValues.put(COLUMN_GROUP, lclass);

                            sqdb.insert(TABLE_LYCEUM_STUDENTS, null, sValues);

                        }

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String getCollegeStudentCurVer() {
        Cursor res = sqdb.rawQuery("SELECT "+COLUMN_STUDENTS_VER+" FROM "+TABLE_VER, null);
        res.moveToNext();
        String version = res.getString(0);
        return version;
    }

    public void updateCollegeStudentCurrentVersion(String newVersion) {
        ContentValues versionValues = new ContentValues();
        versionValues.put(COLUMN_STUDENTS_VER, newVersion);
        sqdb.update(TABLE_VER, versionValues, COLUMN_STUDENTS_VER+"=" + getCollegeStudentCurVer(), null);
    }

    public String getLyceumStudentCurVer() {
        Cursor res = sqdb.rawQuery("SELECT "+COLUMN_LYCEUM_VER+" FROM "+TABLE_VER, null);
        res.moveToNext();
        String version = res.getString(0);
        return version;
    }

    public void updateLyceumStudentCurrentVersion(String newVersion) {
        ContentValues versionValues = new ContentValues();
        versionValues.put(COLUMN_LYCEUM_VER, newVersion);
        sqdb.update(TABLE_VER, versionValues, COLUMN_LYCEUM_VER+"=" + getLyceumStudentCurVer(), null);
    }


    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public boolean checkInetConnection(){
        if(isNetworkAvailable(this)){
            return true;
        }else{
            Toast.makeText(MainActivity.this, "Интернетіңізді тексерініз ", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
*/
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_today) {
            changeFragment(todayFragment);
        } else if (id == R.id.nav_personnel) {
            changeFragment(personnelListFragment);

        }else if (id == R.id.nav_college) {
            changeFragment(collegeListFragment);

        }else if (id == R.id.nav_lyceum) {
            changeFragment(lyceumListFragment);

        } else if (id == R.id.nav_orders) {
            changeFragment(orderFragment);

        } else if (id == R.id.nav_dayli_report) {
            changeFragment(dayliReportFragment);

        } else if (id == R.id.nav_monthly_report) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void changeFragment(Fragment cfragment){
        Fragment fragment = cfragment;
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();
    }
}
