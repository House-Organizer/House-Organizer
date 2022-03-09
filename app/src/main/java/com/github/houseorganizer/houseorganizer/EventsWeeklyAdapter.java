package com.github.houseorganizer.houseorganizer;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class EventsWeeklyAdapter extends RecyclerView.Adapter<EventsWeeklyAdapter.ViewHolder> {

    private final List<String> days;

    public EventsWeeklyAdapter() {
        this.days = Arrays.asList("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Button titleView;

        public ViewHolder(View eventView) {
            super(eventView);

            titleView = eventView.findViewById(R.id.event_weekly_title);
        }
    }

    @NonNull
    @Override
    public EventsWeeklyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View eventView = inflater.inflate(R.layout.calendar_weekly_row, parent, false);

        return new ViewHolder(eventView);
    }

    @Override
    public void onBindViewHolder(EventsWeeklyAdapter.ViewHolder holder, int position) {
        String day = days.get(position);

        Button titleView = holder.titleView;
        titleView.setText(day);
        titleView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(day);
            builder.setMessage("List of events for this day somehow");
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }
}
