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
    private final double cost;
    private double subTotal;
    private int numModified;

    public SharesAdapter(HashMap<String, Double> shares, double cost) {
        this.shares = shares;
        this.cost = cost;
        subTotal = cost;
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
        holder.amount.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                shares.put(user, Double.parseDouble(holder.amount.getText().toString()));
                modified.put(user, true);
                updateShares();
                notifyItemRangeChanged(0, getItemCount());
            }
        });
    }

    private void updateShares() {
        updateSubTotal();
        for (String user : user_emails) {
            if (Boolean.FALSE.equals(modified.get(user))) {
                shares.put(user, subTotal/(getItemCount()-numModified));
            }
        }
    }

    private void updateSubTotal() {
        double res = cost;
        numModified = 0;
        for (String user : user_emails) {
            if (Boolean.TRUE.equals(modified.get(user))) {
                res = res - shares.get(user);
                numModified++;
            }
        }
        subTotal = res;
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
            user = itemView.findViewById(R.id.share_user_email);
            amount = itemView.findViewById(R.id.share_amount);
        }
    }
}
