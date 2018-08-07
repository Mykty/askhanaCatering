package com.example.mykty.askhanacatering.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.fragments.main_tabs_fragments.CollegeFragment;
import com.example.mykty.askhanacatering.fragments.main_tabs_fragments.LyceumFragment;
import com.example.mykty.askhanacatering.fragments.main_tabs_fragments.PersonnelFragment;
import com.example.mykty.askhanacatering.module.Order;
import com.example.mykty.askhanacatering.module.ViewPagerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DayliReportFragment extends Fragment {
    View view;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    ViewPagerAdapter adapter;
    DatabaseReference mDatabaseRef;
    static TextView textView;
    ArrayList<String> daysStore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_today, container, false);

        setupViews();
        getDays();

        return view;
    }

    public void setupViews() {
        getActivity().setTitle(getResources().getString(R.string.dayli_report));
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        daysStore = new ArrayList<>();
        viewPager = view.findViewById(R.id.viewpager);
        adapter = new ViewPagerAdapter(getChildFragmentManager());

        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.i("info", "T: " + daysStore.get(position));

                getReportByDay(daysStore.get(position));

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

    }

    public void getReportByDay(String day) {
        Query query = mDatabaseRef.child("days").child("personnel").child(day);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String foodType = "";

                for (DataSnapshot daysSnapshot : dataSnapshot.getChildren()) {
                    String f = daysSnapshot.getKey().toString();
                    foodType += f;
                }

                textView.setText(foodType);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getDays() {
        Query query = mDatabaseRef.child("days").child("personnel");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot daysSnapshot : dataSnapshot.getChildren()) {
                    String days = daysSnapshot.getKey().toString();

                    daysStore.add(days);
                    daysStore.add(days);
                    days = days.replace("_", ".");
                    adapter.addFragment(new DayliReportFragmentItem(), "" + days);
                    adapter.addFragment(new DayliReportFragmentItem(), "" + days);
                }
                setupViewPager();
                getReportByDay(daysStore.get(0));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setupViewPager() {
//        adapter.addFragment(new PersonnelFragment(), "07.08");
//        adapter.addFragment(new PersonnelFragment(), "06.08");
//        adapter.addFragment(new PersonnelFragment(), "05.08");
//        adapter.addFragment(new PersonnelFragment(), "04.08");
        //adapter.addFragment(new CollegeFragment(), "Колледж");
        //adapter.addFragment(new LyceumFragment(), "Лицей");

        viewPager.setAdapter(adapter);
    }

    public static class DayliReportFragmentItem extends Fragment {
        View view;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            view = inflater.inflate(R.layout.fragment_dayli_report, container, false);
            textView = view.findViewById(R.id.tview);

            return view;
        }
    }
}
