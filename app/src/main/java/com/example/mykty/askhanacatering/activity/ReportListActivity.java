package com.example.mykty.askhanacatering.activity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.adapter.MenuListAdapter;
import com.example.mykty.askhanacatering.adapter.PersonnelListAdapter;
import com.example.mykty.askhanacatering.database.StoreDatabase;
import com.example.mykty.askhanacatering.module.PMenu;
import com.example.mykty.askhanacatering.module.RecyclePersonnelItemHelper;
import com.example.mykty.askhanacatering.module.RecyclerItemClickListener;
import com.example.mykty.askhanacatering.module.Personnel;
import com.example.mykty.askhanacatering.module.RecyclerItemTouchHelper;
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
import static com.example.mykty.askhanacatering.database.StoreDatabase.TABLE_COLLEGE_STUDENTS;
import static com.example.mykty.askhanacatering.database.StoreDatabase.TABLE_LYCEUM_STUDENTS;
import static com.example.mykty.askhanacatering.database.StoreDatabase.TABLE_PERSONNEL;

public class ReportListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, RecyclePersonnelItemHelper.RecyclerItemTouchHelperListener  {

    private static RecyclerView recyclerView;
    private RecyclerView.LayoutManager linearLayoutManager;
    SQLiteDatabase sqdb;
    StoreDatabase storeDb;
    DatabaseReference mDatabaseRef;
    PersonnelListAdapter adapter;
    ArrayList<Personnel> store;
    ArrayList<Personnel> storeCopy;
    String type;
    RelativeLayout relativeLayout;
    Personnel deletedPersonnel;
    String deletedIdNumber;
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback;
    String f_time, firebaseDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personnel_list);
        setupViews();

        ArrayList<String> myList = (ArrayList<String>) getIntent().getSerializableExtra("list");
        String type = getIntent().getStringExtra("type");
        f_time = getIntent().getStringExtra("f_time");
        firebaseDate = getIntent().getStringExtra("firebaseDate");

        if(type.equals("personnel")) {
            fillPersonnel(myList);

        }else if(type.equals("college")) {
            fillStudents(myList, TABLE_COLLEGE_STUDENTS);

        }else if(type.equals("lyceum")) {
            fillStudents(myList, TABLE_LYCEUM_STUDENTS);
        }
    }

    public void fillPersonnel(ArrayList<String> myList) {

        for(String id_number: myList) {

            Cursor cursor = sqdb.rawQuery("SELECT * FROM " + TABLE_PERSONNEL + " WHERE id_number = '"+id_number+"'", null);

            if (cursor != null && (cursor.getCount() > 0)) {
                cursor.moveToNext();

                Personnel personnel = new Personnel("" + cursor.getString(0),
                        "" + cursor.getString(1),
                        "" + cursor.getString(2),
                        "" + cursor.getString(3),
                        "" + cursor.getString(4),
                        "" + cursor.getString(5));

                store.add(personnel);
            }
        }

        storeCopy = (ArrayList<Personnel>) store.clone();
        adapter.notifyDataSetChanged();
    }

    public void fillStudents(ArrayList<String> myList, String tableName) {

        for(String id_number: myList) {

            Cursor cursor = sqdb.rawQuery("SELECT * FROM " + tableName + " WHERE id_number = '"+id_number+"'", null);

            if (cursor != null && (cursor.getCount() > 0)) {
                cursor.moveToNext();

                Personnel personnel = new Personnel("", "" + cursor.getString(2),
                        "" + cursor.getString(3),
                        "" + cursor.getString(4),
                        "" + cursor.getString(6),
                        "" + cursor.getString(0));

                store.add(personnel);
            }
        }

        storeCopy = (ArrayList<Personnel>) store.clone();
        adapter.notifyDataSetChanged();
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
        store.clear();

        if (text.isEmpty()) {
            store.addAll(storeCopy);
        } else {
            text = text.toLowerCase();
            for (Personnel item : storeCopy) {
                if (item.getInfo().toLowerCase().contains(text) || item.getInfo().toUpperCase().contains(text)) {
                    store.add(item);
                }
            }
        }

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

        store = new ArrayList<>();
        storeCopy = new ArrayList<>();

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new PersonnelListAdapter(this, store);

        recyclerView.setAdapter(adapter);

//        itemTouchHelperCallback = new RecyclePersonnelItemHelper(0, ItemTouchHelper.LEFT, this);
//        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        if (viewHolder instanceof PersonnelListAdapter.MyViewHolder) {

            String dTitle = store.get(viewHolder.getAdapterPosition()).getInfo();

            final Personnel deletedPersonnel = store.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            deletedIdNumber = deletedPersonnel.getId_number();
            delFromFirebase(deletedIdNumber);

            adapter.removeItem(deletedIndex);
/*
            Snackbar snackbar = Snackbar.make(relativeLayout, dTitle + "тізімнен өшірілді!", Snackbar.LENGTH_LONG);

            snackbar.setAction("Қайтару", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.restoreItem(deletedPersonnel, deletedIndex);
                    //undoToFirebase(deletedIdNumber, deletedPersonnel);
                }
            });

            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();*/
        }
    }

    public void delFromFirebase(String key){
       // mDatabaseRef = mDatabaseRef.child("days").child("personnel").child(firebaseDate).child(f_time).child(key);

        //Query myTopPostsQuery = mDatabase.child("current_student_version");
        /*
        Query myTopPostsQuery = mDatabaseRef.child("days").child("personnel").child(firebaseDate).child(f_time).child(key);
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String pIdNumber = dataSnapshot.getKey();
                String test = dataSnapshot.getValue().toString();

                System.out.println(pIdNumber+" "+test);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

//        mDatabaseRef.child("days").child("personnel").child(firebaseDate).child(f_time).child(key).removeValue();
        mDatabaseRef.child("days").child("personnel").child(firebaseDate).child(f_time).child("39f9192e").removeValue();
        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
    }

/*
    public void undoToFirebase(String deletedKey, Personnel personnel){
        mDatabaseRef.child("days").child("personnel").child(firebaseDate).child(f_time).child(deletedKey).removeValue();
        mDatabaseRef.child(deletedKey).setValue(teacher);
    }
*/
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

}
