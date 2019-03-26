package com.ips.lib.onlib;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.ips.lib.onlib.Models.Book;
import com.ips.lib.onlib.Models.BookRefined;
import com.ips.lib.onlib.Models.User;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class IssueBookActivity extends AppCompatActivity {


    private static final String TAG = "IssueBookActivity";
    private EditText compCode, bookID;
    private Button issueBookBtn;
    private ProgressBar progressBar;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private User user;
    private Book book;
    private BookRefined bookRefined;
    private static final long MAX_BOOK_COUNT = 3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);
        compCode = findViewById(R.id.computerCodeET);
        bookID = findViewById(R.id.bookIdET);
        issueBookBtn = findViewById(R.id.issueBtn);
        progressBar = findViewById(R.id.issuePbar);
        progressBar.setVisibility(View.GONE);
        database = FirebaseDatabase.getInstance();
        Map<String, String> timeStamp = ServerValue.TIMESTAMP;
        Log.d(TAG, "issueBook: timestamp = " );
        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        myRef = database.getReference();
        issueBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (compCode.getText().toString().equals("") || bookID.getText().toString().equals("")) {
                    Toast.makeText(IssueBookActivity.this, "Computer Code or Book ID can't be empty", Toast.LENGTH_SHORT).show();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    validate();
                }

            }
        });

    }

    private void validate() {

        String comp_code = compCode.getText().toString();
        final String book_id = bookID.getText().toString();
        Query query = myRef.child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_computer_code))
                .equalTo(comp_code);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: user exists");
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        user = ds.getValue(User.class);
                    }

                    Query query1 = myRef.child(getString(R.string.dbname_books))
                            .orderByChild(getString(R.string.field_book_id))
                            .equalTo(book_id);

                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Log.d(TAG, "onDataChange: book exists");
                                for (DataSnapshot ds1 : dataSnapshot.getChildren()) {
                                    book = ds1.getValue(Book.class);
                                }
                                if (user != null && book != null) {
                                    Log.d(TAG, "user: " + user.toString() + " book: " + book.toString());
                                    issueBook();
                                }

                            } else {
                                Toast.makeText(IssueBookActivity.this, "Invalid Book ID", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {
                    Toast.makeText(IssueBookActivity.this, "Invalid computer code", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void issueBook() {
        if (user.getBooks_issued_count() < MAX_BOOK_COUNT) {
            user.setIssued_books(user.getIssued_books() + book.getBook_id() + "!");
            user.setBooks_issued_count(user.getBooks_issued_count() + 1);
            //add issue date
            Map<String, String> timeStamp = ServerValue.TIMESTAMP;
            Log.d(TAG, "issueBook: timestamp = " + timeStamp);
            book.setIssued_to(user.getComputer_code());
            book.setIs_issued("true");
            final String refined_key = book.getName() + book.getAuthor() + book.getEdition();
            Query query1 = myRef.child(getString(R.string.dbname_refined_books))
                    .child(refined_key);


            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        bookRefined = dataSnapshot.getValue(BookRefined.class);
                        int a =   Integer.parseInt(bookRefined.getAvailable()) - 1;
                        bookRefined.setAvailable(String.valueOf(a));
                        myRef.child(getString(R.string.dbname_refined_books))
                                .child(refined_key).setValue(bookRefined);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
         }

         else
         {
             Toast.makeText(this, "No slots available!", Toast.LENGTH_SHORT).show();
         }
    }
}
