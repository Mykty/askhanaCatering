package com.example.mykty.askhanacatering.fragments;

import android.app.Activity;
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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.activity.PersonnelListActivity;
import com.example.mykty.askhanacatering.adapter.PMenuListAdapter;
import com.example.mykty.askhanacatering.adapter.PersonnelListAdapter;
import com.example.mykty.askhanacatering.database.StoreDatabase;
import com.example.mykty.askhanacatering.fragments.main_tabs_fragments.PersonnelFragment;
import com.example.mykty.askhanacatering.module.PMenu;
import com.example.mykty.askhanacatering.module.Personnel;
import com.example.mykty.askhanacatering.module.RecyclerItemClickListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_CARD_NUMBER;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_FKEY;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_ID_NUMBER;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_INFO;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_OTHER_COUNT;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_PHOTO;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_TEACHER_COUNT;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_TOTAL_COUNT;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_TYPE;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_VOLUNTEER_COUNT;
import static com.example.mykty.askhanacatering.database.StoreDatabase.COLUMN_WORKER_COUNT;
import static com.example.mykty.askhanacatering.database.StoreDatabase.TABLE_PERSONNEL;
import static com.example.mykty.askhanacatering.database.StoreDatabase.TABLE_PERSONNEL_COUNT;

public class PersonnelListFragment extends Fragment implements SearchView.OnQueryTextListener {

    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager linearLayoutManager;
    private RecyclerView.LayoutManager gridLayoutManager, gridLayoutManager2;
    private static RecyclerView recyclerView;
    private static ArrayList<PMenu> menu;
    View view;
    SQLiteDatabase sqdb;
    StoreDatabase storeDb;
    DatabaseReference mDatabaseRef;
    int teacherC = 0, workerC = 0, volunteerC = 0, others = 0, totalC = 0;
    TextView total;
    boolean versionChanged = false;
    String types[] = {"teacher", "worker", "volunteer", "others"};
    String[] SPINNERLIST = {"Мұғалім", "Персонал", "Тәрбиеші", "Басқа"};
    Dialog addNewPersonnelDialog;
    PMenu teachersMenu, workersMenu, volunteersMenu, othersMenu;
    EditText pInfo, pIdNumber, otherDesc;
    LinearLayout myLayout;
    int pos = 0;
    String otherDescText = "url";
    ArrayList<Personnel> personalleStore;
    ArrayList<Personnel> personalleStoreCopy;
    PersonnelListAdapter adapterForAllPer;
    boolean searched = false;
    RelativeLayout relativeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_personnel, container, false);
        getActivity().setTitle(getResources().getString(R.string.personnel_list));
        setupViews();

        if (!checkVersion()) fillPersonnelCount();
        createDialog();


        return view;
    }

    public void setupViews() {
        recyclerView = view.findViewById(R.id.my_recycler_view);
        relativeLayout = view.findViewById(R.id.coordinatorLayout);

        recyclerView.setHasFixedSize(true);
        total = view.findViewById(R.id.textView2);

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        FirebaseApp.initializeApp(getActivity());
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        linearLayoutManager = new LinearLayoutManager(getActivity());
        gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        gridLayoutManager2 = new GridLayoutManager(getActivity(), 1);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        menu = new ArrayList<PMenu>();

        teachersMenu = new PMenu("Мұғалім", R.drawable.menu1, "0");
        workersMenu = new PMenu("Персонал", R.drawable.menu2, "0");
        volunteersMenu = new PMenu("Тәрбиеші", R.drawable.menu3, "0");
        othersMenu = new PMenu("Басқа", R.drawable.menu3, "0");

        menu.add(teachersMenu);
        menu.add(workersMenu);
        menu.add(volunteersMenu);
        menu.add(othersMenu);

        personalleStore = new ArrayList<Personnel>();
        personalleStoreCopy = new ArrayList<Personnel>();

        adapter = new PMenuListAdapter(menu);
        adapterForAllPer = new PersonnelListAdapter(getActivity(), personalleStore);

        recyclerView.setAdapter(adapter);
//        recyclerView.setAdapter(adapterForAllPer);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {
                        if (!searched) {
                            Intent intent = new Intent(getActivity(), PersonnelListActivity.class);
                            intent.putExtra("type", types[position]);
                            startActivity(intent);
                        } else {

                            showDialog(personalleStore.get(position), position);
                            Toast.makeText(getActivity(), "After search", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                })
        );
    }

    public void createDialog() {
        addNewPersonnelDialog = new Dialog(getActivity());
        addNewPersonnelDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        addNewPersonnelDialog.setContentView(R.layout.dialog_add_new_personnel);

        pInfo = addNewPersonnelDialog.findViewById(R.id.pInfo);
        pIdNumber = addNewPersonnelDialog.findViewById(R.id.pIdNumber);

        otherDesc = new EditText(getActivity());
        otherDesc.setHint("басқа түсіндірмесі");
        otherDesc.setHintTextColor(getActivity().getResources().getColor(R.color.grey));

        Button btnOK = addNewPersonnelDialog.findViewById(R.id.btnOk);
        Button btnCancel = addNewPersonnelDialog.findViewById(R.id.btnCancel);

        myLayout = addNewPersonnelDialog.findViewById(R.id.mainL);
        otherDesc.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, SPINNERLIST);
        Spinner spinner = addNewPersonnelDialog.findViewById(R.id.android_material_design_spinner);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 3) {
                    myLayout.addView(otherDesc);
                } else {
                    myLayout.removeView(otherDesc);
                }
                pos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pInfo.length() > 0 && pIdNumber.length() > 0) {
                    String info = pInfo.getText().toString();
                    String idNumber = pIdNumber.getText().toString();
                    String type = types[pos];

                    if (otherDesc.getParent() != null) {
                        otherDescText = otherDesc.getText().toString();
                    }

                    String newKey = mDatabaseRef.child("personnel_store").child("store").push().getKey();

                    Personnel personnel = new Personnel("", "" + info, "" + newKey, idNumber, otherDescText, type);
                    mDatabaseRef.child("personnel_store").child("store").child(newKey).setValue(personnel);
                    refreshPersonnels();

                    addNewPersonnelDialog.dismiss();
                    showSuccessToast(pInfo.getText().toString());
                    pInfo.setText("");
                    pIdNumber.setText("");
                    incrementVersion();

                } else {
                    if (isEmpty(pInfo.getText().toString())) pInfo.setError("Ақпарат толтырылмады");
                    if (isEmpty(pIdNumber.getText().toString()))
                        pIdNumber.setError("Ақпарат толтырылмады");

                }

            }
        });

//        FloatingActionButton fab = view.findViewById(R.id.fab);
//
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if(checkInetConnection()) {
//                    addNewPersonnelDialog.show();
//                }
//            }
//        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewPersonnelDialog.dismiss();
            }
        });
    }


    int selectedPos = 0;
    Button ok;
    String tName, id_code;

    public void showDialog(final Personnel personnel, final int pos) {
        tName = personnel.getInfo();
        id_code = personnel.getCard_number();

        final Dialog alert = new Dialog(getActivity(), R.style.AlertDialogTheme);
        alert.setContentView(R.layout.dialog_edit);

        final TextView tvName = alert.findViewById(R.id.textViewName);
        TextView oIdNumber = alert.findViewById(R.id.oldIdNUmber);
        final EditText nCardNumber = alert.findViewById(R.id.newIdNUmber);

        ok = alert.findViewById(R.id.btnOk);
        Button cancel = alert.findViewById(R.id.btnCancel);
        Button del = alert.findViewById(R.id.btnDel);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, SPINNERLIST);
        Spinner spinner = alert.findViewById(R.id.persTypeSpinner);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPos = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tvName.setText(tName);
        oIdNumber.setText("CARD Number: " + id_code);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnOk:
                        if (isNetworkAvailable(getActivity())) {
                            if (nCardNumber.getText().length() > 0) {
                                updateCardNumber(personnel, nCardNumber.getText().toString());
                                //fillPersonnel(type);
                                Toast.makeText(getActivity(), tName + " CARD Number өзгерді", Toast.LENGTH_SHORT).show();
                            }

                            if (selectedPos != 0) {
                                String newType = types[selectedPos];
                                updatePersonnelType(personnel, newType);
                                Toast.makeText(getActivity(), tName + "Персонал түрі өзгерді", Toast.LENGTH_SHORT).show();

                            }
                        }


                        break;
                    case R.id.btnDel:
                        if (isNetworkAvailable(getActivity())) {
                            deletePersonnel(tName, pos);
                        }
                        break;


                    case R.id.btnCancel:

                        nCardNumber.setText("");
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

    public void updatePersonnelType(Personnel personnel, final String type) {
        final String info = personnel.getInfo();

        ContentValues versionValues = new ContentValues();
        versionValues.put(COLUMN_TYPE, type.toLowerCase());

        sqdb.update(TABLE_PERSONNEL, versionValues, COLUMN_TYPE + "='" + info + "'", null);

        mDatabaseRef.child("personnel_store").child("store").child(personnel.getKey()).child("type").setValue(type.toLowerCase());
        incrementVersion();
    }

    public void updateCardNumber(Personnel personnel, final String cardNumber) {

        String info = personnel.getInfo();
        ContentValues versionValues = new ContentValues();
        versionValues.put(COLUMN_ID_NUMBER, cardNumber.toLowerCase());

        sqdb.update(TABLE_PERSONNEL, versionValues, COLUMN_INFO + "='" + info + "'", null);
        mDatabaseRef.child("personnel_store").child("store").child(personnel.getKey()).child("card_number").setValue(cardNumber.toLowerCase());
        incrementVersion();
        personnel.setCard_number(cardNumber.toLowerCase());

    }

    Personnel deletedPersonnel;
    String deletedKey;

    public void deletePersonnel(final String info, final int pos) {
        sqdb.delete(TABLE_PERSONNEL, COLUMN_INFO + "='" + info + "'", null);
        adapterForAllPer.removeItem(pos);

        Query myTopPostsQuery = mDatabaseRef.child("personnel_store").child("store");
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    for (DataSnapshot personnelStore : dataSnapshot.getChildren()) {
                        Personnel personnel = personnelStore.getValue(Personnel.class);
                        if (info.equals(personnel.getInfo())) {
                            String key = personnelStore.getKey();

                            mDatabaseRef.child("personnel_store").child("store").child(key).removeValue();
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
                adapterForAllPer.restoreItem(deletedPersonnel, pos);
                mDatabaseRef.child("personnel_store").child("store").child(deletedKey).setValue(deletedPersonnel);

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

                if (dataSnapshot.exists()) {
                    long version = (long) dataSnapshot.getValue();
                    version++;
                    mDatabaseRef.child("personnel_ver").setValue(version);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        fillPersonnelCount();
    }

    public boolean isEmpty(String text) {
        return TextUtils.isEmpty(text);
    }

    public void fillPersonnelCount() {
        Cursor cursor = sqdb.rawQuery("SELECT * FROM " + TABLE_PERSONNEL_COUNT, null);
        personalleStore.clear();
        personalleStoreCopy.clear();

        if (cursor != null && (cursor.getCount() > 0)) {
            cursor.moveToNext();

            totalC = Integer.parseInt(cursor.getString(0));
            teacherC = Integer.parseInt(cursor.getString(1));
            workerC = Integer.parseInt(cursor.getString(2));
            volunteerC = Integer.parseInt(cursor.getString(3));
            others = Integer.parseInt(cursor.getString(4));
        }

        updateViews();

        Cursor cursorPer = sqdb.rawQuery("SELECT * FROM " + TABLE_PERSONNEL, null);

        if (cursorPer != null && (cursorPer.getCount() > 0)) {
            while (cursorPer.moveToNext()) {

                personalleStore.add(new Personnel("" + cursorPer.getString(0),
                        "" + cursorPer.getString(1),
                        "" + cursorPer.getString(2),
                        "" + cursorPer.getString(3),
                        "" + cursorPer.getString(4),
                        "" + cursorPer.getString(5)));

            }
        }

        personalleStoreCopy = (ArrayList<Personnel>) personalleStore.clone();
    }

    public void refreshPersonnels() {
        mDatabaseRef.child("personnel_store").child("store").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    totalC = 0;
                    teacherC = 0;
                    workerC = 0;
                    volunteerC = 0;
                    others = 0;

                    storeDb.cleanPersonnel(sqdb);

                    for (DataSnapshot teachersSnapshot : dataSnapshot.getChildren()) {
                        Personnel personnel = teachersSnapshot.getValue(Personnel.class);

                        String key = teachersSnapshot.getKey().toString();
                        Log.i("info", "key: " + key);

                        String info = personnel.getInfo();
                        String idNumber = personnel.getId_number().toLowerCase();
                        String cardNumber = personnel.getCard_number().toLowerCase();
                        String photo = personnel.getPhoto();
                        String type = personnel.getType();
                        totalC++;

                        if (type.equals("teacher")) teacherC++;
                        else if (type.equals("worker")) workerC++;
                        else if (type.equals("volunteer")) volunteerC++;
                        else if (type.equals("others")) others++;
                        else if (type.contains("guest")) others++;

                        ContentValues personnelValue = new ContentValues();
                        personnelValue.put(COLUMN_FKEY, key);
                        personnelValue.put(COLUMN_INFO, info);
                        personnelValue.put(COLUMN_ID_NUMBER, idNumber);
                        personnelValue.put(COLUMN_CARD_NUMBER, cardNumber);
                        personnelValue.put(COLUMN_PHOTO, photo);
                        personnelValue.put(COLUMN_TYPE, type);

                        sqdb.insert(TABLE_PERSONNEL, null, personnelValue);

                    }
                    updateViews();
                    updateDb();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateDb() {
        storeDb.cleanPersonnelCount(sqdb);

        ContentValues personnelValueCount = new ContentValues();
        personnelValueCount.put(COLUMN_TOTAL_COUNT, totalC);
        personnelValueCount.put(COLUMN_TEACHER_COUNT, teacherC);
        personnelValueCount.put(COLUMN_WORKER_COUNT, workerC);
        personnelValueCount.put(COLUMN_VOLUNTEER_COUNT, volunteerC);
        personnelValueCount.put(COLUMN_OTHER_COUNT, others);

        sqdb.insert(TABLE_PERSONNEL_COUNT, null, personnelValueCount);
    }

    public void updateViews() {
        teachersMenu.setCount("" + teacherC);
        workersMenu.setCount("" + workerC);
        volunteersMenu.setCount("" + volunteerC);
        othersMenu.setCount("" + others);

        total.setText("Барлығы: " + totalC);

        adapter.notifyDataSetChanged();
    }

    public boolean checkVersion() {
        versionChanged = false;

        Query myTopPostsQuery = mDatabaseRef.child("personnel_ver");
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String newVersion = dataSnapshot.getValue().toString();
                    if (!getCurrentVersion().equals(newVersion)) {
                        updateCurrentVersion(newVersion);
                        refreshPersonnels();

                        versionChanged = true;
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return versionChanged;
    }

    public String getCurrentVersion() {
        Cursor res = sqdb.rawQuery("SELECT personnel_ver FROM versions", null);
        res.moveToNext();

        return res.getString(0);
    }

    public void updateCurrentVersion(String newVersion) {
        ContentValues versionValues = new ContentValues();
        versionValues.put("personnel_ver", newVersion);

        sqdb.update("versions", versionValues, "personnel_ver=" + getCurrentVersion(), null);
        System.out.println("Updated version: " + getCurrentVersion());
    }

    public void showSuccessToast(String info) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) view.findViewById(R.id.custom_toast_layout));
        TextView text = toastLayout.findViewById(R.id.custom_toast_message);
        text.setText(info + " сәтті енгізілді!");

        Toast toast = new Toast(getActivity());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        toast.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        searchView.setIconified(true);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {

            @Override
            public boolean onClose() {
                hideKeyboard(getActivity());
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {

            return true;
        }

        /*if (id == R.id.act_list) {
            recyclerView.setLayoutManager(linearLayoutManager);
            return true;
        }
        if (id == R.id.act_grid) {
            recyclerView.setLayoutManager(gridLayoutManager);
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (query.length() > 0) {
            recyclerView.setLayoutManager(gridLayoutManager2);
            recyclerView.setAdapter(adapterForAllPer);
            searched = true;
            filter(query);
        } else {

            searched = false;
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(adapter);
        }
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String newText) {
        filter(newText);
        return false;
    }

    public void filter(String text) {
        personalleStore.clear();

        if (text.isEmpty()) {
            personalleStore.addAll(personalleStoreCopy);
        } else {
            text = text.toLowerCase();
            for (Personnel item : personalleStoreCopy) {
                if (item.getInfo().toLowerCase().contains(text) || item.getInfo().toUpperCase().contains(text)) {
                    personalleStore.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    public boolean checkInetConnection() {
        if (isNetworkAvailable(getActivity())) {
            return true;
        } else {
            Toast.makeText(getActivity(), "Интернет байланысыңызды тексеріңіз", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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