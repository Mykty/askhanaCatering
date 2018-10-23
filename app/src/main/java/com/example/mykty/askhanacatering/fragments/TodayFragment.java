package com.example.mykty.askhanacatering.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.fragments.main_tabs_fragments.CollegeFragment;
import com.example.mykty.askhanacatering.fragments.main_tabs_fragments.LyceumFragment;
import com.example.mykty.askhanacatering.fragments.main_tabs_fragments.PersonnelFragment;
import com.example.mykty.askhanacatering.module.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TodayFragment extends Fragment {
    View view;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_today, container, false);

        viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        getActivity().setTitle("Бастысы");

        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new PersonnelFragment(), getString(R.string.personnel));
        adapter.addFragment(new CollegeFragment(), getString(R.string.college));
        adapter.addFragment(new LyceumFragment(), getString(R.string.lyceum));

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
    }
}
