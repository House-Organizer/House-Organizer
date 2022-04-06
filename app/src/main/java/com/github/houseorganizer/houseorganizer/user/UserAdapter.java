package com.github.houseorganizer.houseorganizer.user;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    final Context cx;
    final List<String> usersName;

    public UserAdapter(Context cx, List<String> usersName) {
        this.cx = cx;
        this.usersName = usersName;
    }

    @NonNull
    @Override
    public UserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(cx);
        View view = inflater.inflate(R.layout.user_row, parent, false);
        return new UserHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserHolder holder, int position) {
        holder.userName.setText(usersName.get(position));
    }

    @Override
    public int getItemCount() {
        return usersName.size();
    }

    public static class UserHolder extends RecyclerView.ViewHolder {
        final TextView userName;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.houseName);
        }
    }
}