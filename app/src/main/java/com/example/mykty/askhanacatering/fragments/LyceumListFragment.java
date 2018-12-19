package com.example.mykty.askhanacatering.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.activity.StudentCabinet;
import com.example.mykty.askhanacatering.adapter.PersonnelListAdapter;
import com.example.mykty.askhanacatering.database.StoreDatabase;
import com.example.mykty.askhanacatering.module.GroupDataItem;
import com.example.mykty.askhanacatering.module.Personnel;
import com.example.mykty.askhanacatering.module.Student;
import com.example.mykty.askhanacatering.module.StudentsItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class LyceumListFragment extends Fragment implements View.OnClickListener, SearchView.OnQueryTextListener {
    private RecyclerView mRecyclerView;
    private Context mContext;
    private DatabaseReference mDatabaseRef;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    Dialog dialog;
    Cursor sCursor;
    String imgUrl;
    DateFormat dateF, timeF, dateFr;
    String date, time, firebaseDate;
    String classes[];
    ArrayList<String> latecomersStore;
    HashMap<String, Student> idNumberHashMap;
    View view;
    TextView textViewTotal;
    FloatingActionButton fab;
    Dialog addNewCollegeStudent;
    Button addNewStudentBtn, btnCancel; //choosePhotoBtn, scannerQrCodeBtn
    FirebaseStorage storage;
    StorageReference storageReference;
    RecyclerDataAdapter recyclerDataAdapter;
    Spinner spinnerGroups;
    EditText sInfo, sCardNumber;
    private final int PICK_IMAGE_REQUEST = 71;
    private Uri filePath;
    private final int CAMERA_REQUEST = 77;
    boolean photoSelected = false, qrCodeScannered = false;
    ArrayList<GroupDataItem> groupsList;
    ArrayList<Personnel> studentsStoreForSearch;
    ArrayList<Personnel> studentsStoreForSearchCopy;
    PersonnelListAdapter adapterForAllStudents;
    Query studentsQuery;

    ArrayList<StudentsItem> studentStore;
    GroupDataItem groupDataItem;
    int totalCount = 0;
    ArrayList<String> groupsStore;
    public static final String TABLE_LYCEUM_STUDENTS = "lyceum_students_list";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_for_list, container, false);

        setupViews();
        manageDate();

        return view;
    }

    public void setupViews() {
        getActivity().setTitle(getResources().getString(R.string.lyceum_list));

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();
        mContext = getActivity();
        mRecyclerView = view.findViewById(R.id.recyclerView);
        textViewTotal = view.findViewById(R.id.textViewTotal);
        // createResDialog();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        latecomersStore = new ArrayList<>();
        idNumberHashMap = new HashMap<>();
        fab = view.findViewById(R.id.fab_guest);
        fab.setOnClickListener(this);
        studentsStoreForSearch = new ArrayList<>();
        studentsStoreForSearchCopy = new ArrayList<>();

//        getCollegeStudents();
        getStudents();
        createDialogs();

    }

    public void createDialogs() {
        addNewCollegeStudent = new Dialog(getActivity());
        addNewCollegeStudent.setTitle(getResources().getString(R.string.enter_new_student));
        addNewCollegeStudent.setContentView(R.layout.dialog_add_new_college_student);

        classes = getResources().getStringArray(R.array.classes);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, classes);
        spinnerGroups = addNewCollegeStudent.findViewById(R.id.select_group_spinner);
        spinnerGroups.setAdapter(arrayAdapter);

        addNewStudentBtn = addNewCollegeStudent.findViewById(R.id.btnOk);
        btnCancel = addNewCollegeStudent.findViewById(R.id.btnCancel);

        sInfo = addNewCollegeStudent.findViewById(R.id.sInfo);
        sCardNumber = addNewCollegeStudent.findViewById(R.id.sCardNumber);

        addNewStudentBtn.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
    }

    public void modifyAdapter() {
        recyclerDataAdapter = new RecyclerDataAdapter(groupsList);
        adapterForAllStudents = new PersonnelListAdapter(getActivity(), studentsStoreForSearch, idNumberHashMap);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(recyclerDataAdapter);
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_guest:

                if (checkInetConnection()) {
                    addNewCollegeStudent.show();
                }
                break;

            case R.id.btnCancel:
                clearAll();
                break;

            case R.id.btnOk:

                boolean tOk = true;

                String sGroup = spinnerGroups.getSelectedItem().toString();
                String sName = sInfo.getText().toString();
                String sCardN = sCardNumber.getText().toString();

                if(sName.contains("\n")){
                    sName = sName.substring(0, sName.length()-1);
                }

                if(sCardN.contains("\n")){
                    sCardN = sCardN.substring(0, sCardN.length()-1);
                }

                if (sGroup.equals(getResources().getString(R.string.select))) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.select_group_mistake), Toast.LENGTH_SHORT).show();
                    tOk = false;
                } else if (sName.length() == 0) {
                    sInfo.setError(getResources().getString(R.string.s_info_mistake));
                    tOk = false;
                } else if (sCardN.length() == 0) {
                    sCardNumber.setError(getResources().getString(R.string.read_card_number_mistake));
                    tOk = false;
                }

                if (tOk) {
                    registerStudent(sGroup, sName, sCardN, returnedResult);
                }

                break;
        }
    }

    String returnedResult = "qr code";
    String downloadUri = "photo url";

    private void registerStudent(String sGroup, String sName, String card_number, String qr_code) {

        String id_number = getIdNumber();
        Student student = new Student(sName, id_number, card_number, downloadUri, qr_code);

        String key = mDatabaseRef.child("classes").child(sGroup).push().getKey();
        mDatabaseRef.child("classes").child(sGroup).child(key).setValue(student);

        incrementLyceumVersion();
        clearAll();
        showSuccessToast();
    }


    public void getStudents() {
        totalCount = 0;
        groupsStore = new ArrayList<>();

        Cursor cursorGroup = sqdb.rawQuery("SELECT s_group FROM " + TABLE_LYCEUM_STUDENTS, null);

        if (cursorGroup != null && (cursorGroup.getCount() > 0)) {
            while (cursorGroup.moveToNext()) {
                if (!groupsStore.contains(cursorGroup.getString(0)))
                    groupsStore.add(cursorGroup.getString(0));
            }
        }

        groupsList = new ArrayList<>();

        for (String group : groupsStore) {
            Cursor cursorStd = sqdb.rawQuery("SELECT * FROM " + TABLE_LYCEUM_STUDENTS + " WHERE s_group = '" + group + "'", null);
            studentStore = new ArrayList<>();

            if (cursorStd != null && (cursorStd.getCount() > 0)) {
                while (cursorStd.moveToNext()) {

                    Student student = new Student(cursorStd.getString(0),
                            cursorStd.getString(1),
                            cursorStd.getString(2),
                            cursorStd.getString(3),
                            cursorStd.getString(4),
                            cursorStd.getString(5),
                            cursorStd.getString(6));


                    studentStore.add(new StudentsItem(student.getName()));
                    studentsStoreForSearch.add(new Personnel("", "" + student.getName(), " ", " ", "Группа: " + group, "others"));
                    idNumberHashMap.put(student.getName(), student);

                    totalCount++;
                }
            }

            groupDataItem = new GroupDataItem(studentStore);
            groupDataItem.setParentName(group);
            groupsList.add(groupDataItem);


        }

        studentsStoreForSearchCopy = (ArrayList<Personnel>) studentsStoreForSearch.clone();

        textViewTotal.setText(getResources().getString(R.string.all_title) + " " + totalCount + " " + getString(R.string.student));
        modifyAdapter();
    }

    public String getIdNumber() {
        Date date = new Date();
        String idN = "i" + date.getTime();
        return idN;
    }

    public void clearAll() {
        spinnerGroups.setSelection(0);
        sInfo.getText().clear();
        sCardNumber.getText().clear();
        photoSelected = false;
        qrCodeScannered = false;
        addNewCollegeStudent.dismiss();
    }

    public void incrementLyceumVersion() {
        Query myTopPostsQuery = mDatabaseRef.child("lyceum_student_list_ver");
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long version = (long) dataSnapshot.getValue();
                version++;
                mDatabaseRef.child("lyceum_student_list_ver").setValue(version);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        getStudents();

    }

    public void showSuccessToast() {
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) view.findViewById(R.id.custom_toast_layout));
        TextView text = toastLayout.findViewById(R.id.custom_toast_message);
        text.setText("Жаңа оқушы сәтті енгізілді!");

        Toast toast = new Toast(getActivity());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        toast.show();
    }

    private class RecyclerDataAdapter extends RecyclerView.Adapter<RecyclerDataAdapter.MyViewHolder> {
        private ArrayList<GroupDataItem> dummyParentDataItems;

        RecyclerDataAdapter(ArrayList<GroupDataItem> dummyParentDataItems) {
            this.dummyParentDataItems = dummyParentDataItems;
        }

        @Override
        public RecyclerDataAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parent_child_listing2, parent, false);
            return new MyViewHolder(itemView);
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
            for (int textViewIndex = 0; textViewIndex < noOfChild; textViewIndex++) {
                TextView currentTextView = (TextView) holder.linearLayout_childItems.getChildAt(textViewIndex);
                currentTextView.setText(dummyParentDataItem.getChildDataItems().get(textViewIndex).getChildName());

                if (latecomersStore.contains(currentTextView.getText().toString())) {
                    currentTextView.setBackgroundColor(getResources().getColor(R.color.red));
                }
            }

            holder.tvCount.setText("" + noOfChild);
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
                    textView.setTextSize(20.0f);

                    textView.setBackgroundColor(getResources().getColor(R.color.white));
//                    textView.setBackground(getResources().getDrawable(R.drawable.background_sub_module_text));
//                    textView.setBackground(ContextCompat.getDrawable(context, R.drawable.background_sub_module_text));
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
                    if (checkInetConnection()) {

                        textViewClicked = (TextView) view;
                        clickedSName = textViewClicked.getText().toString();

                        Intent intent = new Intent(getActivity(), StudentCabinet.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("type", "lyceum");
                        bundle.putSerializable("sClass", idNumberHashMap.get(clickedSName));
                        intent.putExtras(bundle);
                        startActivity(intent);

                    }
                }
            }

        }
    }

    TextView textViewClicked;
    String qr_code;
    String clickedSName;

    public void createResDialog() {
        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.scann_res);

        TextView studentName = dialog.findViewById(R.id.sName);
        ImageView studentImg = dialog.findViewById(R.id.imageV);

        Button btOk = dialog.findViewById(R.id.buttonOk);
        Button btCancel = dialog.findViewById(R.id.buttonCancel);

        studentName.setText(clickedSName);

        sCursor = getStudentsByName(clickedSName);

        if (((sCursor != null) && (sCursor.getCount() > 0))) {
            sCursor.moveToNext();

            qr_code = sCursor.getString(0);
            imgUrl = sCursor.getString(3);
        }

        Glide.with(this)
                .load(imgUrl)
                .placeholder(R.drawable.s_icon)
                .into(studentImg);

        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (query.length() > 0) {
            mRecyclerView.setAdapter(adapterForAllStudents);
            filter(query);
        } else {

            mRecyclerView.setAdapter(recyclerDataAdapter);
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onQueryTextSubmit(String newText) {
        filter(newText);
        return false;
    }

    public void filter(String text) {
        studentsStoreForSearch.clear();

        if (text.isEmpty()) {
            studentsStoreForSearch.addAll(studentsStoreForSearchCopy);
        } else {
            text = text.toLowerCase();
            for (Personnel item : studentsStoreForSearchCopy) {
                if (item.getInfo().toLowerCase().contains(text) || item.getInfo().toUpperCase().contains(text)) {
                    studentsStoreForSearch.add(item);
                }
            }
        }


        adapterForAllStudents.notifyDataSetChanged();
    }

    public void manageDate() {
        dateF = new SimpleDateFormat("EEEE, dd_MM_yyyy");//2001.07.04
        dateFr = new SimpleDateFormat("dd_MM_yyyy");//2001.07.04
        timeF = new SimpleDateFormat("HH:mm");//14:08

        date = dateF.format(Calendar.getInstance().getTime());
        firebaseDate = dateFr.format(Calendar.getInstance().getTime());
        time = timeF.format(Calendar.getInstance().getTime());

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public Cursor getStudentsByName(String sname) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_LYCEUM_STUDENTS + " WHERE name=?", new String[]{sname});
        return res;
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public boolean checkInetConnection() {
        if (isNetworkAvailable(getActivity())) {
            return true;
        } else {
            Toast.makeText(getActivity(), getString(R.string.checkInetConnection), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

}