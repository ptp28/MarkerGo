package edu.northeastern.markergo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private TextView signupText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signupText = findViewById(R.id.signupText);
        String text = signupText.getText().toString();
        SpannableString s = new SpannableString(text);
        s.setSpan(new UnderlineSpan(), text.indexOf("Create Here"), s.length(), 0);
        signupText.setText(s);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
    }

    public void openSignupActivity(View view) {
        startActivity(new Intent(getApplicationContext(), SignupActivity.class));
    }

    public void authenticate(View view) {
        // authenticate
    }
}