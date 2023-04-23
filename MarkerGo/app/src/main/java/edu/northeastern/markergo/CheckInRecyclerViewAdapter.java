package edu.northeastern.markergo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CheckInRecyclerViewAdapter extends RecyclerView.Adapter<CheckInRecyclerViewAdapter.TextViewHolder> {
    List<String> checkInList;

    public CheckInRecyclerViewAdapter(List<String> checkInList) {
        for(String s: checkInList)
        this.checkInList = checkInList;
    }

    @NonNull
    @Override
    public TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.check_in_recycler_item, parent, false);
        CheckInRecyclerViewAdapter.TextViewHolder textViewHolder = new TextViewHolder(view);
        return textViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TextViewHolder holder, int position) {
        holder.textView.setText(checkInList.get(position));
    }

    @Override
    public int getItemCount() {
        return checkInList.size();
    }

     class TextViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.checkInItemTV);
        }
    }
}
