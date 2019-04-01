package com.ips.lib.onlib;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.ips.lib.onlib.Models.BookNotification;
import com.ips.lib.onlib.Models.BookRefined;
import com.ips.lib.onlib.Models.User;
import com.ips.lib.onlib.utils.BookHelper;
import com.ips.lib.onlib.utils.DateHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Formatter;

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
    private String issueDate;
    private Date issueDateNotif;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue);
        new MyTask().execute();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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
                closeKeyBoard();
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
                                    if(book.getIs_issued().equals("false")) {
                                        Log.d(TAG, "user: " + user.toString() + " book: " + book.toString());
                                        issueBook();
                                    }
                                    else
                                    {
                                        Toast.makeText(IssueBookActivity.this, "Book is already issued", Toast.LENGTH_SHORT).show();
                                    }
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
            if(user.getIssued_books()!= null){
                user.setIssued_books(user.getIssued_books() + book.getBook_id() + "!");
            }
            else
            {
                user.setIssued_books(book.getBook_id() + "!");
            }

            user.setBooks_issued_count(user.getBooks_issued_count() + 1);
            myRef.child(getString(R.string.dbname_users))
                    .child(user.getUser_id())
                    .setValue(user);

            book.setIssue_date(issueDate);
            Map<String, String> timeStamp = ServerValue.TIMESTAMP;
            Log.d(TAG, "issueBook: timestamp = " + timeStamp);
            book.setIssued_to(user.getComputer_code());
            book.setIs_issued("true");
            myRef.child(getString(R.string.dbname_books))
                    .child(book.getBook_id())
                    .setValue(book);

            final String refined_key = BookHelper.getRefinedKey(book);
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
                        progressBar.setVisibility(View.GONE);
                        BookNotification bookNotification = new BookNotification();
                        bookNotification.setContent("Book issued successfully \n title: " + book.getName());
                        bookNotification.setDate(DateHelper.getDateString(issueDateNotif));
                        myRef.child(getString(R.string.dbname_notifications))
                                .child(user.getUser_id())
                                .setValue(bookNotification);
                        Toast.makeText(IssueBookActivity.this, "Book issued Successfully", Toast.LENGTH_SHORT).show();
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


    private class MyTask extends AsyncTask<Void, Void, Long> {

        @Override
        protected Long doInBackground(Void... params) {
            String url = "https://time.is/Unix_time_now";
            Document doc;
            Elements elements = null;
            try {

                doc = Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
                String[] tags = new String[] {
                        "div[id=time_section]",
                        "div[id=clock0_bg]"
                };
                elements= doc.select(tags[0]);
                for (int i = 0; i <tags.length; i++) {
                    elements = elements.select(tags[i]);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return Long.parseLong(elements.text() + "000");
        }


        @Override
        protected void onPostExecute(Long result) {
            String difference = "";
            Log.d(TAG, "onPostExecute: date = " + result );
            Date date = new Date(result);
            issueDateNotif = date;
            Log.d(TAG, "onPostExecute: date " + date);
            issueDate = DateHelper.getDateString(date);
            Log.d(TAG, "onPostExecute: issueDate = " + issueDate);
            Date backDate = null;
            String timeStamp = "2019-03-09T19:25:09+0000";
            backDate = DateHelper.getFormattedDate(timeStamp);
            Log.d(TAG, "getTimeStampDifference: date today "+ date);
            Log.d(TAG, "getTimeStampDifference: date recieved " + backDate);
            difference = DateHelper.getDaysDifference(backDate, date);
            Log.d(TAG, "onPostExecute: difference = " + difference);
            }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                break;
        }
        return true;
    }

    private void closeKeyBoard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

}
