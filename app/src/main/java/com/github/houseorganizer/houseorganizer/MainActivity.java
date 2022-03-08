package com.github.houseorganizer.houseorganizer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Constant that specifies the name of the extra data with package prefix
    public static final String EXTRA_MESSAGE = "com.github.houseorganizer.houseorganizer.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /* Called when the user taps the Send button */
    @SuppressWarnings("unused")
    public void sendMessage(View view) {
        // Get name
        EditText editText = findViewById(R.id.mainName);
        String message = editText.getText().toString();

        // An intent is an object that provides runtime binding between separate components
        // The intent represents an app's intent to do something
        Intent intent = new Intent(this, LoginActivity.class);
        // An intent can carry data types as key-value pairs called extras
        //intent.putExtra(EXTRA_MESSAGE, message);

        startActivity(intent);
    }
}