package com.example.pouch;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "SignupActivity";
    private SharedPreferences sharedPreferences; //오토 로그인 시작점

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        findViewById(R.id.btn_signup).setOnClickListener(signupbtntouch);
        findViewById(R.id.btn_login).setOnClickListener(loginbtntouch);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    private void startSignupActivity(){
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
    View.OnClickListener signupbtntouch = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.btn_signup) {
                startSignupActivity();
            }
        }
    };

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing Activity")
                .setMessage("Are you sure you want to close this App?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();  // This will close the app
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }

    View.OnClickListener loginbtntouch = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.btn_login) {
                String email = ((EditText) findViewById(R.id.insert_email)).getText().toString();
                String password = ((EditText) findViewById(R.id.insert_password)).getText().toString();
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    startMainActivity();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                                    switch (errorCode) {
                                        case "ERROR_INVALID_EMAIL":
                                            Toast.makeText(LoginActivity.this, "The email address is badly formatted.",
                                                    Toast.LENGTH_SHORT).show();
                                            break;
                                        case "ERROR_USER_NOT_FOUND":
                                            Toast.makeText(LoginActivity.this, "There is no user corresponding to this email.",
                                                    Toast.LENGTH_SHORT).show();
                                            break;
                                        case "ERROR_WRONG_PASSWORD":
                                            Toast.makeText(LoginActivity.this, "The password is invalid for the given email.",
                                                    Toast.LENGTH_SHORT).show();
                                            break;
                                        default:
                                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                    Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }
                            }
                        });
            }
        }
    };
}