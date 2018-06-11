package com.example.mykty.askhanacatering.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.fragments.new_eaters.BreakfastFragment;
import com.example.mykty.askhanacatering.fragments.new_eaters.DinnerFragment;
import com.example.mykty.askhanacatering.fragments.new_eaters.LunchFragment;

public class EnterNewEaterActivity extends AppCompatActivity{

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    changeFragment(new BreakfastFragment());
                    return true;
                case R.id.navigation_dashboard:

                    changeFragment(new LunchFragment());
                    return true;
                case R.id.navigation_notifications:

                    changeFragment(new DinnerFragment());
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_eater);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public void changeFragment(Fragment current_fragment){
        Fragment fragment = current_fragment;
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content, fragment).commit();

    }
}
