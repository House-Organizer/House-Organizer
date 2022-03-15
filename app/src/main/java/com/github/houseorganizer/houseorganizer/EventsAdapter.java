package com.github.houseorganizer.houseorganizer;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import com.github.houseorganizer.houseorganizer.Calendar.*;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    private static final int DAYS_PER_WEEK = 7;
    Calendar calendar;
    public EventsAdapter(Calendar calendar) {
        this.calendar = calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Button titleView;
        public TextView dateView;
        public ViewHolder(View eventView) {
            super(eventView);

            switch (calendar.getView()) {
                case MONTHLY:
                    titleView = eventView.findViewById(R.id.event_monthly_title);
                    dateView = null;
                    break;
                case WEEKLY:
                    titleView = eventView.findViewById(R.id.event_weekly_title);
                    dateView = null;
                    break;
                case UPCOMING:
                    titleView = eventView.findViewById(R.id.event_upcoming_title);
                    dateView = eventView.findViewById(R.id.event_upcoming_date);
            }
        }
    }

    @NonNull
    @Override
    public EventsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        switch (calendar.getView()) {
            case MONTHLY:
                return new ViewHolder(inflater.inflate(R.layout.calendar_monthly_cell, parent, false));
            case WEEKLY:
                return new ViewHolder(inflater.inflate(R.layout.calendar_weekly_row, parent, false));
            default:
                return new ViewHolder(inflater.inflate(R.layout.calendar_upcoming_row, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(EventsAdapter.ViewHolder holder, int position) {
        Button titleView = holder.titleView;
        switch (calendar.getView()) {
            case MONTHLY:
                titleView.setText(String.format(Locale.ENGLISH, "%d", position + 1));
                titleView.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                        .setTitle(Integer.toString(position + 1)).setMessage("List of events for this day somehow")
                        .setMessage("List of events for this day somehow").show());
                break;
            case WEEKLY:
                String[] days = new String[]{"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};

                titleView.setText(days[position]);
                titleView.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                        .setTitle(days[position])
                        .setMessage("List of events for this day somehow").show());
                break;
            case UPCOMING:
                Event event = calendar.getEvents().get(position);
                titleView.setText(event.getTitle());
                titleView.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                        .setTitle(event.getTitle())
                        .setMessage(event.getDescription()).show());
                TextView dateView = holder.dateView;
                dateView.setText(dateView.getContext().getResources().getString(R.string.calendar_upcoming_date,
                        event.getStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        }
    }

    @Override
    public int getItemCount() {
        switch (calendar.getView()) {
            case MONTHLY:
                return YearMonth.of(LocalDate.now().getYear(), LocalDate.now().getMonth()).lengthOfMonth();
            case WEEKLY:
                return DAYS_PER_WEEK;
            default:
                return calendar.getEvents().size();
        }
    }
}
