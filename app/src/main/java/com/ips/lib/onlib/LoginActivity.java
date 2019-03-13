package com.ips.lib.onlib;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText computerCode;
    private EditText password;
    private Button login;
    private ProgressBar pbar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        computerCode = findViewById(R.id.computerCode);
        password = findViewById(R.id.password);
        login = findViewById(R.id.loginBtn);
        pbar = findViewById(R.id.loginPbar);
        mAuth = FirebaseAuth.getInstance();
        pbar.setVisibility(View.GONE);
        computerCode.setAlpha(1f);
        password.setAlpha(1f);
        login.setAlpha(1f);
        initSignIn();
    }

    private void initSignIn() {
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String compCode = computerCode.getText().toString();
                String pass = password.getText().toString();
                if (compCode.equals("") || pass.equals("")) {
                    Toast.makeText(LoginActivity.this, "credentials cannot be empty", Toast.LENGTH_SHORT).show();
                } else {
                    compCode += "@ipsa.com";
                    signIn(compCode, pass);
                    pbar.setVisibility(View.VISIBLE);
                    computerCode.setAlpha(0.5f);
                    password.setAlpha(0.5f);
                    login.setAlpha(0.5f);
                }
            }
        });
    }


    private void signIn(String computerCode, String password) {
        Log.d(TAG, "signIn: credentials: " + computerCode + ", " + password);
        mAuth.signInWithEmailAndPassword(computerCode, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: signin successfull");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();

                            updateUI(null);
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: signIn failed: " + e.getMessage());
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Log.d(TAG, "updateUI: user logging in");
            pbar.setVisibility(View.GONE);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.d(TAG, "updateUI: user is null");
            pbar.setVisibility(View.GONE);
        }
    }


}
