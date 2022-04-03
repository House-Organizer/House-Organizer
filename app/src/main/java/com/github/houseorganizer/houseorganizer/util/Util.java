package com.github.houseorganizer.houseorganizer.util;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Map;
import java.util.Objects;

public class Util {

    public static final String SHARED_PREFS = "com.github.houseorganizer.houseorganizer.sharedPrefs";

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

    public static SharedPreferences getSharedPrefs(Activity a) {
        return a.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getSharedPrefsEditor(Activity a) {
        SharedPreferences prefs = getSharedPrefs(a);
        return prefs.edit();
    }
}
