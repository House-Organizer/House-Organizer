package com.github.houseorganizer.houseorganizer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.Calendar.Event;
import com.github.houseorganizer.houseorganizer.util.Util;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    private static final int DAYS_PER_WEEK = 7;
    Calendar calendar;
    public EventsAdapter(Calendar calendar) {
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
                titleView.setOnClickListener(v -> eventButtonListener(event, v, position));
                TextView dateView = holder.dateView;
                dateView.setText(dateView.getContext().getResources().getString(R.string.calendar_upcoming_date,
                        event.getStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        }
    }

    private void eventButtonListener(Event event, View v, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        new AlertDialog.Builder(v.getContext())
                .setTitle(event.getTitle())
                .setMessage(event.getDescription())
                .setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss())
                .setNegativeButton(R.string.delete, (dialog, id) -> {
                    db.collection("events")
                            .document(event.getId())
                            .delete();
                    ArrayList<Event> newEvents = new ArrayList<>(calendar.getEvents());
                    newEvents.remove(event);
                    calendar.setEvents(newEvents);
                    this.notifyItemRemoved(position);
                    dialog.dismiss();
                })
                .setNeutralButton(R.string.edit, (dialog, id) ->{
                    LayoutInflater inflater = LayoutInflater.from(v.getContext());
                    final View dialogView = inflater.inflate(R.layout.event_creation, null);
                    ((EditText) dialogView.findViewById(R.id.new_event_title)).setText(event.getTitle());
                    ((EditText) dialogView.findViewById(R.id.new_event_desc)).setText(event.getDescription());
                    ((EditText) dialogView.findViewById(R.id.new_event_date)).setText(event.getStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                    ((EditText) dialogView.findViewById(R.id.new_event_duration)).setText(Long.toString(event.getDuration()));
                    new AlertDialog.Builder(v.getContext())
                            .setTitle(R.string.event_editing_title)
                            .setView(dialogView)
                            .setPositiveButton(R.string.confirm, (editForm, editFormId) -> editEventAndDismiss(event, editForm, dialogView, db, position))
                            .setNegativeButton(R.string.cancel, (editForm, editFormId) -> dialog.dismiss())
                            .show();
                }).show();
    }

    private void editEventAndDismiss(Event eventObj, DialogInterface editForm, View dialogView, FirebaseFirestore db, int position) {
        Map<String, Object> data = new HashMap<>();
        final String title = ((EditText) dialogView.findViewById(R.id.new_event_title)).getText().toString();
        final String desc = ((EditText) dialogView.findViewById(R.id.new_event_desc)).getText().toString();
        final String date = ((EditText) dialogView.findViewById(R.id.new_event_date)).getText().toString();
        final String duration = ((EditText) dialogView.findViewById(R.id.new_event_duration)).getText().toString();
        eventObj.setTitle(title);
        eventObj.setDescription(desc);
        eventObj.setStart(LocalDateTime.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        eventObj.setDuration(Integer.parseInt(duration));
        Map<String, String> event = new HashMap<>();
        event.put("title", title);
        event.put("desc", desc);
        event.put("date", date);
        event.put("duration", duration);
        if (Util.putEventStringsInData(event, data)) {
            editForm.dismiss();
            return;
        }
        db.collection("events").document(eventObj.getId()).set(data, SetOptions.merge());
        editForm.dismiss();
        this.notifyItemChanged(position);
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
