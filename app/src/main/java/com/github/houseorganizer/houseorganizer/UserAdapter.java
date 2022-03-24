package com.github.houseorganizer.houseorganizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserHolder> {

    final Context cx;
    final List<String> usersEmail;

    public UserAdapter(Context cx, List<String> usersName) {
        this.cx = cx;
        this.usersEmail = usersName;
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
        holder.userEmail.setText(usersEmail.get(position));
    }

    @Override
    public int getItemCount() {
        return usersEmail.size();
    }

    public static class UserHolder extends RecyclerView.ViewHolder {
        final TextView userEmail;

        public UserHolder(@NonNull View itemView) {
            super(itemView);
            userEmail = itemView.findViewById(R.id.houseName);
        }
    }
}