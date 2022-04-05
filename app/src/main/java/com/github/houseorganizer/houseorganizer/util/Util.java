package com.github.houseorganizer.houseorganizer.util;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.houseorganizer.houseorganizer.R;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.Objects;

public class Util {
    public static boolean putEventStringsInData(Map<String, String> event, Map<String, Object> data) {
        data.put("title", event.get("title"));
        data.put("description", event.get("desc"));
        try {
            TemporalAccessor start = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").parse(event.get("date"));
            data.put("start", LocalDateTime.from(start).toEpochSecond(ZoneOffset.UTC));
            data.put("duration", Integer.valueOf(Objects.requireNonNull(event.get("duration"))));
        } catch(Exception e) {
            return true;
        }
        return false;
    }


}
