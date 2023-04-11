package edu.northeastern.markergo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import edu.northeastern.markergo.utils.Util;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText nameText;
    private EditText emailText;
    private EditText passwordText;
    private EditText confirmPasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        nameText = findViewById(R.id.name);
        emailText = findViewById(R.id.email);
        passwordText = findViewById(R.id.password);
        confirmPasswordText = findViewById(R.id.confirmPassword);

        TextView loginText = findViewById(R.id.loginText);
        String text = loginText.getText().toString();
        SpannableString s = new SpannableString(text);
        s.setSpan(new UnderlineSpan(), text.indexOf("Login Here"), s.length(), 0);
        loginText.setText(s);
    }

    public void openLoginActivity(View view) {
        super.finish();
    }

    public void signup(View view) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        String confirmPassword = confirmPasswordText.getText().toString();
        String name = nameText.getText().toString();

        // check password == confirmPassword or not
        boolean isValidEmail = Util.isValidEmail(email);
        boolean isValidPassword = Util.isValidPassword(password);
        boolean passwordMatches = Util.passwordMatches(password, confirmPassword);

        if (!isValidEmail && !isValidPassword) {
            emailText.setError("Invalid email");
            passwordText.setError("Password does not satisfy all constraints");
        } else if (!isValidEmail) {
            emailText.setError("Invalid email");
        } else if (!isValidPassword) {
            passwordText.setError("Password does not satisfy all constraints");
        } else if (!passwordMatches) {
            confirmPasswordText.setError("Both password don't match");
        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    addNameToUserprofile(name);
                    displayToast("Signup successful");
                } else {
                    displayToast("Account with this email exists already!");
                }
            });
        }
    }

    private void addNameToUserprofile(String name) {
        FirebaseUser user = mAuth.getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build();
        assert user != null;
        user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                displayToast("Account created successfully!");
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            } else {
                displayToast("Error in updating user profile");
            }
        });
    }

    private void displayToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}