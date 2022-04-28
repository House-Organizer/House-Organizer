package com.github.houseorganizer.houseorganizer.NavBar;

import android.content.Context;
import android.content.Intent;

import com.github.houseorganizer.houseorganizer.panels.CalendarActivity;
import com.github.houseorganizer.houseorganizer.panels.MainScreenActivity;
import com.github.houseorganizer.houseorganizer.shop.GroceriesActivity;
import com.google.firebase.firestore.DocumentReference;

public class NavBarHelpers {

    public static Intent changeActivityIntent(String buttonText, DocumentReference currentHouse,
                                               String originActivity, Context context) {
        // Using the title and non resource strings here
        // otherwise there is a warning that ids inside a switch are non final
        if(originActivity.equals(buttonText))return null;

        switch(buttonText){
            case "Main Screen":
                return new Intent(context, MainScreenActivity.class);
            case "Calendar":
                Intent intentC = new Intent(context, CalendarActivity.class);
                intentC.putExtra("house", currentHouse.getId());
                return intentC;
            case "Groceries":
                Intent intentG = new Intent(context, GroceriesActivity.class);
                intentG.putExtra("house", currentHouse.getId());
                return intentG;
            case "Tasks":
                break;
            default:
                break;
        }
        return null;
    }
}
