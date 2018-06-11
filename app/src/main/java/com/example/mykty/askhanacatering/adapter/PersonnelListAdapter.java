package com.example.mykty.askhanacatering.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.module.Personnel;

import java.util.ArrayList;

public class PersonnelListAdapter extends RecyclerView.Adapter<PersonnelListAdapter.MyViewHolder> {

    private ArrayList<Personnel> dataSet;
    Activity activity;

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

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.personalle_adapter_item, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        Personnel item = dataSet.get(position);
        holder.perName.setText(item.getInfo());

        if (item.getType().equals("others")) {
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
        }

        Glide.with(activity)
                .load(item.getPhoto())
                .placeholder(R.drawable.t_icon)
                .into(holder.perImage);

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