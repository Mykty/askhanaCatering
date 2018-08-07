package com.example.mykty.askhanacatering.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.activity.ReportListActivity;
import com.example.mykty.askhanacatering.database.StoreDatabase;
import com.example.mykty.askhanacatering.module.GroupDataItem;
import com.example.mykty.askhanacatering.module.PMenu;
import com.example.mykty.askhanacatering.module.RecyclerItemClickListener;
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

public class StudentListFragment extends Fragment {
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
    PMenu breakfastMenu;
    PMenu dinnerMenu;
    PMenu lunchMenu;

    public void setupViews() {
        recyclerView = view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        FirebaseApp.initializeApp(getActivity());
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        linearLayoutManager = new LinearLayoutManager(getActivity());
        gridLayoutManager = new GridLayoutManager(getActivity(), 2);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        /*RecyclerDataAdapter recyclerDataAdapter = new RecyclerDataAdapter(getGroupsData());
        recyclerView.setAdapter(recyclerDataAdapter);
        recyclerView.setHasFixedSize(true);*/

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
    }

    public void manageDate() {
        dateF = new SimpleDateFormat("EEEE, dd_MM_yyyy");
        dateFr = new SimpleDateFormat("dd_MM");//2001.07.04
        date = dateF.format(Calendar.getInstance().getTime());

        firebaseDate = dateFr.format(Calendar.getInstance().getTime());
        firebaseDate = "23_04";

        tvDate.setText(date.replace('_', '.'));
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

    private class RecyclerDataAdapter extends RecyclerView.Adapter<RecyclerDataAdapter.MyViewHolder> {
        private ArrayList<GroupDataItem> dummyParentDataItems;

        RecyclerDataAdapter(ArrayList<GroupDataItem> dummyParentDataItems) {
            this.dummyParentDataItems = dummyParentDataItems;
        }

        @Override
        public RecyclerDataAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parent_child_listing, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerDataAdapter.MyViewHolder holder, int position) {
            GroupDataItem dummyParentDataItem = dummyParentDataItems.get(position);
            holder.textView_parentName.setText(dummyParentDataItem.getParentName());
            //
            int noOfChildTextViews = holder.linearLayout_childItems.getChildCount();
            int noOfChild = dummyParentDataItem.getChildDataItems().size();
            if (noOfChild < noOfChildTextViews) {
                for (int index = noOfChild; index < noOfChildTextViews; index++) {
                    TextView currentTextView = (TextView) holder.linearLayout_childItems.getChildAt(index);
                    currentTextView.setVisibility(View.GONE);
                }
            }
            for (int textViewIndex = 0; textViewIndex < noOfChild; textViewIndex++) {
                TextView currentTextView = (TextView) holder.linearLayout_childItems.getChildAt(textViewIndex);
                currentTextView.setText(dummyParentDataItem.getChildDataItems().get(textViewIndex).getChildName());
/*
                if(latecomersStore.contains(currentTextView.getText().toString())){
                    currentTextView.setBackgroundColor(getResources().getColor(R.color.red));
                }
                */
            }
        }

        @Override
        public int getItemCount() {
            return dummyParentDataItems.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private Context context;
            private TextView textView_parentName;
            private LinearLayout linearLayout_childItems;

            MyViewHolder(View itemView) {
                super(itemView);
                context = itemView.getContext();
                textView_parentName = itemView.findViewById(R.id.tv_parentName);
                linearLayout_childItems = itemView.findViewById(R.id.ll_child_items);
                linearLayout_childItems.setVisibility(View.GONE);
                int intMaxNoOfChild = 0;

                for (int index = 0; index < dummyParentDataItems.size(); index++) {
                    int intMaxSizeTemp = dummyParentDataItems.get(index).getChildDataItems().size();
                    if (intMaxSizeTemp > intMaxNoOfChild) intMaxNoOfChild = intMaxSizeTemp;
                }
                for (int indexView = 0; indexView < intMaxNoOfChild; indexView++) {
                    TextView textView = new TextView(context);
                    textView.setId(indexView);
                    textView.setPadding(20, 20, 0, 20);
                    textView.setTextSize(20.0f);
                    //textView.setGravity(Gravity.CENTER);
                    textView.setBackground(ContextCompat.getDrawable(context, R.drawable.background_sub_module_text));
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);//LinearLayout.LayoutParams.WRAP_CONTENT);
                    textView.setOnClickListener(this);

                    linearLayout_childItems.addView(textView, layoutParams);
                }
                textView_parentName.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.tv_parentName) {
                    if (linearLayout_childItems.getVisibility() == View.VISIBLE) {
                        linearLayout_childItems.setVisibility(View.GONE);
                    } else {
                        linearLayout_childItems.setVisibility(View.VISIBLE);
                    }
                } else {
                    /*
                    if(isNetworkAvailable(GroupsRecyclerActivity.this)){

                        textViewClicked = (TextView) view;
                        textViewClicked.setBackgroundColor(getResources().getColor(R.color.red));
                        clickedSName = textViewClicked.getText().toString();

                        if(!latecomersStore.contains(clickedSName)){
                            createResDialog();
                        }
                    }else{
                        Toast.makeText(GroupsRecyclerActivity.this, "Check internet connection", Toast.LENGTH_LONG).show();
                    }
                    */
                }
            }
        }
    }


}