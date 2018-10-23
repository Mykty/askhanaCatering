package com.example.mykty.askhanacatering.fragments.main_tabs_fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.activity.EnterNewEaterActivity;
import com.example.mykty.askhanacatering.activity.ReportListActivity;
import com.example.mykty.askhanacatering.activity.ScannerActivity;
import com.example.mykty.askhanacatering.adapter.PMenuListAdapter;
import com.example.mykty.askhanacatering.database.StoreDatabase;
import com.example.mykty.askhanacatering.module.RecyclerItemClickListener;
import com.example.mykty.askhanacatering.module.PMenu;
import com.example.mykty.askhanacatering.module.Student;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.text.TextUtils.isEmpty;

public class CollegeFragment extends Fragment {
    View view;
    TextView tvDate;
    String date, firebaseDate;
    DateFormat dateF, dateFr;
    private static RecyclerView recyclerView;
    SQLiteDatabase sqdb;
    StoreDatabase storeDb;
    DatabaseReference mDatabaseRef;
    private RecyclerView.LayoutManager linearLayoutManager, gridLayoutManager;
    private static ArrayList<PMenu> menu;
    private static RecyclerView.Adapter adapter;
    int breakfastCount, lunchCount, dinnerCount;
    ArrayList<String> breakfastList;
    ArrayList<String> lunchList;
    ArrayList<String> dinnerList;
    PMenu breakfastMenu;
    PMenu dinnerMenu;
    PMenu lunchMenu;
    Dialog buyFoodDialog;
    String mealType[] = {"breakfast", "lunch", "dinner"};
    int collegeBreakfastEatersInt, collegeLunchEatersInt, collegeDinnerEatersInt;
    FloatingActionButton buyFoodBtn;
    Student student;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_college, container, false);
        tvDate = view.findViewById(R.id.textViewTotal);

        manageDate();
        setupViews();
        refreshDayCount();
        collegeEatersCount();

        return view;
    }

    public void setupViews() {
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();
        buyFoodBtn = view.findViewById(R.id.fab_guest);

        FirebaseApp.initializeApp(getActivity());
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        linearLayoutManager = new LinearLayoutManager(getActivity());
        gridLayoutManager = new GridLayoutManager(getActivity(), 2);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        breakfastList = new ArrayList<>();
        lunchList = new ArrayList<>();
        dinnerList = new ArrayList<>();

        menu = new ArrayList();
        breakfastMenu = new PMenu("Таңғы ас", R.drawable.menu1, "0");
        lunchMenu = new PMenu("Түскі ас", R.drawable.menu3, "0");
        dinnerMenu = new PMenu("Кешкі ас", R.drawable.menu2, "0");

        menu.add(breakfastMenu);
        menu.add(lunchMenu);
        menu.add(dinnerMenu);

        adapter = new PMenuListAdapter(menu);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {
                        Intent intent = new Intent(getActivity(), ReportListActivity.class);
                        intent.putExtra("type", "college");
                        intent.putExtra("firebaseDate", "firebaseDate");

                        if (position == 0 && breakfastList.size() != 0) {
                            intent.putExtra("f_time", "breakfast");
                            intent.putExtra("list", breakfastList);
                            startActivity(intent);

                        } else if (position == 1 && lunchList.size() != 0) {
                            intent.putExtra("f_time", "lunch");
                            intent.putExtra("list", lunchList);
                            startActivity(intent);

                        } else if (position == 2 && dinnerList.size() != 0) {
                            intent.putExtra("f_time", "dinner");
                            intent.putExtra("list", dinnerList);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                })
        );

        createGuestDialog();
        buyFoodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buyFoodDialog.show();
            }
        });
    }

    public void refreshDayCount() {
        mDatabaseRef.child("days").child("college").child(firebaseDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    breakfastCount = 0;
                    lunchCount = 0;
                    dinnerCount = 0;

                    breakfastList.clear();
                    lunchList.clear();
                    dinnerList.clear();

                    for (DataSnapshot daysSnap : dataSnapshot.getChildren()) {
                        for (DataSnapshot foodTime : daysSnap.getChildren()) {

                            if (daysSnap.getKey().equals("breakfast")) {
                                breakfastCount++;
                                breakfastList.add(foodTime.getKey());

                            } else if (daysSnap.getKey().equals("lunch")) {
                                lunchCount++;
                                lunchList.add(foodTime.getKey());

                            } else if (daysSnap.getKey().equals("dinner")) {
                                dinnerCount++;
                                dinnerList.add(foodTime.getKey());
                            }
                        }
                    }
                    updateViews();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void collegeEatersCount() {
        mDatabaseRef.child("f_time").child("breakfast").child("college").child(firebaseDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    collegeBreakfastEatersInt = (int) dataSnapshot.getChildrenCount();
                } else {
                    collegeBreakfastEatersInt = 0;
                }

                updateViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mDatabaseRef.child("f_time").child("lunch").child("college").child(firebaseDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    collegeLunchEatersInt = (int) dataSnapshot.getChildrenCount();
                } else {
                    collegeLunchEatersInt = 0;
                }

                updateViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseRef.child("f_time").child("dinner").child("college").child(firebaseDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    collegeDinnerEatersInt = (int) dataSnapshot.getChildrenCount();
                } else {
                    collegeDinnerEatersInt = 0;
                }

                updateViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateViews() {
        breakfastMenu.setCount(breakfastCount + " / " + collegeBreakfastEatersInt);
        lunchMenu.setCount(lunchCount + " / " + collegeLunchEatersInt);
        dinnerMenu.setCount(dinnerCount + " / " + collegeDinnerEatersInt);

        adapter.notifyDataSetChanged();
    }

    Button selectStudent, bntOK, btnCancel;

    int pos = 1;

    public void createGuestDialog() {
        buyFoodDialog = new Dialog(getActivity(), R.style.CustomDialog);
        buyFoodDialog.setContentView(R.layout.dialog_buy_one_day_food);
        buyFoodDialog.setCanceledOnTouchOutside(false);
        buyFoodDialog.setTitle("1 күндің тамақ");
        bntOK = buyFoodDialog.findViewById(R.id.btnOk);
        btnCancel = buyFoodDialog.findViewById(R.id.btnCancel);
        selectStudent = buyFoodDialog.findViewById(R.id.selectStudent);

        final String[] SPINNERLIST = {"Таңғы ас", "Түскі ас", "Кешкі ас"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, SPINNERLIST);
        Spinner guestSpinner = buyFoodDialog.findViewById(R.id.guest_spinner);

        guestSpinner.setAdapter(arrayAdapter);
        guestSpinner.setSelection(1);

        guestSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        selectStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent t = new Intent(getActivity(), ScannerActivity.class);
                startActivityForResult(t, 101);
            }
        });

        bntOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(findedStudent){
                    String idNumber = student.getId_number();
                    String meal = mealType[pos];

                    mDatabaseRef.child("payed").child("college").child(idNumber).child(meal).child(firebaseDate).setValue(1);
                    mDatabaseRef.child("f_time").child(meal).child("college").child(firebaseDate).child(idNumber).push().setValue(1);
                    buyFoodDialog.dismiss();
                    showSuccessToast(student.getName());
                }else{
                    Toast.makeText(getActivity(), getString(R.string.selectStudentMistake), Toast.LENGTH_SHORT).show();
                }

                /*if (pInfo.length() > 0 && pIdNumber.length() > 0) {
                    String info = pInfo.getText().toString();
                    String idNumber = pIdNumber.getText().toString();
                    String meal = mealType[pos];


                    mDatabaseRef.child("days").child("college").child(firebaseDate).child(meal).child(idNumber).setValue(0);

                    buyFoodDialog.dismiss();
                    showSuccessToast(info);
                    pInfo.setText("");
                    pIdNumber.setText("");
                    refreshDayCount();

                } else {
                    if (isEmpty(pInfo.getText().toString())) pInfo.setError("Ақпарат толтырылмады");
                    if (isEmpty(pIdNumber.getText().toString()))
                        pIdNumber.setError("Ақпарат толтырылмады");

                }*/
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectStudent.setText(getString(R.string.select));
                buyFoodDialog.dismiss();
            }
        });

    }
    boolean findedStudent = false;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 101) {
            if(resultCode == Activity.RESULT_OK){
                Bundle bundle = data.getExtras();
                student = (Student) bundle.getSerializable("findedStudent");
                selectStudent.setText(student.getName());
                findedStudent = true;

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Студент табылған жоқ", Toast.LENGTH_SHORT).show();
                findedStudent = false;
            }
        }
    }

    public void showSuccessToast(String info) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) view.findViewById(R.id.custom_toast_layout));
        TextView text = toastLayout.findViewById(R.id.custom_toast_message);
        text.setText(info + " сәтті енгізілді!");

        Toast toast = new Toast(getActivity());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        toast.show();
    }

    public void manageDate() {
        dateF = new SimpleDateFormat("EEEE, dd.MM.yyyy");
        dateFr = new SimpleDateFormat("dd_MM_yyyy");//2001.07.04
        date = dateF.format(Calendar.getInstance().getTime());

        firebaseDate = dateFr.format(Calendar.getInstance().getTime());

        tvDate.setText(date);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.p_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.act_list) {
            recyclerView.setLayoutManager(linearLayoutManager);
            return true;
        }
        if (id == R.id.act_grid) {
            recyclerView.setLayoutManager(gridLayoutManager);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}