package com.example.mykty.askhanacatering.fragments.main_tabs_fragments;

import android.app.Dialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.example.mykty.askhanacatering.adapter.PMenuListAdapter;
import com.example.mykty.askhanacatering.database.StoreDatabase;
import com.example.mykty.askhanacatering.module.RecyclerItemClickListener;
import com.example.mykty.askhanacatering.module.PMenu;
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
    int breakfastCount2, lunchCount2, dinnerCount2;
    ArrayList<String> breakfastList;
    ArrayList<String> lunchList;
    ArrayList<String> dinnerList;
    PMenu breakfastMenu;
    PMenu dinnerMenu;
    PMenu lunchMenu;
    Dialog addNewGuestDialog;
    String mealType[] = {"breakfast", "lunch", "dinner"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_college, container, false);
        tvDate = view.findViewById(R.id.textView2);
        manageDate();
        setupViews();
        refreshDayCount();
        return view;
    }

    public void refreshDayCount() {
        mDatabaseRef.child("days").child("college").child(firebaseDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                breakfastCount = 0; lunchCount = 0; dinnerCount = 0;
                breakfastCount2 = 0; lunchCount2 = 0; dinnerCount2 = 0;

                for (DataSnapshot daysSnap : dataSnapshot.getChildren()) {

                    for (DataSnapshot foodTime : daysSnap.getChildren()){

                        if(daysSnap.getKey().equals("breakfast")){
                            breakfastCount++;
                            String id_number = foodTime.getKey();
                            Long value = (Long) daysSnap.child(id_number).getValue();
                            if(value==1){
                                breakfastCount2++;
                                breakfastList.add(id_number);
                            }
                        }else if(daysSnap.getKey().equals("lunch")){
                            lunchCount++;
                            String id_number = foodTime.getKey();
                            Long value = (Long) daysSnap.child(id_number).getValue();
                            if(value==1){
                                lunchCount2++;
                                lunchList.add(id_number);
                            }
                        }else if(daysSnap.getKey().equals("dinner")){
                            dinnerCount++;
                            String id_number = foodTime.getKey();
                            Long value = (Long) daysSnap.child(id_number).getValue();
                            if(value==1){
                                dinnerCount2++;
                                dinnerList.add(id_number);
                            }
                        }


                    }
                }
                updateViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateViews() {
        breakfastMenu.setCount(breakfastCount2+" / "+breakfastCount);
        lunchMenu.setCount(lunchCount2+" / "+lunchCount);
        dinnerMenu.setCount(dinnerCount2+" / "+dinnerCount);

        adapter.notifyDataSetChanged();
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
                        Intent intent = new Intent(getActivity(), ReportListActivity.class);
                        intent.putExtra("type", "college");

                        if(position==0 && breakfastList.size()!=0 ){
                            intent.putExtra("list", breakfastList);
                            startActivity(intent);

                        }else if(position==1 && lunchList.size()!=0 ){
                            intent.putExtra("list", lunchList);
                            startActivity(intent);

                        }else if(position==2 && dinnerList.size()!=0 ){
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
        FloatingActionButton fabNewEater = view.findViewById(R.id.fab_guest);

        fabNewEater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getActivity(), EnterNewEaterActivity.class));

            }
        });
    }

    EditText pInfo, pIdNumber;
    int pos = 1;

    public void createGuestDialog() {
        addNewGuestDialog = new Dialog(getActivity());
        addNewGuestDialog.setContentView(R.layout.dialog_add_guest);
        addNewGuestDialog.setTitle("Колледж студент енгізу");
        TextView today = addNewGuestDialog.findViewById(R.id.textViewToday);
        Button bntOK = addNewGuestDialog.findViewById(R.id.btnOk);
        pInfo = addNewGuestDialog.findViewById(R.id.pInfo);
        pIdNumber = addNewGuestDialog.findViewById(R.id.pIdNumber);

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

                if (pInfo.length() > 0 && pIdNumber.length() > 0) {
                    String info = pInfo.getText().toString();
                    String idNumber = pIdNumber.getText().toString();
                    String meal = mealType[pos];


                    mDatabaseRef.child("days").child("college").child(firebaseDate).child(meal).child(idNumber).setValue(0);

                    addNewGuestDialog.dismiss();
                    showSuccessToast(info);
                    pInfo.setText("");
                    pIdNumber.setText("");
                    refreshDayCount();

                } else {
                    if (isEmpty(pInfo.getText().toString())) pInfo.setError("Ақпарат толтырылмады");
                    if (isEmpty(pIdNumber.getText().toString())) pIdNumber.setError("Ақпарат толтырылмады");

                }
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

    public void manageDate() {
        dateF = new SimpleDateFormat("EEEE, dd.MM.yyyy");
        dateFr = new SimpleDateFormat("dd_MM");//2001.07.04
        date = dateF.format(Calendar.getInstance().getTime());

        firebaseDate = dateFr.format(Calendar.getInstance().getTime());
        firebaseDate = "23_04";

        tvDate.setText(date.replace('_', '.'));
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
}