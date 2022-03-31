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

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.houseorganizer.houseorganizer.Calendar.Event;
import com.github.houseorganizer.houseorganizer.util.Util;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    private static final int DAYS_PER_WEEK = 7;
    private final ActivityResultLauncher<String> getPicture;

    String eventToAttach;

    Calendar calendar;
    public EventsAdapter(Calendar calendar, ActivityResultLauncher<String> getPicture) {
        this.calendar = calendar;
        this.getPicture = getPicture;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Button titleView;
        public TextView dateView = null;
        public Button attachView = null;
        public ViewHolder(View eventView) {
            super(eventView);

            switch (calendar.getView()) {
                case MONTHLY:
                    titleView = eventView.findViewById(R.id.event_monthly_title);
                    break;
                case WEEKLY:
                    titleView = eventView.findViewById(R.id.event_weekly_title);
                    break;
                case UPCOMING:
                    titleView = eventView.findViewById(R.id.event_upcoming_title);
                    dateView = eventView.findViewById(R.id.event_upcoming_date);
                    attachView = eventView.findViewById(R.id.event_upcoming_attach);
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
    public void onBindViewHolder(@NonNull EventsAdapter.ViewHolder holder, int position) {
        switch (calendar.getView()) {
            case MONTHLY:
                prepareMonthlyView(holder, position);
                break;
            case WEEKLY:
                prepareWeeklyView(holder, position);
                break;
            case UPCOMING:
                prepareUpcomingView(holder, position);

        }
    }

    private void prepareMonthlyView(ViewHolder holder, int position) {
        holder.titleView.setText(String.format(Locale.ENGLISH, "%d", position + 1));
        holder.titleView.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle(Integer.toString(position + 1)).setMessage("List of events for this day somehow")
                .setMessage("List of events for this day somehow").show());
    }

    private void prepareWeeklyView(ViewHolder holder, int position) {
        String[] days = new String[]{"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};

        holder.titleView.setText(days[position]);
        holder.titleView.setOnClickListener(v -> new AlertDialog.Builder(v.getContext())
                .setTitle(days[position])
                .setMessage("List of events for this day somehow").show());
    }

    private void prepareUpcomingView(EventsAdapter.ViewHolder holder, int position) {
        Event event = calendar.getEvents().get(position);
        holder.titleView.setText(event.getTitle());
        holder.titleView.setOnClickListener(v -> eventButtonListener(event, v));
        holder.dateView.setText(holder.dateView.getContext().getResources().getString(R.string.calendar_upcoming_date,
                event.getStart().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        holder.attachView.setOnClickListener(v -> {
            this.eventToAttach = event.getId();
            getPicture.launch("image/*");
        });
    }

    private void eventButtonListener(Event event, View v) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        new AlertDialog.Builder(v.getContext())
                .setTitle(event.getTitle())
                .setMessage(event.getDescription())
                .setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss())
                .setNegativeButton(R.string.delete, (dialog, id) -> {
                    db.collection("events")
                            .document(event.getId())
                            .delete();
                    dialog.dismiss();
                })
                .setNeutralButton(R.string.edit, (dialog, id) ->{
                    LayoutInflater inflater = LayoutInflater.from(v.getContext());
                    final View dialogView = inflater.inflate(R.layout.event_creation, null);
                    new AlertDialog.Builder(v.getContext())
                            .setTitle(R.string.event_editing_title)
                            .setView(dialogView)
                            .setPositiveButton(R.string.confirm, (editForm, editFormId) -> editEventAndDismiss(event.getId(), editForm, dialogView, db))
                            .setNegativeButton(R.string.cancel, (editForm, editFormId) -> dialog.dismiss())
                            .show();
                }).show();
    }

    private void editEventAndDismiss(String eventId, DialogInterface editForm, View dialogView, FirebaseFirestore db) {
        Map<String, Object> data = new HashMap<>();
        final String title = ((EditText) dialogView.findViewById(R.id.new_event_title)).getText().toString();
        final String desc = ((EditText) dialogView.findViewById(R.id.new_event_desc)).getText().toString();
        final String date = ((EditText) dialogView.findViewById(R.id.new_event_date)).getText().toString();
        final String duration = ((EditText) dialogView.findViewById(R.id.new_event_duration)).getText().toString();
        Map<String, String> event = new HashMap<>();
        event.put("title", title);
        event.put("desc", desc);
        event.put("date", date);
        event.put("duration", duration);
        if (Util.putEventStringsInData(event, data)) {
            editForm.dismiss();
            return;
        }
        db.collection("events").document(eventId).set(data, SetOptions.merge());
        editForm.dismiss();
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
