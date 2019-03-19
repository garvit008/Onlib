package com.ips.lib.onlib;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ips.lib.onlib.Models.User;

public class RegisterUserActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "RegisterUserActivity";
    private Spinner userTypeChoice;
    private String userType;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference myRef;
    private EditText compCode, name, email, password;
    private Button register;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        compCode = findViewById(R.id.computerCodeET);
        name = findViewById(R.id.nameET);
        email = findViewById(R.id.emailET);
        password = findViewById(R.id.passwordET);
        register = findViewById(R.id.registerBtn);
        progressBar = findViewById(R.id.registerPbar);
        progressBar.setVisibility(View.GONE);
        userTypeChoice = findViewById(R.id.userTypeSpinner);
        firebaseDatabase = FirebaseDatabase.getInstance();
        myRef = firebaseDatabase.getReference();
        mAuth = FirebaseAuth.getInstance();
        setUpSpinner();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                registerUser();
            }
        });
    }

    private void setUpSpinner(){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_type, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        userTypeChoice.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i){

            case 0:
                userType = "User";
                break;

            case 1:
                userType = "Librarian";
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        userType = "User";
    }

    private void registerUser(){
        final String computerCode = compCode.getText().toString();
        final String username = name.getText().toString();
        final String emailStr = email.getText().toString();
        String pass = password.getText().toString();
        final String userID;
        if(computerCode.equals("") || username.equals("") ||
            emailStr.equals("") || pass.equals("")){
            Toast.makeText(this, "Credentials cannot be null", Toast.LENGTH_SHORT).show();
        }
        else{
            mAuth.createUserWithEmailAndPassword(computerCode+"@ipsa.com", pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                progressBar.setVisibility(View.GONE);
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(RegisterUserActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                addUserToDatabase(user.getUid(), computerCode, username, emailStr);
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterUserActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                updateUI(null);
                            }

                        }
                    });
        }
    }

    private void updateUI(FirebaseUser user){
        if(user!=null){

            Intent intent = new Intent(RegisterUserActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            Toast.makeText(this, "Registration failed! ", Toast.LENGTH_SHORT).show();
        }
    }

    private void addUserToDatabase(String userID, String compCode, String name, String email){
        User user = new User();
        user.setComputer_code(compCode);
        user.setUser_id(userID);
        user.setEmail(email);
        user.setName(name);
        user.setIssued_books(null);
        user.setWishlist(null);
        user.setProfile_pic("");
        myRef.child(getString(R.string.dbname_users))
                .child(userID).setValue(user);
    }

}
