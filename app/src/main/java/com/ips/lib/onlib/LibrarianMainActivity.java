package com.ips.lib.onlib;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ips.lib.onlib.utils.SharedPrefManager;

public class LibrarianMainActivity extends AppCompatActivity {

    private static final String TAG = "LibrarianMainActivity";
    private Button logout;
    private FirebaseAuth mAuth;
    private SharedPrefManager sharedPrefManager;
    private CardView addBook, issueBook, returBook;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_librarian_main);
        addBook = findViewById(R.id.addBookCv);
        addBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LibrarianMainActivity.this, AddBookActivity.class);
                startActivity(intent);
            }
        });
        issueBook = findViewById(R.id.issueBookCv);
        issueBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LibrarianMainActivity.this, IssueBookActivity.class);
                startActivity(intent);
            }
        });
        returBook = findViewById(R.id.returnBookCv);
        returBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LibrarianMainActivity.this, ReturnBookActivity.class);
                startActivity(intent);
            }
        });
        mAuth = FirebaseAuth.getInstance();
        updateUI(mAuth.getCurrentUser());
        sharedPrefManager = new SharedPrefManager(this);
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                sharedPrefManager.clearUserType();
                Intent intent = new Intent(LibrarianMainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(FirebaseAuth.getInstance().getCurrentUser() == null){
            updateUI(null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user){
        if(user!=null){
            Log.d(TAG, "updateUI: user logged in");
        }
        else{
            Intent intent = new Intent(LibrarianMainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

}
