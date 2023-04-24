package edu.northeastern.markergo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.northeastern.markergo.models.CheckInHistory;

public class CheckInRecyclerViewAdapter extends RecyclerView.Adapter<CheckInRecyclerViewAdapter.TextViewHolder> {
    List<CheckInHistory> checkInList;

    public CheckInRecyclerViewAdapter(List<CheckInHistory> checkInList) {
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
        holder.textViewName.setText(checkInList.get(position).getName());
        holder.textViewCount.setText(String.format("Visited %s time", checkInList.get(position).getCount()));
        holder.textViewLastVisited.setText(String.format("Last visited on - %s", checkInList.get(position).getLastVisited()));
        if (checkInList.get(position).getLocationImageBitmap() != null) {
            holder.imageViewLocation.setImageBitmap(checkInList.get(position).getLocationImageBitmap());
        }
    }

    @Override
    public int getItemCount() {
        return checkInList.size();
    }

    class TextViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewCount;
        TextView textViewLastVisited;
        ImageView imageViewLocation;

        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.locationName);
            textViewCount = itemView.findViewById(R.id.locationCount);
            textViewLastVisited = itemView.findViewById(R.id.locationLastVist);
            imageViewLocation = itemView.findViewById(R.id.locationImage);
        }
    }
}
