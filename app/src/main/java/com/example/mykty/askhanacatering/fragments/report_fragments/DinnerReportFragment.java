package com.example.mykty.askhanacatering.fragments.report_fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.module.GroupDataItem;
import com.example.mykty.askhanacatering.module.StudentsItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class DinnerReportFragment extends Fragment {
    View view;
    DatabaseReference mDatabaseRef;
    RecyclerView recyclerView;
    RecyclerDataAdapter recyclerDataAdapter;
    ArrayList<GroupDataItem> groupsList;
    HashMap<String, Long> personnelDaysR, collegeDaysR, lyceumDaysR;
    HashMap<String, Long> maxStore;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dayli_report, container, false);
        setupViews();
        getDays();
        return view;
    }

    public void setupViews() {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        recyclerView = view.findViewById(R.id.recycler_view);
        groupsList = new ArrayList<>();
        maxStore = new HashMap<>();
        personnelDaysR = new HashMap<>();
        collegeDaysR = new HashMap<>();
        lyceumDaysR = new HashMap<>();
    }

    public void getDays() {
        Query query = mDatabaseRef.child("days");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    groupsList.clear();

                    for (DataSnapshot organization : dataSnapshot.getChildren()) {
                        String oName = organization.getKey().toString();

                        maxStore.put(oName, organization.getChildrenCount());

                        for (DataSnapshot days : organization.getChildren()) {
                            String day = days.getKey().toString();
                            getReportByDay(oName, day);
                        }
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getReportByDay(final String oName, final String day) {
        Query query = mDatabaseRef.child("days").child(oName).child(day).child("dinner");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    long size = dataSnapshot.getChildrenCount();

                    if (oName.equals("personnel")) personnelDaysR.put(day, size);
                    else if (oName.equals("college")) collegeDaysR.put(day, size);
                    else if (oName.equals("lyceum")) lyceumDaysR.put(day, size);

                    if (personnelDaysR.size() != 0 && maxStore.get("personnel") == personnelDaysR.size() &&
                            collegeDaysR.size() != 0 && maxStore.get("college") == collegeDaysR.size() &&
                            lyceumDaysR.size() != 0 && maxStore.get("lyceum") == lyceumDaysR.size())

                        modifyAdapter();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void modifyAdapter() {

        ArrayList<StudentsItem> childStoreNewJobs;
        GroupDataItem daysGroup;
        String perStr = " Персонал: ";
        String colStr = " Колледж: ";
        String lycStr = " Лицей: ";

        for (String date : personnelDaysR.keySet()) {

            childStoreNewJobs = new ArrayList<>();

            String childName = perStr + personnelDaysR.get(date);

            childName += "\n" + colStr;

            if ((collegeDaysR.containsKey(date))) {
                childName += collegeDaysR.get(date);
                collegeDaysR.remove(date);
            } else
                childName += 0;

            childName += "\n" + lycStr;

            if ((lyceumDaysR.containsKey(date))) {
                childName += lyceumDaysR.get(date);
                lyceumDaysR.remove(date);
            } else
                childName += 0;

            childStoreNewJobs.add(new StudentsItem("" + childName));

            daysGroup = new GroupDataItem(childStoreNewJobs);
            daysGroup.setParentName(date);
            groupsList.add(daysGroup);
        }

        for (String date : collegeDaysR.keySet()) {

            childStoreNewJobs = new ArrayList<>();

            String childName = perStr;

            childName += (personnelDaysR.containsKey(date)) ? personnelDaysR.get(date) : 0;

            childName += "\n" + colStr + collegeDaysR.get(date);

            childName += "\n" + lycStr;

            if ((lyceumDaysR.containsKey(date))) {
                childName += lyceumDaysR.get(date);
                lyceumDaysR.remove(date);
            } else
                childName += 0;


            childStoreNewJobs.add(new StudentsItem("" + childName));

            daysGroup = new GroupDataItem(childStoreNewJobs);
            daysGroup.setParentName(date);
            groupsList.add(daysGroup);
        }

        for (String date : lyceumDaysR.keySet()) {

            childStoreNewJobs = new ArrayList<>();

            String childName = perStr;

            childName += (personnelDaysR.containsKey(date)) ? personnelDaysR.get(date) : 0;

            childName += "\n" + colStr;
            childName += (collegeDaysR.containsKey(date)) ? collegeDaysR.get(date) : 0;

            childName += "\n" + lycStr + lyceumDaysR.get(date);

            childStoreNewJobs.add(new StudentsItem("" + childName));

            daysGroup = new GroupDataItem(childStoreNewJobs);
            daysGroup.setParentName(date);
            groupsList.add(daysGroup);
        }


        recyclerDataAdapter = new RecyclerDataAdapter(groupsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(recyclerDataAdapter);
        recyclerView.setHasFixedSize(true);
    }


    private class RecyclerDataAdapter extends RecyclerView.Adapter<RecyclerDataAdapter.MyViewHolder> {
        private ArrayList<GroupDataItem> dummyParentDataItems;

        RecyclerDataAdapter(ArrayList<GroupDataItem> dummyParentDataItems) {
            this.dummyParentDataItems = dummyParentDataItems;
        }

        @Override
        public RecyclerDataAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parent_child_listing2, parent, false);
            return new RecyclerDataAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerDataAdapter.MyViewHolder holder, int position) {
            GroupDataItem dummyParentDataItem = dummyParentDataItems.get(position);
            holder.textView_parentName.setText(dummyParentDataItem.getParentName());

            int noOfChildTextViews = holder.linearLayout_childItems.getChildCount();
            int noOfChild = dummyParentDataItem.getChildDataItems().size();


            if (noOfChild < noOfChildTextViews) {
                for (int index = noOfChild; index < noOfChildTextViews; index++) {
                    TextView currentTextView = (TextView) holder.linearLayout_childItems.getChildAt(index);
                    currentTextView.setVisibility(View.GONE);
                }
            }
            int count = 0;
            String[] splitStore = null;

            for (int textViewIndex = 0; textViewIndex < noOfChild; textViewIndex++) {
                TextView currentTextView = (TextView) holder.linearLayout_childItems.getChildAt(textViewIndex);
                String childName = dummyParentDataItem.getChildDataItems().get(textViewIndex).getChildName();
                currentTextView.setText(childName);
                splitStore = childName.split(" ");//1,3,5

                count = Integer.parseInt(splitStore[2].trim()) + Integer.parseInt(splitStore[4].trim()) + Integer.parseInt(splitStore[6].trim());
            }

            holder.tvCount.setText("" + count);
        }

        @Override
        public int getItemCount() {
            return dummyParentDataItems.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private Context context;
            private TextView textView_parentName, tvCount;
            private LinearLayout linearLayout_childItems;

            MyViewHolder(View itemView) {
                super(itemView);
                context = itemView.getContext();
                textView_parentName = itemView.findViewById(R.id.tv_parentName);
                tvCount = itemView.findViewById(R.id.tv_count);
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
                    textView.setTextSize(16.0f);
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

                    TextView textViewClicked = (TextView) view;

                }
            }
        }
    }

}

                /*
                groupsList = new ArrayList<>();

                GroupDataItem oneDay;

                String foodType = "";

                for (DataSnapshot daysSnapshot : dataSnapshot.getChildren()) {
                    String f = daysSnapshot.getKey().toString();
                    foodType += f;
                }

                textView.setText(foodType);*/

//            long collegeCount = 0, lyceumCount = 0;

//
//            childName  = childName+"\n"+"College: "+collegeCount;
//
//            if(lyceumDaysR.containsKey(date)) childName  = childName+"\n"+"Lyceum: "+lyceumDaysR.get(date);