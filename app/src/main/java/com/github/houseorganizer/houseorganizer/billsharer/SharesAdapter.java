package com.github.houseorganizer.houseorganizer.billsharer;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SharesAdapter extends RecyclerView.Adapter<SharesAdapter.ShareHolder> {

    private final HashMap<String, Double> shares;
    private final HashMap<String, Boolean> modified;
    private final List<String> user_emails;

    public SharesAdapter(HashMap<String, Double> shares) {
        this.shares = shares;
        user_emails = new ArrayList<>();
        modified = new HashMap<>();
        shares.forEach((k, v) -> user_emails.add(k));
        shares.forEach((k, v) -> modified.put(k, false));
    }

    @NonNull
    @Override
    public ShareHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.share_row, parent, false);
        return new ShareHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ShareHolder holder, int position) {
        String user = user_emails.get(position);
        holder.user.setText(user);
        holder.amount.setText(Objects.requireNonNull(shares.get(user)).toString());
        holder.amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                modified.put(user, true);
                updateShares();
            }
        });
    }

    private void updateShares() {

    }

    @Override
    public int getItemCount() {
        return user_emails.size();
    }

    public static class ShareHolder extends RecyclerView.ViewHolder {
        public TextView user;
        public EditText amount;

        public ShareHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
