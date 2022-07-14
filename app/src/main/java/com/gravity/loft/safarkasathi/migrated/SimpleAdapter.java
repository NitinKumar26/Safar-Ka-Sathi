package com.gravity.loft.safarkasathi.migrated;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.gravity.loft.safarkasathi.R;

import java.util.Map;

public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.ViewHoder> {

    Map<String, String> data;
    Callback callback;

    public SimpleAdapter(Map<String, String> data, Callback callback) {
        this.data = data;
        this.callback = callback;
    }

    @NonNull
    @Override
    public SimpleAdapter.ViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHoder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple, parent, false));
    }

    @Override
    public void onBindViewHolder( SimpleAdapter.ViewHoder holder, int position) {
        holder.text.setText((String) data.keySet().toArray()[position]);
    }

    @Override
    public int getItemCount() {
        return data.values().size();
    }

    public class ViewHoder extends RecyclerView.ViewHolder {
        TextView text;
        View checkBtn;

        public ViewHoder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text_2);
            checkBtn = itemView.findViewById(R.id.check);
            checkBtn.setOnClickListener(view -> {
                String f = (String) data.keySet().toArray()[getAdapterPosition()];
                String s = (String) data.values().toArray()[getAdapterPosition()];
                callback.onSelect(f, s);
            });
        }
    }

    public interface Callback{
        public void onSelect(String key, String value);
    }
}