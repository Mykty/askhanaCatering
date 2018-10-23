package com.example.mykty.askhanacatering.fragments.main_tabs_fragments;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.example.mykty.askhanacatering.activity.MainActivity;
import com.example.mykty.askhanacatering.activity.ReportListActivity;
import com.example.mykty.askhanacatering.adapter.PMenuListAdapter;
import com.example.mykty.askhanacatering.database.StoreDatabase;
import com.example.mykty.askhanacatering.module.RecyclerItemClickListener;
import com.example.mykty.askhanacatering.module.PMenu;
import com.example.mykty.askhanacatering.module.Personnel;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_ID_NUMBER;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_INFO;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_PHOTO;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_TYPE;
import static com.example.mykty.askhanacatering.database.StoreDatabase.TABLE_PERSONNEL;

public class PersonnelFragment extends Fragment {
    View view;
    TextView tvDate;
    String date, firebaseDate;
    DateFormat dateF, firebaseDateFormat;
    private static RecyclerView recyclerView;
    SQLiteDatabase sqdb;
    StoreDatabase storeDb;
    DatabaseReference mDatabaseRef;
    private RecyclerView.LayoutManager linearLayoutManager, gridLayoutManager;
    private static ArrayList<PMenu> menu;
    private static RecyclerView.Adapter adapter;
    int breakfastCount = 0, lunchCount = 0, dinnerCount = 0;
    int totalBreakfast, totalPersonnel, totalDinner;

    PMenu breakfastMenu;//завтрак
    PMenu lunchMenu; //обед
    PMenu dinnerMenu; //ужин

    ArrayList<String> breakfastList;
    ArrayList<String> lunchList;
    ArrayList<String> dinnerList;
    Dialog addNewGuestDialog;

    String types[] = {"volunteer","worker","volunteer"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_lyceum, container, false);
        tvDate = view.findViewById(R.id.textView2);

        manageDate();
        setupViews();
        personnelListCount();
        refreshDayCount();

        return view;
    }

    int count = 0;
    public void refreshDayCount() {
        mDatabaseRef.child("days").child("personnel").child(firebaseDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("count","count: "+count);
                count++;

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

    public void updateViews() {
        breakfastMenu.setCount(breakfastCount + " / " + totalBreakfast);
        lunchMenu.setCount(lunchCount + " / " + totalPersonnel);
        dinnerMenu.setCount(dinnerCount + " / " + totalDinner);

        adapter.notifyDataSetChanged();
    }

    public void personnelListCount() {

        totalPersonnel = 0;
        Cursor res = sqdb.rawQuery("SELECT " + COLUMN_INFO + " FROM " + TABLE_PERSONNEL, null);
        while (res.moveToNext()) totalPersonnel++;

        res.close();
        tarbiewiListCount();
    }
    String TABLE_PERSONNEL = "personnel_store";

    public void tarbiewiListCount() {

        totalBreakfast = 0;
        totalDinner = 0;

        String[] params = new String[]{"volunteer"};
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_PERSONNEL + " WHERE type=? ORDER BY info", params);
        while (res.moveToNext()) totalBreakfast++;

        totalDinner = totalBreakfast;

        res.close();
        updateViews();

    }

    public void setupViews() {
        recyclerView = view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

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
                        if(checkInetConnection()){
                            Intent intent = new Intent(getActivity(), ReportListActivity.class);
                            intent.putExtra("type", "personnel");
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
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                })
        );

        createGuestDialog();
        /*
        FloatingActionButton fabGuest = view.findViewById(R.id.fab_guest);

        fabGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkInetConnection()) addNewGuestDialog.show();
            }
        });
        */
    }

    EditText pInfo, pCardNumber;
    int pos = 1;

    public void createGuestDialog() {

        addNewGuestDialog = new Dialog(getActivity());
        addNewGuestDialog.setContentView(R.layout.dialog_add_guest);
        addNewGuestDialog.setTitle("Қонақ енгізу");
        TextView today = addNewGuestDialog.findViewById(R.id.textViewToday);
        Button bntOK = addNewGuestDialog.findViewById(R.id.btnOk);
        pInfo = addNewGuestDialog.findViewById(R.id.pInfo);
        pCardNumber = addNewGuestDialog.findViewById(R.id.pIdNumber);

        final String[] SPINNERLIST = {"Таңғы ас", "Түскі ас", "Кешкі ас"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, SPINNERLIST);
        Spinner guestSpinner = addNewGuestDialog.findViewById(R.id.guest_spinner);

        today.setText(date);
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

        bntOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pInfo.length() > 0 && pCardNumber.length() > 0) {
                    String info = pInfo.getText().toString();
                    String cardNumber = pCardNumber.getText().toString();
                    String type = "others";

                    /*

                    String newKey = mDatabaseRef.child("personnel_store").child("store").push().getKey();

                    Personnel personnel = new Personnel("" + info, ""+newKey, idNumber, otherDescText, type);
                    mDatabaseRef.child("personnel_store").child("store").child(newKey).setValue(personnel);
                    refreshPersonnels();

                     */


                    String newKey = mDatabaseRef.child("personnel_store").child("store").push().getKey();
                    Personnel personnel = new Personnel("" + info, newKey, cardNumber, "Қонақ", type);


                    mDatabaseRef.child("personnel_store").child("store").child(newKey).setValue(personnel);

                    addNewGuestDialog.dismiss();
                    showSuccessToast(info);
                    pInfo.setText("");
                    pCardNumber.setText("");

                    incrementVersion();
                    refreshDayCount();

                } else {
                    if (isEmpty(pInfo.getText().toString())) pInfo.setError("Ақпарат толтырылмады");
                    if (isEmpty(pCardNumber.getText().toString())) pCardNumber.setError("Ақпарат толтырылмады");

                }
            }
        });
    }

    public void incrementVersion() {
        Query myTopPostsQuery = mDatabaseRef.child("personnel_ver");
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    long version = (long) dataSnapshot.getValue();
                    version++;
                    mDatabaseRef.child("personnel_ver").setValue(version);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void showSuccessToast(String info){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) view.findViewById(R.id.custom_toast_layout));
        TextView text = toastLayout.findViewById(R.id.custom_toast_message);
        text.setText(info+" сәтті енгізілді!");

        Toast toast = new Toast(getActivity());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        toast.show();
    }

    public boolean isEmpty(String text) {
        return TextUtils.isEmpty(text);
    }

    public void manageDate() {
        dateF = new SimpleDateFormat("EEEE, dd.MM.yyyy");
        date = dateF.format(Calendar.getInstance().getTime());

        firebaseDateFormat = new SimpleDateFormat("dd_MM_yyyy");//2001.07.04
        firebaseDate = firebaseDateFormat.format(Calendar.getInstance().getTime());
        //firebaseDate = "23_04";

        tvDate.setText(date);
//        tvDate.setText(firebaseDate);
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