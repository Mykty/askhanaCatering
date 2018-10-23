package com.example.mykty.askhanacatering.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.activity.StudentCabinet;
import com.example.mykty.askhanacatering.module.Personnel;
import com.example.mykty.askhanacatering.module.Student;

import java.util.ArrayList;
import java.util.HashMap;

public class PersonnelListAdapter extends RecyclerView.Adapter<PersonnelListAdapter.MyViewHolder> {

    private ArrayList<Personnel> dataSet;
    Activity activity;
    HashMap<String, Student> idNumberHashMap;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView perName, otherDesc;
        ImageView perImage;
        LinearLayout linearLayout;
        public RelativeLayout viewBackground, viewForeground;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.perName = itemView.findViewById(R.id.perName);
            this.perImage = itemView.findViewById(R.id.perImage);
            this.linearLayout = itemView.findViewById(R.id.linearL);

            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.relativeItem);
        }
    }

    public PersonnelListAdapter(Activity activity, ArrayList<Personnel> data) {
        this.activity = activity;
        this.dataSet = data;
    }
    public PersonnelListAdapter(Activity activity, ArrayList<Personnel> data, HashMap<String, Student> idNumberHashMap) {
        this.activity = activity;
        this.dataSet = data;
        this.idNumberHashMap = idNumberHashMap;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.personalle_adapter_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Personnel item = dataSet.get(position);
        holder.perName.setText(item.getInfo());

        if (item.getType().equals("others")) {

            holder.perName.setText(holder.perName.getText()+"\n"+item.getPhoto());
            /*
            holder.linearLayout.removeAllViews();

            holder.perName = new TextView(activity);
            holder.otherDesc = new TextView(activity);

            holder.perName.setTextColor(activity.getResources().getColor(R.color.black));
            holder.perName.setTextSize(20f);

            holder.otherDesc.setTextColor(activity.getResources().getColor(R.color.grey));
            holder.otherDesc.setTextSize(15f);


            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            p.weight = 1;

            holder.perName.setLayoutParams(p);
            holder.otherDesc.setLayoutParams(p);

            holder.perName.setText(item.getInfo());
            holder.otherDesc.setText(item.getPhoto());

            holder.linearLayout.addView(holder.perName);
            holder.linearLayout.addView(holder.otherDesc);
            */
        }

        Glide.with(activity)
                .load(item.getPhoto())
                .placeholder(R.drawable.s_icon).fitCenter()
                .into(holder.perImage);

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idNumberHashMap != null) {
                    String clickedSName = item.getInfo().toString();

                    Intent intent = new Intent(activity, StudentCabinet.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "college");
                    bundle.putSerializable("sClass", idNumberHashMap.get(clickedSName));
                    intent.putExtras(bundle);
                    activity.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void removeItem(int position) {
        dataSet.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Personnel student, int position) {
        dataSet.add(position, student);
        notifyItemInserted(position);
    }
}