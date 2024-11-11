package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up "Get Started" button
        Button getStartedButton = findViewById(R.id.btnGetStarted);
        if (getStartedButton != null) {
            getStartedButton.setOnClickListener(v -> {
                Toast.makeText(MainActivity.this, "Button clicked!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            });
        }

        // Set up "Have an account? Login" text
        TextView loginTextView = findViewById(R.id.tvLogin);
        if (loginTextView != null) {
            loginTextView.setOnClickListener(v -> {
                // Navigate to LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            });
        }
    }
}

