package com.example.mykty.askhanacatering.activity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.adapter.PersonnelListAdapter;
import com.example.mykty.askhanacatering.database.StoreDatabase;
import com.example.mykty.askhanacatering.module.RecyclerItemClickListener;
import com.example.mykty.askhanacatering.module.Personnel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_ID_NUMBER;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_INFO;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_PHOTO;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_TYPE;
import static com.example.mykty.askhanacatering.database.StoreDatabase.TABLE_PERSONNEL;

public class PersonnelListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private static RecyclerView recyclerView;
    private RecyclerView.LayoutManager linearLayoutManager;
    SQLiteDatabase sqdb;
    StoreDatabase storeDb;
    DatabaseReference mDatabaseRef;
    PersonnelListAdapter adapter;
    ArrayList<Personnel> personalleStore;
    ArrayList<Personnel> personalleStoreCopy;
    String type;
    RelativeLayout relativeLayout;
    Personnel deletedPersonnel;
    String deletedKey, title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personnel_list);
        setupViews();

        Intent t = getIntent();
        type = t.getStringExtra("type");
        fillPersonnel(type);

        if(type.equals("teacher")) title = "Мұғалімдер тізімі";
        if(type.equals("volunteer")) title = "Тәрбиешілер тізімі";
        if(type.equals("worker")) title = "Персонал тізімі";
        if(type.equals("others")) title = "Басқалар";

        setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String newText) {
        filter(newText);
        return false;
    }

    public void filter(String text) {
        personalleStore.clear();

        if(text.isEmpty()){
            personalleStore.addAll(personalleStoreCopy);
        } else{
            text = text.toLowerCase();
            for(Personnel item: personalleStoreCopy){
                if(item.getInfo().toLowerCase().contains(text) || item.getInfo().toUpperCase().contains(text)){
                    personalleStore.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    public void fillPersonnel(String type) {
        String[] params = new String[]{type};
        personalleStore.clear();

        Cursor cursor = sqdb.rawQuery("SELECT * FROM " + TABLE_PERSONNEL + " WHERE type=? ORDER BY info", params);

        if (cursor != null && (cursor.getCount() > 0)) {
            while (cursor.moveToNext()) {

                personalleStore.add(new Personnel("" + cursor.getString(0),
                        ""+cursor.getString(1),
                        ""+cursor.getString(2),
                        ""+cursor.getString(3),
                        ""+cursor.getString(4)));
            }
        }

        if(type.contains("guest")){
            String splitParam[] = type.split("|");
            String[] params2 = new String[]{splitParam[0]};
            Cursor cursor2 = sqdb.rawQuery("SELECT * FROM " + TABLE_PERSONNEL + " WHERE type=? ORDER BY info", params2);

            if (cursor2 != null && (cursor2.getCount() > 0)) {
                while (cursor2.moveToNext()) {

                    personalleStore.add(new Personnel("" + cursor2.getString(0),
                            ""+cursor2.getString(1),
                            ""+cursor2.getString(2),
                            ""+cursor2.getString(3),
                            ""+cursor.getString(4)));
                }
            }
        }


        personalleStoreCopy = (ArrayList<Personnel>)personalleStore.clone();
        adapter.notifyDataSetChanged();
    }

    public void setupViews() {
        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();

        relativeLayout = findViewById(R.id.relativeLayout);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        linearLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        personalleStore = new ArrayList<Personnel>();
        personalleStoreCopy = new ArrayList<Personnel>();
        adapter = new PersonnelListAdapter(this, personalleStore);

        recyclerView.setAdapter(adapter);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {
                        showDialog(personalleStore.get(position).getInfo(), personalleStore.get(position).getCard_number(), position);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                })
        );
    }

    public void showDialog(final String tName, String id_code, final int pos) {

        final Dialog alert = new Dialog(this, R.style.AlertDialogTheme);
        alert.setContentView(R.layout.dialog_edit);

        final TextView tvName = alert.findViewById(R.id.textViewName);
        TextView oIdNumber = alert.findViewById(R.id.oldIdNUmber);
        final EditText nIdNumber = alert.findViewById(R.id.newIdNUmber);

        Button ok = alert.findViewById(R.id.btnOk);
        Button cancel = alert.findViewById(R.id.btnCancel);
        Button del = alert.findViewById(R.id.btnDel);

        tvName.setText(tName);
        oIdNumber.setText("CARD Number: " + id_code);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnOk:
                        if (isNetworkAvailable(PersonnelListActivity.this)) {
                            updateIdNumber(tName, nIdNumber.getText().toString());
                            fillPersonnel(type);
                            Toast.makeText(PersonnelListActivity.this, tName+" CARD Number өзгерді", Toast.LENGTH_SHORT).show();
                        }

                        break;
                    case R.id.btnDel:
                        if (isNetworkAvailable(PersonnelListActivity.this)) {
                            deletePersonnel(tName, pos);
                        }
                        break;


                    case R.id.btnCancel:

                        nIdNumber.setText("");
                        break;
                }

                alert.dismiss();
            }
        };

        ok.setOnClickListener(listener);
        cancel.setOnClickListener(listener);
        del.setOnClickListener(listener);

        alert.show();
    }
    
    public void updateIdNumber(final String info, final String idNumber) {

        ContentValues versionValues = new ContentValues();
        versionValues.put(COLUMN_ID_NUMBER, idNumber.toLowerCase());

        sqdb.update(TABLE_PERSONNEL, versionValues, COLUMN_INFO + "='" + info + "'", null);

        Query myTopPostsQuery = mDatabaseRef.child("personnel_store");
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    for (DataSnapshot personnelStore : dataSnapshot.getChildren()) {
                        Personnel personnel = personnelStore.getValue(Personnel.class);
                        if (info.equals(personnel.getInfo())) {
                            String key = personnelStore.getKey();

                            mDatabaseRef.child("personnel_store").child(key).child("id_number").setValue(idNumber.toLowerCase());
                            incrementVersion();
                            break;

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void deletePersonnel(final String info, final int pos){
        sqdb.delete(TABLE_PERSONNEL, COLUMN_INFO + "='" + info + "'", null);
        adapter.removeItem(pos);

        Query myTopPostsQuery = mDatabaseRef.child("personnel_store");
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    for (DataSnapshot personnelStore : dataSnapshot.getChildren()) {
                        Personnel personnel = personnelStore.getValue(Personnel.class);
                        if (info.equals(personnel.getInfo())) {
                            String key = personnelStore.getKey();

                            mDatabaseRef.child("personnel_store").child(key).removeValue();
                            incrementVersion();
                            deletedPersonnel = personnel;
                            deletedKey = key;
                            break;

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Snackbar snackbar = Snackbar.make(relativeLayout, "Өшірілді: " + info, Snackbar.LENGTH_LONG);
        snackbar.setAction("Қайтару", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.restoreItem(deletedPersonnel, pos);
                mDatabaseRef.child("personnel_store").child(deletedKey).setValue(deletedPersonnel);

                ContentValues personnelValue = new ContentValues();
                personnelValue.put(COLUMN_INFO, deletedPersonnel.getInfo());
                personnelValue.put(COLUMN_ID_NUMBER, deletedPersonnel.getId_number());
                personnelValue.put(COLUMN_PHOTO, deletedPersonnel.getPhoto());
                personnelValue.put(COLUMN_TYPE, deletedPersonnel.getType());

                sqdb.insert(TABLE_PERSONNEL, null, personnelValue);
            }
        });
        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.show();

    }

    public void incrementVersion() {
        Query myTopPostsQuery = mDatabaseRef.child("personnel_ver");
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long version = (long) dataSnapshot.getValue();
                version++;
                mDatabaseRef.child("personnel_ver").setValue(version);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

}
