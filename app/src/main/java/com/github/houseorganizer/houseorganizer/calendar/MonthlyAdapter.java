package com.github.houseorganizer.houseorganizer.calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.R;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MonthlyAdapter extends CalendarAdapter {

    private ArrayList<ArrayList<Calendar.Event>> items;

    public MonthlyAdapter(Calendar calendar, ActivityResultLauncher<String> getPicture) {
        super(calendar, getPicture);
    }

    @Override
    public CalendarAdapter switchView() {
        return new UpcomingAdapter(calendar, getPicture);
    }

    @Override
    void generateItems(List<Calendar.Event> events) {
        ArrayList<ArrayList<Calendar.Event>> ret = new ArrayList<>();
        for (int i = 0; i < YearMonth.now().lengthOfMonth(); i++) {
            ret.add(new ArrayList<>());
        }
        for (int i = 0; i < events.size(); i++) {
            if (!(events.get(i).getStart().toLocalDate().getMonthValue() == YearMonth.now().getMonthValue()
                && events.get(i).getStart().toLocalDate().getYear() == YearMonth.now().getYear())) {
                continue;
            }
            ret.get(events.get(i).getStart().getDayOfMonth()-1).add(events.get(i));
        }
        items = ret;
    }

    public static class MonthlyDayViewHolder extends RecyclerView.ViewHolder {
        public Button titleView;
        public MonthlyDayViewHolder(View eventView) {
            super(eventView);
            titleView = eventView.findViewById(R.id.event_monthly_title);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        return new MonthlyDayViewHolder(inflater.inflate(R.layout.calendar_monthly_cell, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        prepareMonthlyView((MonthlyDayViewHolder) holder, position);
    }

    private void prepareMonthlyView(MonthlyDayViewHolder holder, int position) {
        if (items.get(position).isEmpty()) {
            holder.titleView.setText(String.format(Locale.ENGLISH, "%d", position + 1));
            holder.titleView.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                    .setMessage(R.string.no_events)
                    .show());
        }
        else {
            holder.titleView.setText(String.format(Locale.ENGLISH, "%d\n\n!", position + 1));

            holder.titleView.setOnClickListener(v -> {
                RecyclerView calendarView = new RecyclerView(v.getContext());
                Calendar dayCalendar = new Calendar();
                dayCalendar.setEvents(items.get(position));
                UpcomingAdapter adapter = new UpcomingAdapter(dayCalendar, getPicture);
                calendarView.setAdapter(adapter);
                calendarView.setLayoutManager(new GridLayoutManager(v.getContext(), 1));
                new AlertDialog.Builder(v.getContext())
                        .setView(calendarView)
                        .show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
