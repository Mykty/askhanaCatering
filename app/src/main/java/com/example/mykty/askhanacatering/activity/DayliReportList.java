package com.example.mykty.askhanacatering.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.module.EaterItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DayliReportList extends AppCompatActivity {
    boolean[] personSelected, foodTimeSelected;
    ArrayList<String> datesSelected;
    DatabaseReference mDatabaseRef;
    String personStore[] = {"personnel", "college", "lyceum"};
    String foodStore1[] = {"breakfast", "lunch", "dinner"};
    String foodStore2[] = {"breakfast", "poldnik1", "lunch", "poldnik2", "dinner"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dayli_report_list);
        datesSelected = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        personSelected = bundle.getBooleanArray("personSelected");
        foodTimeSelected = bundle.getBooleanArray("foodTimeSelected");
        datesSelected = bundle.getStringArrayList("datesSelected");

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        /*personSelected:{
            personnel;
            college;
            lyceum;
        }
*/
        for(boolean b: personSelected){
            Log.i("info", "Person: "+b);
        }

        for(boolean b: foodTimeSelected){
            Log.i("info", "Food Time: "+b);
        }

//        for(String b: datesSelected){
//            Log.i("info", "Dates: "+b);
//        }

        getData();
    }

    public void getData() {
        for (int i = 0; i < personSelected.length; i++) {
            if (personSelected[i]) {
                String pItem = personStore[i];

                for (int j = 0; j < datesSelected.size(); j++) {
                    String dItem = datesSelected.get(j);

                    for (int k = 0; k < foodTimeSelected.length; k++) {
                        String fItem = "";
                        if (foodTimeSelected[k]) {
                            if (personSelected[2]) fItem = foodStore2[k];
                            else fItem = foodStore1[k];

                            getDataFromFirebase(pItem, dItem, fItem);

                        }

                    }
                }
            }
        }
    }

    public void getDataFromFirebase(final String personItem, final String dateItem, final String foodItem) {
        mDatabaseRef = mDatabaseRef.child("days").child(personItem).child(dateItem).child(foodItem);
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for(DataSnapshot eaterItemData: dataSnapshot.getChildren()){
                        String id_number = eaterItemData.getKey().toString();
                        String time = eaterItemData.getValue().toString();

                        //EaterItem eaterItem = new EaterItem(id_number, time);
                        Log.i("firebase", id_number+" "+time);
                    }
                } else {
                    Log.i("firebase", "" + personItem + " " + dateItem + " " + foodItem + " is not exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
