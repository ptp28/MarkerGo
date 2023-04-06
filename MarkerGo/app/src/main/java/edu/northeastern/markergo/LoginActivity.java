package edu.northeastern.markergo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {
    private EditText emailText;
    private EditText passwordText;
    private TextView signupText;
    private FirebaseAuth mAuth;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;
    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signupText = findViewById(R.id.signupText);
        String text = signupText.getText().toString();
        SpannableString s = new SpannableString(text);
        s.setSpan(new UnderlineSpan(), text.indexOf("Create Here"), s.length(), 0);
        signupText.setText(s);

        emailText = findViewById(R.id.email);
        passwordText = findViewById(R.id.password);

        mAuth = FirebaseAuth.getInstance();
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.default_web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        activityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                            String idToken = credential.getGoogleIdToken();
                            if (idToken != null) {
                                authenticateGoogle(idToken);
                            }
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void openSignupActivity(View view) {
        startActivity(new Intent(getApplicationContext(), SignupActivity.class));
    }

    public void authenticate(View view) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                displayToast("Login success!");
            } else {
                displayToast("incorrect Credentials");
            }
        });
    }

    public void authenticateGoogle(String idToken) {
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("succ", "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;
                        displayToast(user.getEmail());
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("err-auth", "signInWithCredential:failure", task.getException());
                    }
                });
    }

    public void signInWithFacebook(View view) {
        Intent intent = new Intent(getApplicationContext(), FacebookAuthActivity.class);
        startActivity(intent);
    }

    public void signInWithGoogle(View view) {
        Log.i("sup", "sup");
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, result -> {
                    IntentSenderRequest intentSenderRequest =
                            new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                    activityResultLauncher.launch(intentSenderRequest);
                })
                .addOnFailureListener(this, e -> Log.d("err-sign", e.getLocalizedMessage()));
    }

    protected void displayToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}