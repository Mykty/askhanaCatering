package com.example.mykty.askhanacatering.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.adapter.MenuListAdapter;
import com.example.mykty.askhanacatering.module.Order;
import com.example.mykty.askhanacatering.module.PMenu;
import com.example.mykty.askhanacatering.module.RecyclerItemClickListener;
import com.example.mykty.askhanacatering.module.RecyclerItemTouchHelper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AddMenuInOrderActivity extends AppCompatActivity implements View.OnClickListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    RecyclerView foodListView;
    LinearLayoutManager linearLayoutManager;
    MenuListAdapter menuListAdapter;
    ArrayList<PMenu> menuList;
    TextView title;
    TextView orderPersonName;
    TextView date;
    TextView status;
    TextView personCount;
    TextView phoneNumber;
    FloatingActionButton addFood;
    Dialog foodDialog;
    TextInputEditText foodTitle, foodDesc;
    Button btnOk, btnCancel;
    MenuItem saveMenuItem;
    boolean saved = true;
    DatabaseReference mDatabaseRef;
    String titleText = "", orderPersonNameText = "", dateEditTextText = "", timeText = "", personCountText = "", phoneNumberText = "";
    String statusText = "жаңа";
    String menuText, keyText;
    boolean editOrder = false;
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback;
    RelativeLayout container;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu);
        setupViews();
        updateViews();

    }

    public void updateViews() {
        Intent menuIntent = getIntent();

        titleText = menuIntent.getStringExtra("title");
        orderPersonNameText = menuIntent.getStringExtra("orderPersonName");
        dateEditTextText = menuIntent.getStringExtra("dateEditText");
        timeText = menuIntent.getStringExtra("time");
        personCountText = menuIntent.getStringExtra("personCount");
        phoneNumberText = menuIntent.getStringExtra("phoneNumber");

        if (menuIntent.hasExtra("status")) {
            editOrder = true;
            statusText = menuIntent.getStringExtra("status");
            menuText = menuIntent.getStringExtra("menu");
            keyText = menuIntent.getStringExtra("keys");

            String menuSplit[] = menuText.split("title: ");

            for (int i = 1; i < menuSplit.length; i++) {
                String descsplit[] = menuSplit[i].split("desc: ");
                menuList.add(new PMenu(descsplit[0], descsplit[1]));
            }
            menuListAdapter.notifyDataSetChanged();
        }

        title.setText(titleText);
        orderPersonName.setText(orderPersonNameText);
        date.setText(dateEditTextText + ", " + timeText);
        personCount.setText(personCountText + " адам");
        status.setText(statusText);
        phoneNumber.setText(phoneNumberText);


        if (statusText.equals("жаңа"))
            status.setTextColor(getResources().getColor(R.color.red));
        if (statusText.equals("қабылданды"))
            status.setTextColor(getResources().getColor(R.color.tabBack2));
        if (statusText.equals("дайын"))
            status.setTextColor(getResources().getColor(R.color.green));

    }

    public void setupViews() {
        foodListView = findViewById(R.id.menuRecycleVIew);
        foodListView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);

        foodListView.setLayoutManager(linearLayoutManager);
        foodListView.setItemAnimator(new DefaultItemAnimator());

        menuList = new ArrayList<>();
        menuListAdapter = new MenuListAdapter(this, menuList);
        foodListView.setAdapter(menuListAdapter);

        FirebaseApp.initializeApp(this);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        container = findViewById(R.id.container);
        title = findViewById(R.id.oTitle);
        orderPersonName = findViewById(R.id.oPerson);
        status = findViewById(R.id.oStatus);
        date = findViewById(R.id.oDate);
        personCount = findViewById(R.id.oCount);
        phoneNumber = findViewById(R.id.oPhoneNumber);
        addFood = findViewById(R.id.btnAddMenu);


        foodDialog = new Dialog(this);
        foodDialog.setTitle("Тамақ енгізу");
        foodDialog.setContentView(R.layout.dialog_new_food);

        foodTitle = foodDialog.findViewById(R.id.fTitle);
        foodDesc = foodDialog.findViewById(R.id.fDesc);
        btnOk = foodDialog.findViewById(R.id.btnOk);
        btnCancel = foodDialog.findViewById(R.id.btnCancel);

        addFood.setOnClickListener(this);
        btnOk.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(foodListView);

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        if (viewHolder instanceof MenuListAdapter.MyViewHolder) {

            String dTitle = menuList.get(viewHolder.getAdapterPosition()).getTitle();

            final PMenu deletedItem = menuList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            menuListAdapter.removeItem(deletedIndex);

            Snackbar snackbar = Snackbar.make(container, dTitle + "тамақтар тізімінен өшірілді!", Snackbar.LENGTH_LONG);

            snackbar.setAction("Қайтару", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menuListAdapter.restoreItem(deletedItem, deletedIndex);
                }
            });

            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
            saved = false;
            saveMenuItem.setVisible(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.absent_save_menu, menu);
        saveMenuItem = menu.findItem(R.id.action_save);
        saveMenuItem.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {

            if (menuList.size() > 0) {
                if (isNetworkAvailable(AddMenuInOrderActivity.this)) {
                    String allMenu = "";
                    for (int i = 0; i < menuList.size(); i++) {
                        allMenu += "title: " + menuList.get(i).getTitle() + " desc: " + menuList.get(i).getDesc() + " ";
                    }

                    if (editOrder) {

                        Order order = new Order("" + keyText,
                                "" + titleText,
                                "" + orderPersonNameText,
                                "" + statusText,
                                "" + dateEditTextText,
                                "" + timeText,
                                "" + personCountText,
                                "" + phoneNumberText,
                                "" + allMenu);

                        mDatabaseRef.child("orders").child(keyText).setValue(order);
                        showEditedOrderToast();

                    }else{

                        String newKey = mDatabaseRef.child("orders").push().getKey();

                        Order order = new Order("" + newKey,
                                "" + titleText,
                                "" + orderPersonNameText,
                                "" + statusText,
                                "" + dateEditTextText,
                                "" + timeText,
                                "" + personCountText,
                                "" + phoneNumberText,
                                "" + allMenu);

                        mDatabaseRef.child("orders").child(newKey).setValue(order);
                        showSuccessToast();

                    }

                    saved = true;
                    onBackPressed();

                } else {
                    Toast.makeText(AddMenuInOrderActivity.this, "Check internet connection", Toast.LENGTH_SHORT).show();
                }
            }

            saveMenuItem.setVisible(false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void showEditedOrderToast() {
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_edited_toast, (ViewGroup) findViewById(R.id.custom_toast_layout));
        TextView text = toastLayout.findViewById(R.id.custom_toast_message);
        text.setText("Тапсырыс сәтті өзгертілді!");

        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        toast.show();
    }

    public void showSuccessToast() {
        LayoutInflater inflater = getLayoutInflater();
        View toastLayout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.custom_toast_layout));
        TextView text = toastLayout.findViewById(R.id.custom_toast_message);
        text.setText("Жаңа тапсырыс сәтті енгізілді!");

        Toast toast = new Toast(this);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(toastLayout);
        toast.show();
    }

    @Override
    public void onBackPressed() {

        if (!saved) {
            Toast.makeText(AddMenuInOrderActivity.this, "Енгізілген меню сақталған жоқ!", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                foodDialog.dismiss();
                break;

            case R.id.btnAddMenu:
                if(checkInetConnection()) foodDialog.show();
                break;

            case R.id.btnOk:
                String titleText = foodTitle.getText().toString();
                String descText = foodDesc.getText().toString();
                boolean hasEmpty = true;

                if (titleText.length() == 0) {
                    foodTitle.setError("толтыруды ұмытпаңыз");
                    hasEmpty = false;
                }
                if (descText.length() == 0) {
                    foodDesc.setError("толтыруды ұмытпаңыз");
                    hasEmpty = false;
                }

                if (hasEmpty) {

                    menuList.add(new PMenu(titleText, descText));
                    foodTitle.getText().clear();
                    foodDesc.getText().clear();

                    menuListAdapter.notifyDataSetChanged();
                    foodDialog.dismiss();
                    saveMenuItem.setVisible(true);
                    saved = false;
                }

                break;
        }
    }

    public boolean checkInetConnection(){
        if(isNetworkAvailable(this)){
            return true;
        }else{
            Toast.makeText(this, "Интернет байланысыңызды тексеріңіз", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}
