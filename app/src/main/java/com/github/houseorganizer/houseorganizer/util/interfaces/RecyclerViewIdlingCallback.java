package com.github.houseorganizer.houseorganizer.util.interfaces;

public interface RecyclerViewIdlingCallback {

    public void setRecyclerViewLayoutCompleteListener(RecyclerViewLayoutCompleteListener listener);

    public void removeRecyclerViewLayoutCompleteListener(RecyclerViewLayoutCompleteListener listener);

    /**
     * Callback for the idling resource to check if the resource (in this example the activity
     * containing the recyclerview) is idle
     */
    public boolean isRecyclerViewLayoutCompleted();

}
