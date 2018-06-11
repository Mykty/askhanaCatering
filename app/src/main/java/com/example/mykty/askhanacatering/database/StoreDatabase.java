package com.example.mykty.askhanacatering.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StoreDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "askhana_catering.db";
    private static final int DATABASE_VERSION = 6;

    public static final String TABLE_PERSONNEL = "personnel_store";
    public static final String COLUMN_INFO = "info";
    public static final String COLUMN_ID_NUMBER = "id_number";
    public static final String COLUMN_PHOTO = "photo";
    public static final String COLUMN_TYPE = "type";

    public static final String TABLE_COLLEGE_STUDENTS = "college_students_list";
    public static final String TABLE_LYCEUM_STUDENTS = "lyceum_students_list";
    public static final String COLUMN_Q_ID = "qr_code";
    public static final String COLUMN_GROUP = "s_group";

    public static final String TABLE_PERSONNEL_COUNT = "personnel_store_count";
    public static final String COLUMN_TOTAL_COUNT = "totalC";
    public static final String COLUMN_TEACHER_COUNT = "teacherC";
    public static final String COLUMN_WORKER_COUNT = "workerC";
    public static final String COLUMN_VOLUNTEER_COUNT = "volunteerC";
    public static final String COLUMN_OTHER_COUNT = "others";

    public static final String TABLE_VER = "versions";
    public static final String COLUMN_PERSONNEL_VER = "personnel_ver";
    public static final String COLUMN_STUDENTS_VER = "college_student_list_ver";
    public static final String COLUMN_LYCEUM_VER = "lyceum_student_list_ver";

    public static final String COLUMN_COLLEGE_DAY_VER = "college_day_ver";
    public static final String COLUMN_LYCEUM_DAY_VER = "lyceum_day_ver";

    Context context;

    public StoreDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_PERSONNEL + "(" +
                COLUMN_INFO + " TEXT, " +
                COLUMN_ID_NUMBER + " TEXT, " +
                COLUMN_PHOTO + " TEXT, " +
                COLUMN_TYPE + " TEXT )");

        db.execSQL("CREATE TABLE " + TABLE_COLLEGE_STUDENTS + "(" +
                COLUMN_Q_ID + " TEXT, " +
                COLUMN_INFO + " TEXT, " +
                COLUMN_ID_NUMBER + " TEXT, " +
                COLUMN_GROUP + " TEXT, " +
                COLUMN_PHOTO  + " INTEGER )");

        db.execSQL("CREATE TABLE " + TABLE_LYCEUM_STUDENTS + "(" +
                COLUMN_Q_ID + " TEXT, " +
                COLUMN_INFO + " TEXT, " +
                COLUMN_ID_NUMBER + " TEXT, " +
                COLUMN_GROUP + " TEXT, " +
                COLUMN_PHOTO  + " INTEGER )");

        db.execSQL("CREATE TABLE " + TABLE_PERSONNEL_COUNT + "(" +
                COLUMN_TOTAL_COUNT + " TEXT, " +
                COLUMN_TEACHER_COUNT + " TEXT, " +
                COLUMN_WORKER_COUNT + " TEXT, " +
                COLUMN_VOLUNTEER_COUNT + " TEXT, " +
                COLUMN_OTHER_COUNT + " TEXT )");

        db.execSQL("CREATE TABLE "+TABLE_VER+"("+
                COLUMN_PERSONNEL_VER + " TEXT, " +
                COLUMN_STUDENTS_VER + " TEXT, " +
                COLUMN_LYCEUM_VER + " TEXT, " +
                COLUMN_COLLEGE_DAY_VER + " TEXT, " +
                COLUMN_LYCEUM_DAY_VER + " TEXT )");


        addVersions(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSONNEL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLLEGE_STUDENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LYCEUM_STUDENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERSONNEL_COUNT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VER);

        onCreate(db);
    }

    public void cleanPersonnel(SQLiteDatabase db) {
        db.execSQL("delete from " + TABLE_PERSONNEL);

    }

    public void cleanCollegeStudentsTable(SQLiteDatabase db){
        db.execSQL("delete from "+ TABLE_COLLEGE_STUDENTS);

    }
    public void cleanLyceumStudentsTable(SQLiteDatabase db){
        db.execSQL("delete from "+ TABLE_LYCEUM_STUDENTS);

    }
    public void cleanPersonnelCount(SQLiteDatabase db) {
        db.execSQL("delete from " + TABLE_PERSONNEL_COUNT);

    }

    public void cleanVersions(SQLiteDatabase db) {
        db.execSQL("delete from " + TABLE_VER);

    }
    public void addVersions(SQLiteDatabase db) {
        ContentValues versionValues = new ContentValues();
        versionValues.put(COLUMN_PERSONNEL_VER, "0");
        versionValues.put(COLUMN_STUDENTS_VER, "0");
        versionValues.put(COLUMN_LYCEUM_VER, "0");
        versionValues.put(COLUMN_COLLEGE_DAY_VER, "0");
        versionValues.put(COLUMN_LYCEUM_DAY_VER, "0");

        db.insert(TABLE_VER, null, versionValues);
    }
}
