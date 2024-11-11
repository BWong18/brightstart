package com.example.brightstart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendResetInstruction;
    private FirebaseAuth mAuth;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Find views by ID
        etEmail = findViewById(R.id.etEmail);
        btnSendResetInstruction = findViewById(R.id.btn_send_reset_instruction);
        backButton = findViewById(R.id.back_button);  // Find the back button by its ID

        // Set up button click listener for password reset
        btnSendResetInstruction.setOnClickListener(v -> sendPasswordResetEmail());

        // Set up back button click listener
        backButton.setOnClickListener(v -> {
            // Go back to the previous activity or close the current one
            onBackPressed();
        });
    }

    private void sendPasswordResetEmail() {
        String email = etEmail.getText().toString().trim();

        // Validate email input
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        // Send reset email using Firebase Authentication
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Notify user that the email was sent successfully
                        Toast.makeText(ForgotPassword.this, "Password reset instructions sent to your email!", Toast.LENGTH_SHORT).show();
                        // Redirect to login page
                        navigateToLoginPage();
                    } else {
                        // Notify user of failure
                        Toast.makeText(ForgotPassword.this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToLoginPage() {
        Intent intent = new Intent(ForgotPassword.this, LoginActivity.class); // Assuming your login activity is named LoginActivity
        startActivity(intent);
        finish();
    }
}
