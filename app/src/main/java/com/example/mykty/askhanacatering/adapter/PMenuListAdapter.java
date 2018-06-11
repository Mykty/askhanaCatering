package com.example.mykty.askhanacatering.adapter;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.module.PMenu;

import java.util.ArrayList;

public class PMenuListAdapter extends RecyclerView.Adapter<PMenuListAdapter.MyViewHolder> {

    private ArrayList<PMenu> dataSet;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitle;
        TextView textVCount;
        ImageView typeIcon;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.textViewTitle    = itemView.findViewById(R.id.textViewTitle);
            this.textVCount = itemView.findViewById(R.id.tVCount);
            this.typeIcon   = itemView.findViewById(R.id.typeIcon);
        }
    }

    public PMenuListAdapter(ArrayList<PMenu> data) {
        this.dataSet = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.p_card_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        PMenu item = dataSet.get(position);
        holder.textViewTitle.setText(item.getTitle());
        holder.textVCount.setText(""+item.getCount());
        holder.typeIcon.setImageResource(item.getIcon());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}