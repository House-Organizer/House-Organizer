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

import com.github.houseorganizer.houseorganizer.Calendar.Event;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.ViewHolder> {

    private List<Event> events;

    public EventsAdapter(List<Event> events) {
        this.events = events;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Button titleView;
        public TextView dateView;

        public ViewHolder(View eventView) {
            super(eventView);

            titleView = eventView.findViewById(R.id.event_title);
            dateView = eventView.findViewById(R.id.event_date);
        }
    }

    @NonNull
    @Override
    public EventsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View eventView = inflater.inflate(R.layout.calendar_row, parent, false);

        return new ViewHolder(eventView);
    }

    @Override
    public void onBindViewHolder(EventsAdapter.ViewHolder holder, int position) {
        Event event = events.get(position);

        Button titleView = holder.titleView;
        titleView.setText(event.getTitle());
        titleView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(event.getTitle());
            builder.setMessage(event.getDescription());
            builder.show();
        });
        TextView dateView = holder.dateView;
        dateView.setText(event.getStart().toString());
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
