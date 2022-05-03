package com.github.houseorganizer.houseorganizer.util;

import androidx.test.espresso.idling.CountingIdlingResource;

public class EspressoIdlingResource {

    public static final String RESOURCE = "GLOBAL";
    public static CountingIdlingResource countingIdlingResource = new CountingIdlingResource(RESOURCE);

    public void increment() {
        countingIdlingResource.increment();
    }

    public void decrement() {
        if (!countingIdlingResource.isIdleNow())
            countingIdlingResource.decrement();
    }
}
