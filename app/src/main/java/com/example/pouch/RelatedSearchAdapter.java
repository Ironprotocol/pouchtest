package com.example.pouch;

import androidx.annotation.NonNull;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RelatedSearchAdapter extends RecyclerView.Adapter<RelatedSearchAdapter.ViewHolder> {

    private List<String> relatedSearchList;
    private OnItemClickListener listener;

    public RelatedSearchAdapter(List<String> relatedSearchList) {
        this.relatedSearchList = relatedSearchList;
    }

    public interface OnItemClickListener {
        void onItemClick(String searchQuery);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.related_search_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String searchQuery = relatedSearchList.get(position);
        holder.bind(searchQuery);
    }

    @Override
    public int getItemCount() {
        Log.d("RelatedSearchAdapter", "Item count: " + relatedSearchList.size());
        return relatedSearchList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.relatedSearchTextView);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(relatedSearchList.get(position));
                    }
                }
            });
        }

        public void bind(String searchQuery) {
            Log.d("RelatedSearchAdapter", "Binding text: " + searchQuery);
            textView.setText(searchQuery);
        }
    }
}


