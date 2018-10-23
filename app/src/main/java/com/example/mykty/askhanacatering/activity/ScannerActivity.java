package com.example.mykty.askhanacatering.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.database.StoreDatabase;
import com.example.mykty.askhanacatering.module.Student;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    String TABLE_COLLEGE_STUDENTS = "college_students_list";
    TextView studentName;
    Button btOk;
    ImageView imageV;
    Dialog dialog;
    HashMap<String, Integer> checkerHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scannerView = new ZXingScannerView(this);
        setContentView(scannerView);
        int currentApiVersion = Build.VERSION.SDK_INT;

        if (currentApiVersion >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                Toast.makeText(getApplicationContext(), "Permission already granted!", Toast.LENGTH_LONG).show();
            } else {
                requestPermission();
            }
        }
        checkPermission();
//        requestPermission();

        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.scann_res);

        imageV = dialog.findViewById(R.id.imageV);

        btOk = dialog.findViewById(R.id.buttonOk);
        Button btCancel = dialog.findViewById(R.id.buttonCancel);
        studentName = dialog.findViewById(R.id.sName);
        btOk.setEnabled(false);
        checkerHashMap = new HashMap<>();

    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onResume() {
        super.onResume();

        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if (scannerView == null) {
                    scannerView = new ZXingScannerView(this);
                    setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            } else {
                requestPermission();
            }
        }else{
            scannerView.setResultHandler(this);
            scannerView.startCamera();

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(ScannerActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void handleResult(Result result) {
        final String myResult = result.getText().toString();

        findStudent(myResult);

        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                scannerView.resumeCameraPreview(ScannerActivity.this);
//
//                dialog.dismiss();
//                scannerView.stopCamera();
//
//                Intent intent = getIntent();
//                Bundle bundle = new Bundle();
//                bundle.putSerializable("findedStudent", student);
//                intent.putExtras(bundle);
//
//                setResult(Activity.RESULT_OK, intent);
//                finish();
            }
        });
    }

    public Cursor getStudentByQrCode(String qr_code) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_COLLEGE_STUDENTS + " WHERE qr_code=?", new String[]{qr_code});
        return res;
    }

    Student student;
    Intent intent;

    public void findStudent(String qr_code) {
        Cursor res = getStudentByQrCode(qr_code);

        if (((res != null) && (res.getCount() > 0))) {
            res.moveToNext();

            String qr_code_str = res.getString(1);
            String name = res.getString(2);
            String id_number = res.getString(3);
            String card_number = res.getString(4);
            String photo = res.getString(6);

            student = new Student(name, id_number, card_number, photo, qr_code_str);
            scannerView.resumeCameraPreview(ScannerActivity.this);
            scannerView.stopCamera();

            intent = getIntent();
            Bundle bundle = new Bundle();
            bundle.putSerializable("findedStudent", student);
            intent.putExtras(bundle);

            setResult(Activity.RESULT_OK, intent);
            finish();

        } else {
            setResult(Activity.RESULT_CANCELED, intent);
            finish();
        }
    }
}