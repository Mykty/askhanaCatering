package com.example.mykty.askhanacatering.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.activity.DayliReportList;
import com.example.mykty.askhanacatering.fragments.report_fragments.BreakfastReportFragment;
import com.example.mykty.askhanacatering.fragments.report_fragments.DinnerReportFragment;
import com.example.mykty.askhanacatering.fragments.report_fragments.LunchReportFragment;
import com.example.mykty.askhanacatering.module.MultiSpinner;
import com.example.mykty.askhanacatering.module.ViewPagerAdapter;
import com.squareup.timessquare.CalendarPickerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.squareup.timessquare.CalendarPickerView.SelectionMode.MULTIPLE;
import static com.squareup.timessquare.CalendarPickerView.SelectionMode.RANGE;

public class DayliReportFragment extends Fragment implements View.OnClickListener {
    View view;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_today, container, false);

        setupViews();

        return view;
    }

    public void setupViews() {
        getActivity().setTitle(getResources().getString(R.string.dayli_report));
        viewPager = view.findViewById(R.id.viewpager);

        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setupViewPager();
        createDateDialogs();

    }

    Dialog repoDialog, repoDialogCalendar;

    private MultiSpinner spinnerPersons, spinnerTime;
    private ArrayAdapter<String> personsStoreAdapter, foodTimeStoreAdapter;
    String[] pStore, fTimeStore1, fTimeStore2;
    Button selectDaysBtn, daysSelectedBtn, scrollToTodayBtn, btnReporSubmit;
    Date minDate, today;
    CalendarPickerView calendar;
    List<Date> dates;
    ArrayList<String> datesSelected;
    SimpleDateFormat formatStr, formatStr2;
    public void createDateDialogs() {

        repoDialog = new Dialog(getActivity());
        repoDialog.setContentView(R.layout.dialog_report);
        repoDialog.setTitle(getResources().getString(R.string.dayli_report_title));
        btnReporSubmit = repoDialog.findViewById(R.id.btnReporSubmit);

        repoDialogCalendar = new Dialog(getActivity());
        repoDialogCalendar.setContentView(R.layout.dialog_report_calendar);

        datesSelected = new ArrayList<>();
        daysSelectedBtn = repoDialogCalendar.findViewById(R.id.okBtn);
        scrollToTodayBtn = repoDialogCalendar.findViewById(R.id.btnToday);

        daysSelectedBtn.setOnClickListener(this);
        scrollToTodayBtn.setOnClickListener(this);
        btnReporSubmit.setOnClickListener(this);

        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        minDate = new Date(2018 - 1900, 7, 1);
        today = new Date();
        today.getTime();

        calendar = repoDialogCalendar.findViewById(R.id.calendar_view);
        calendar.init(minDate, nextYear.getTime()).inMode(MULTIPLE);

        selectDaysBtn = repoDialog.findViewById(R.id.selectDays);
        selectDaysBtn.setOnClickListener(this);

        pStore = getResources().getStringArray(R.array.personsStore);
        fTimeStore1 = getResources().getStringArray(R.array.foodTimeStore1);
        fTimeStore2 = getResources().getStringArray(R.array.foodTimeStore2);

        personsStoreAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, pStore);
        foodTimeStoreAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, fTimeStore1);

        spinnerPersons = repoDialog.findViewById(R.id.spinnerPersons);
        spinnerPersons.setAdapter(personsStoreAdapter, false, onPersonSelectedListener);
        spinnerPersons.setAllSelectedDisplayMode(MultiSpinner.AllSelectedDisplayMode.DisplayAllItems);

        spinnerTime = repoDialog.findViewById(R.id.spinnerTime);
        spinnerTime.setAdapter(foodTimeStoreAdapter, false, onTimeSelectedListener);
        spinnerTime.setAllSelectedDisplayMode(MultiSpinner.AllSelectedDisplayMode.DisplayAllItems);


        formatStr = new SimpleDateFormat("EEEE dd.MM");
        formatStr2 = new SimpleDateFormat("dd_MM");

        /*boolean[] selectedItems = new boolean[adapter.getCount()];
        selectedItems[1] = true; // select second item
        spinner.setSelected(selectedItems);
        */
    }

    boolean [] personSelected, foodTimeSelected;
    private MultiSpinner.MultiSpinnerListener onPersonSelectedListener = new MultiSpinner.MultiSpinnerListener() {
        public void onItemsSelected(boolean[] selected) {

            if (selected[2]) {
                foodTimeStoreAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, fTimeStore2);
            } else {
                foodTimeStoreAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, fTimeStore1);
            }

            personSelected= selected;

            spinnerPersons.setSelected(selected);
            spinnerTime.setAdapter(foodTimeStoreAdapter, false, onTimeSelectedListener);

        }
    };

    private MultiSpinner.MultiSpinnerListener onTimeSelectedListener = new MultiSpinner.MultiSpinnerListener() {
        public void onItemsSelected(boolean[] selected) {
            spinnerTime.setSelected(selected);
            foodTimeSelected = selected;
        }
    };

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(new BreakfastReportFragment(), getResources().getString(R.string.title_breakfast));
        adapter.addFragment(new LunchReportFragment(), getResources().getString(R.string.title_lunch));
        adapter.addFragment(new DinnerReportFragment(), getResources().getString(R.string.title_dinner));
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.report_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.report) {
            repoDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.selectDays:

                repoDialogCalendar.show();

                break;

            case R.id.btnToday:

                calendar.scrollToDate(today);

                break;

            case R.id.okBtn:
                dates = calendar.getSelectedDates();
                datesSelected.clear();

                String datesStr = "";
                for (Date d : dates) {
                    String date = formatStr.format(d);
                    datesStr += date + "\n";
                    datesSelected.add(formatStr2.format(d));
                }

                selectDaysBtn.setText(datesStr.substring(0, datesStr.length()-1));
                repoDialogCalendar.dismiss();

                break;

            case R.id.btnReporSubmit:

                Intent intent = new Intent(getActivity(),DayliReportList.class);

                Bundle bundle = new Bundle();
                bundle.putBooleanArray("personSelected", personSelected);
                bundle.putStringArrayList("datesSelected", datesSelected);
                bundle.putBooleanArray("foodTimeSelected", foodTimeSelected);
                intent.putExtras(bundle);

                repoDialog.dismiss();
                startActivity(intent);

                break;
        }
    }
}
