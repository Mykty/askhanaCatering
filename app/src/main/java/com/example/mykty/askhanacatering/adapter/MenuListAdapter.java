package com.example.mykty.askhanacatering.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mykty.askhanacatering.R;
import com.example.mykty.askhanacatering.module.PMenu;

import java.util.ArrayList;

public class MenuListAdapter extends RecyclerView.Adapter<MenuListAdapter.MyViewHolder> {

    private ArrayList<PMenu> dataSet;
    Activity activity;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView desc;
        public RelativeLayout viewBackground;
        public LinearLayout viewForeground;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.title  = itemView.findViewById(R.id.mTitle);
            this.desc   = itemView.findViewById(R.id.mDesc);

            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.relativeItem);

        }
    }

    public MenuListAdapter(Activity activity, ArrayList<PMenu> data) {
        this.dataSet = data;
        this.activity = activity;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_list, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        PMenu menu = dataSet.get(position);
        holder.title.setText(menu.getTitle());
        holder.desc.setText(menu.getDesc());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public void removeItem(int position) {
        dataSet.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(PMenu menu, int position) {
        dataSet.add(position, menu);
        notifyItemInserted(position);
    }

}