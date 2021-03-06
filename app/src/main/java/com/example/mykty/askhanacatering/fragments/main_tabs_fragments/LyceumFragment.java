package com.example.mykty.askhanacatering.fragments.main_tabs_fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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
import android.widget.TextView;

import com.example.mykty.askhanacatering.R;
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

import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_INFO;
import static com.example.mykty.askhanacatering.database.StoreDatabase.TABLE_LYCEUM_STUDENTS;

public class LyceumFragment extends Fragment {
    View view;
    TextView tvDate;
    String date, firebaseDate;
    DateFormat dateF, dateFr;
    int breakfastCount, lunchCount, dinnerCount, poldnik1Count, poldnik2Count;
    int breakfastCount2, totalLyceumStuddents, dinnerCount2;

    PMenu breakfastMenu, dinnerMenu, lunchMenu, poldnik1, poldnik2;
    private static RecyclerView recyclerView;
    SQLiteDatabase sqdb;
    StoreDatabase storeDb;
    DatabaseReference mDatabaseRef;
    private RecyclerView.LayoutManager linearLayoutManager, gridLayoutManager;
    private static ArrayList<PMenu> menu;
    private static RecyclerView.Adapter adapter;
    ArrayList<String> breakfastList, lunchList, dinnerList, poldnik1List, poldnik2List;

    int lyceumBreakfastEatersInt, lyceumDinnerEatersInt;
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
        calcTotalLyceumStudents();
        refreshDayCount();
        collegeEatersCount();

        return view;
    }

    public void refreshDayCount() {
        mDatabaseRef.child("days").child("lyceum").child(firebaseDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    breakfastCount = 0;
                    lunchCount = 0;
                    dinnerCount = 0;
                    poldnik1Count = 0;
                    poldnik2Count = 0;

                    breakfastList.clear();
                    lunchList.clear();
                    dinnerList.clear();
                    poldnik2List.clear();
                    poldnik1List.clear();

                    for (DataSnapshot daysSnap : dataSnapshot.getChildren()) {

                        for (DataSnapshot foodTime : daysSnap.getChildren()) {

                            if (daysSnap.getKey().equals("breakfast")) {
                                breakfastCount++;
                                breakfastList.add(foodTime.getKey());
                            } else if (daysSnap.getKey().equals("lunch")) {
                                lunchCount++;
                                lunchList.add(foodTime.getKey());
                            } else if (daysSnap.getKey().equals("poldnik1")) {
                                poldnik1Count++;
                                poldnik1List.add(foodTime.getKey());
                            } else if (daysSnap.getKey().equals("poldnik2")) {
                                poldnik2Count++;
                                poldnik2List.add(foodTime.getKey());
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
        mDatabaseRef.child("f_time").child("breakfast").child("lyceum").child(firebaseDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    lyceumBreakfastEatersInt = (int) dataSnapshot.getChildrenCount();
                } else {
                    lyceumBreakfastEatersInt = 0;
                }

                updateViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabaseRef.child("f_time").child("dinner").child("lyceum").child(firebaseDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    lyceumDinnerEatersInt = (int) dataSnapshot.getChildrenCount();
                } else {
                    lyceumDinnerEatersInt = 0;
                }

                updateViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void updateViews() {
        breakfastMenu.setCount(breakfastCount + " / " + lyceumBreakfastEatersInt);

        lunchMenu.setCount(lunchCount + " / " + totalLyceumStuddents);
        poldnik1.setCount(poldnik1Count + " / " + totalLyceumStuddents);
        poldnik2.setCount(poldnik2Count + " / " + totalLyceumStuddents);

        dinnerMenu.setCount(dinnerCount + " / " + lyceumDinnerEatersInt);

        adapter.notifyDataSetChanged();
    }

    public void calcTotalLyceumStudents() {

        Cursor res = sqdb.rawQuery("SELECT " + COLUMN_INFO + " FROM " + TABLE_LYCEUM_STUDENTS, null);

        totalLyceumStuddents = res.getCount();
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

        menu = new ArrayList();
        breakfastMenu = new PMenu("Таңғы ас", R.drawable.menu1, "0");
        poldnik1 = new PMenu("Полдник 1", R.drawable.menu3, "0");
        lunchMenu = new PMenu("Түскі ас", R.drawable.menu3, "0");
        poldnik2 = new PMenu("Полдник 2", R.drawable.menu3, "0");
        dinnerMenu = new PMenu("Кешкі ас", R.drawable.menu2, "0");

        menu.add(breakfastMenu);
        menu.add(poldnik1);
        menu.add(lunchMenu);
        menu.add(poldnik2);
        menu.add(dinnerMenu);

        adapter = new PMenuListAdapter(menu);
        recyclerView.setAdapter(adapter);

        breakfastList = new ArrayList<>();
        lunchList = new ArrayList<>();
        dinnerList = new ArrayList<>();

        poldnik1List = new ArrayList<>();
        poldnik2List = new ArrayList<>();

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {

                        Intent intent = new Intent(getActivity(), ReportListActivity.class);
                        intent.putExtra("type", "lyceum");

                        if (position == 0 && breakfastList.size() != 0) {

                            intent.putExtra("f_time", "breakfast");
                            intent.putExtra("list", breakfastList);

                            startActivity(intent);
                        } else if (position == 1 && poldnik1List.size() != 0) {

                            intent.putExtra("f_time", "poldnik1");
                            intent.putExtra("list", poldnik1List);

                            startActivity(intent);
                        } else if (position == 2 && lunchList.size() != 0) {

                            intent.putExtra("f_time", "lunch");
                            intent.putExtra("list", lunchList);

                            startActivity(intent);
                        } else if (position == 3 && poldnik2List.size() != 0) {

                            intent.putExtra("f_time", "poldnik2");
                            intent.putExtra("list", poldnik2List);

                            startActivity(intent);
                        } else if (position == 4 && dinnerList.size() != 0) {

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