package com.github.houseorganizer.houseorganizer;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Locale;

public class EventsMonthlyAdapter extends RecyclerView.Adapter<EventsMonthlyAdapter.ViewHolder> {

    public EventsMonthlyAdapter() {
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Button titleView;

        public ViewHolder(View eventView) {
            super(eventView);

            titleView = eventView.findViewById(R.id.event_monthly_title);
        }
    }

    @NonNull
    @Override
    public EventsMonthlyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View eventView = inflater.inflate(R.layout.calendar_monthly_cell, parent, false);

        return new ViewHolder(eventView);
    }

    @Override
    public void onBindViewHolder(EventsMonthlyAdapter.ViewHolder holder, int position) {

        Button titleView = holder.titleView;
        titleView.setText(String.format(Locale.ENGLISH, "%d", position + 1));
        titleView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(Integer.toString(position + 1));
            builder.setMessage("List of events for this day somehow");
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth()).lengthOfMonth();
    }
}
