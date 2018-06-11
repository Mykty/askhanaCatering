package com.example.mykty.askhanacatering.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;
import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.activity.AddMenuInOrderActivity;
import com.example.mykty.askhanacatering.module.Order;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.MyViewHolder>{

    private ArrayList<Order> orders;
    Activity activity;
    Order deletedOrder = null;
    int deletedIndex = 0;
    String deletedKey;
    DatabaseReference mDatabaseRef;

    CalendarDatePickerDialogFragment cdp;
    RadialTimePickerDialogFragment rtpd;
    CalendarDatePickerDialogFragment.OnDateSetListener onDateSetListener2;
    RadialTimePickerDialogFragment.OnTimeSetListener onTimeSetListener2;
    int mYear, mMonth, mDay, mHour, mMinute;
    String monthNames [] = {"0", "январь","февраль","март","апрель","май","июнь","июль","август","сентябрь","октябрь","ноябрь","декабрь"};
    Dialog addNewOrderDialog;
    TextInputEditText titleEditText, orderPersonNameEditText , dateEditText, timeEditText, personCountEditText, phoneNumberEditText;
    Button btnOk, btnCancel;
    String FRAG_TAG_DATE_PICKER = "date_picker";
    Intent menuIntent;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView orderPersonName;
        TextView date;
        TextView status;
        TextView personCount;
        TextView phoneNumber;
        TextView iconText;
        RelativeLayout relativeLayout;
        LinearLayout childLayout;
        public TextView buttonViewOption;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.oTitle);
            this.orderPersonName = itemView.findViewById(R.id.oPerson);
            this.status = itemView.findViewById(R.id.oStatus);
            this.date = itemView.findViewById(R.id.oDate);
            this.personCount = itemView.findViewById(R.id.oCount);
            this.phoneNumber = itemView.findViewById(R.id.oPhoneNumber);

            this.iconText = itemView.findViewById(R.id.iconText);

            this.buttonViewOption = itemView.findViewById(R.id.textViewOptions);
            this.relativeLayout = itemView.findViewById(R.id.relativeItem);
            this.childLayout = itemView.findViewById(R.id.childLayout);
        }
    }

    public OrderListAdapter(Activity activity, ArrayList<Order> orders) {
        this.activity = activity;
        this.orders = orders;


        FirebaseApp.initializeApp(activity);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        createDialogs();
        menuIntent = new Intent(activity, AddMenuInOrderActivity.class);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_list, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Order order = orders.get(position);
        holder.title.setText(order.getTitle());
        holder.orderPersonName.setText(order.getOrderPersonName());
        holder.status.setText(order.getStatus());
        holder.date.setText(order.getDate() + ", " + order.getTime());
        holder.personCount.setText(order.getPersonCount() + " адам");
        holder.phoneNumber.setText(order.getPhoneNumber());

        holder.iconText.setText("" + order.getTitle().charAt(0));

        if (order.getStatus().equals("жаңа"))
            holder.status.setTextColor(activity.getResources().getColor(R.color.red));
        if (order.getStatus().equals("қабылданды"))
            holder.status.setTextColor(activity.getResources().getColor(R.color.tabBack2));
        if (order.getStatus().equals("дайын"))
            holder.status.setTextColor(activity.getResources().getColor(R.color.green));

        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkInetConnection()) goToMenuActivity(order);
            }
        });

        holder.orderPersonName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkInetConnection()) goToMenuActivity(order);
            }
        });

        holder.iconText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkInetConnection()) goToMenuActivity(order);
            }
        });


        holder.childLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPhoneDialog(order);
            }
        });

        holder.buttonViewOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkInetConnection()) {

                    PopupMenu popup = new PopupMenu(activity, holder.buttonViewOption);
                    popup.inflate(R.menu.options_menu);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.menu1:
                                    showEditDialog(order);
                                    break;
                                case R.id.menu2:

                                    deletedOrder = order;
                                    deletedIndex = position;
                                    deletedKey = order.getKeys();

                                    Snackbar snackbar = Snackbar.make(holder.relativeLayout, "Removed: " + order.getTitle(), Snackbar.LENGTH_LONG);

                                    delFromFirebase(deletedKey, position);

                                    snackbar.setAction("Қайтару", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            restoreItem(deletedOrder, deletedIndex);
                                            undoToFirebase(deletedKey, deletedOrder);
                                        }
                                    });
                                    snackbar.setActionTextColor(Color.YELLOW);
                                    snackbar.show();

                                    Toast.makeText(activity, "" + order.getOrderPersonName(), Toast.LENGTH_SHORT).show();

                                    break;
                            }
                            return false;
                        }
                    });

                    popup.show();
                }

            }
        });

    }


    public void goToMenuActivity(Order cOrder){

        menuIntent.putExtra("title", cOrder.getTitle());
        menuIntent.putExtra("orderPersonName", cOrder.getOrderPersonName());
        menuIntent.putExtra("dateEditText", cOrder.getDate());
        menuIntent.putExtra("time", cOrder.getTime());
        menuIntent.putExtra("personCount", cOrder.getPersonCount());
        menuIntent.putExtra("phoneNumber", cOrder.getPhoneNumber());

        menuIntent.putExtra("status", cOrder.getStatus());
        menuIntent.putExtra("menu", cOrder.getMenu());
        menuIntent.putExtra("keys", cOrder.getKeys());

        activity.startActivity(menuIntent);

    }
    public void showEditDialog(final Order cOrder){

        titleEditText.setText(cOrder.getTitle());
        orderPersonNameEditText.setText(cOrder.getOrderPersonName());
        dateEditText.setText(cOrder.getDate());
        timeEditText.setText(cOrder.getTime());
        personCountEditText.setText(cOrder.getPersonCount());
        phoneNumberEditText.setText(cOrder.getPhoneNumber());

        addNewOrderDialog.setCancelable(false);
        addNewOrderDialog.setCanceledOnTouchOutside(false);
        addNewOrderDialog.show();

        btnOk.setText("OK");

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String titleText = titleEditText.getText().toString();
                String orderPersonNameText = orderPersonNameEditText.getText().toString();
                String dateEditTextText = dateEditText.getText().toString();
                String timeText = timeEditText.getText().toString();
                String personCountText = personCountEditText.getText().toString();
                String phoneNumberText = phoneNumberEditText.getText().toString();

                boolean hasEmpty = true;

                if (titleText.length() == 0) {
                    titleEditText.setError("толтыруды ұмытпаңыз");
                    hasEmpty = false;
                }
                if (orderPersonNameText.length() == 0) {
                    orderPersonNameEditText.setError("толтыруды ұмытпаңыз");
                    hasEmpty = false;
                }
                if (dateEditTextText.length() == 0) {
                    dateEditText.setError("толтыруды ұмытпаңыз");
                    hasEmpty = false;
                }
                if (timeText.length() == 0) {
                    timeEditText.setError("толтыруды ұмытпаңыз");
                    hasEmpty = false;
                }
                if (personCountText.length() == 0) {
                    personCountEditText.setError("толтыруды ұмытпаңыз");
                    hasEmpty = false;
                }
                if (phoneNumberText.length() == 0) {
                    phoneNumberEditText.setError("толтыруды ұмытпаңыз");
                    hasEmpty = false;
                }

                if (hasEmpty) {

                    Order editedOrder = new Order(""+cOrder.getKeys(),
                            "" + titleText,
                            "" + orderPersonNameText,
                            ""+cOrder.getStatus(),
                            "" + dateEditTextText,
                            "" + timeText,
                            "" + personCountText,
                            "" + phoneNumberText,
                            ""+cOrder.getMenu());

                    mDatabaseRef.child("orders").child(cOrder.getKeys()).setValue(editedOrder);

                    addNewOrderDialog.dismiss();

                }
            }
        });

        final FragmentManager manager = ((AppCompatActivity)activity).getSupportFragmentManager();

        dateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(hasFocus) cdp.show(manager, FRAG_TAG_DATE_PICKER);

            }
        });
        timeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(hasFocus) rtpd.show(manager, FRAG_TAG_DATE_PICKER);

            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewOrderDialog.dismiss();
            }
        });
    }

    public void createDialogs() {
        Calendar calendar = Calendar.getInstance();
        mYear = calendar.get(Calendar.YEAR);
        mMonth = calendar.get(Calendar.MONTH);
        mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mHour = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);


        addNewOrderDialog = new Dialog(activity);
        addNewOrderDialog.setTitle("Жаңа тапсырыс енгізу");
        addNewOrderDialog.setContentView(R.layout.dialog_new_order);

        titleEditText = addNewOrderDialog.findViewById(R.id.oTitle);
        orderPersonNameEditText = addNewOrderDialog.findViewById(R.id.oPersonName);
        dateEditText = addNewOrderDialog.findViewById(R.id.oDate);
        timeEditText = addNewOrderDialog.findViewById(R.id.oTime);
        personCountEditText = addNewOrderDialog.findViewById(R.id.oPersonCount);
        phoneNumberEditText = addNewOrderDialog.findViewById(R.id.oPhoneNumber);

        btnOk = addNewOrderDialog.findViewById(R.id.btnMenu);
        btnCancel = addNewOrderDialog.findViewById(R.id.btnCancel);

        onDateSetListener2 = new CalendarDatePickerDialogFragment.OnDateSetListener() {
            @Override
            public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {

                String dayText, monthText;

                if (dayOfMonth <= 9) {
                    dayText = "0" + dayOfMonth;
                } else
                    dayText = "" + dayOfMonth;

                monthText = monthNames[(monthOfYear + 1)];


                dateEditText.setText(dayText + " " + monthText);
            }
        };

        cdp = new CalendarDatePickerDialogFragment()
                .setOnDateSetListener(onDateSetListener2)
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setPreselectedDate(mYear, mMonth, mDay)
                .setThemeLight()
                .setDoneText("OK")
                .setCancelText("CANCEL");

        onTimeSetListener2 = new RadialTimePickerDialogFragment.OnTimeSetListener(){
            @Override
            public void onTimeSet(RadialTimePickerDialogFragment view, int hourOfDay, int minute) {
                String hourText, minuteText;

                if(hourOfDay <= 9){
                    hourText = "0"+hourOfDay;
                }else{
                    hourText = ""+hourOfDay;
                }

                if(minute <= 9){
                    minuteText = "0"+minute;
                }else{
                    minuteText = ""+minute;
                }
                timeEditText.setText(hourText + ":" + minuteText);
            }
        };

        rtpd = new RadialTimePickerDialogFragment()
                .setOnTimeSetListener(onTimeSetListener2)
                .setFutureMinutesLimit(60)
                .setPastMinutesLimit(60)
                .setForced24hFormat()
                .setThemeLight()
                .setDoneText("OK")
                .setCancelText("Cancel");
    }

    public void delFromFirebase(String key, int pos){
        mDatabaseRef.child("orders").child(key).removeValue();
        removeItem(pos);
    }

    public void undoToFirebase(String deletedKey, Order order){
        mDatabaseRef.child("orders").child(deletedKey).setValue(order);
    }

    public void showPhoneDialog(final Order order){

        new AlertDialog.Builder(activity, R.style.AlertDialogTheme)
                .setTitle("Тапсырушы: " + order.getOrderPersonName())
                .setMessage("Телефон номері: " + order.getPhoneNumber())
                .setPositiveButton("Хабарласу", new DialogInterface.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + order.getPhoneNumber()));
                        activity.startActivity(callIntent);
                    }
                })
                .setNegativeButton("Смс жазу", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Uri uri = Uri.parse("smsto:" + order.getPhoneNumber());
                        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
                        smsIntent.putExtra("sms_body", "СДКЛ асхана: Құрметті " + order.getOrderPersonName() + " cіздің тапсырыс "+order.getStatus());
                        activity.startActivity(smsIntent);
                    }
                })
                .show();
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void removeItem(int position) {
        orders.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Order order, int position) {
        orders.add(position, order);
        notifyItemInserted(position);
    }

    public boolean checkInetConnection(){
        if(isNetworkAvailable(activity)){
            return true;
        }else{
            Toast.makeText(activity, "Интернет байланысыңызды тексеріңіз", Toast.LENGTH_SHORT).show();
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