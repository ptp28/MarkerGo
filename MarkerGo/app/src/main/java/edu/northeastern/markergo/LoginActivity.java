package edu.northeastern.markergo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    private EditText emailText;
    private EditText passwordText;
    private TextView signupText;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private AuthCredential credential;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    private FirebaseFirestore db;

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
                            SignInCredential cred = oneTapClient.getSignInCredentialFromIntent(result.getData());
                            String idToken = cred.getGoogleIdToken();
                            if (idToken != null) {
                                credential = GoogleAuthProvider.getCredential(idToken, null);
                                authenticate(credential);
                            }
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }
                    }
                });
        db = FirebaseFirestore.getInstance();
    }

    public void openSignupActivity(View view) {
        startActivity(new Intent(getApplicationContext(), SignupActivity.class));
    }

    public void signInNormal(View view) {
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        credential = EmailAuthProvider.getCredential(email, password);
        authenticate(credential);
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

    public void signInWithFacebook(View view) {
        Intent intent = new Intent(getApplicationContext(), FacebookAuthActivity.class);
        startActivity(intent);
        super.finish();
    }

    protected void authenticate(AuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        displayToast("Login successful");
                        addUserToDb();
                        Intent intent = new Intent(getApplicationContext(), landingPage.class);
                        startActivity(intent);
                        super.finish();
                    } else {
                        displayToast("Authentication failed");
                    }
                });
    }

    private void addUserToDb() {
        user = mAuth.getCurrentUser();
        assert user != null;
        String uid = user.getUid();

        DocumentReference docRef = db.document("users/" + uid);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.i("status", "Listen failed.", e);
            } else if (snapshot != null && snapshot.exists()) {
                Log.i("status", "document already in users");
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("name", user.getDisplayName());
                data.put("email", user.getProviderData().get(1).getEmail());
                data.put("placesVisited", 0);

                db.collection("users")
                        .document(uid)
                        .set(data, SetOptions.merge())
                        .addOnCompleteListener(task -> Log.i("status", "added document to users"));
            }
        });
    }

    protected void displayToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}