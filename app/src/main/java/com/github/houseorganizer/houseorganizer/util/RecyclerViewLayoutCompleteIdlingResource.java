package com.github.houseorganizer.houseorganizer.util;

import androidx.test.espresso.IdlingResource;

public class RecyclerViewLayoutCompleteIdlingResource implements IdlingResource {

    private ResourceCallback resourceCallback;
    private RecyclerViewIdlingCallback recyclerViewIdlingCallback;
    private RecyclerViewLayoutCompleteListener listener;

    public RecyclerViewLayoutCompleteIdlingResource(RecyclerViewIdlingCallback recyclerViewIdlingCallback){
        this.recyclerViewIdlingCallback = recyclerViewIdlingCallback;

        listener = new RecyclerViewLayoutCompleteListener() {

            @Override
            public void onLayoutCompleted() {
                if (resourceCallback == null){
                    return ;
                }
                if (listener != null) {
                    recyclerViewIdlingCallback.removeRecyclerViewLayoutCompleteListener(listener);
                }
                //Called when the resource goes from busy to idle.
                resourceCallback.onTransitionToIdle();
            }
        };

        // add the listener to the view containing the recyclerview
        recyclerViewIdlingCallback.setRecyclerViewLayoutCompleteListener (listener);
    }
    @Override
    public String getName() {
        return "RecyclerViewLayoutCompleteIdlingResource";
    }

    @Override
    public boolean isIdleNow() {
        return recyclerViewIdlingCallback.isRecyclerViewLayoutCompleted();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }
}
