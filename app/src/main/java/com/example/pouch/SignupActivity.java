package com.example.pouch;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.appcompat.app.AlertDialog;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "SignupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        findViewById(R.id.btn_signup).setOnClickListener(signupbtntouch);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Back")
                .setMessage("Back to Login page?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }
    private void signup() {
        String email = ((EditText) findViewById(R.id.insert_email)).getText().toString();
        String password = ((EditText) findViewById(R.id.insert_password)).getText().toString();
        String passwordConfirm = ((EditText) findViewById(R.id.insert_password_confirm)).getText().toString();

        if (!password.equals(passwordConfirm)) {
            Toast.makeText(SignupActivity.this, "Password are not same", Toast.LENGTH_SHORT).show();
            return;
            }else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            {
                Toast.makeText(SignupActivity.this, "E-mail patterns are not correct", Toast.LENGTH_SHORT).show();
                return;
            }else if (password.length() + passwordConfirm.length() <= 12)
            {
            Toast.makeText(SignupActivity.this, "Password needs at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
            }
            else if (password.equals(passwordConfirm))
             {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(SignupActivity.this, "Sign up success",
                                        Toast.LENGTH_SHORT).show();
                                startloginActivity();
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignupActivity.this, "Authentication failed",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
             }
    }

    View.OnClickListener signupbtntouch = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.btn_signup) {
                signup();
                Log.e("click", "click");
            }
        }
    };
    private void startloginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}