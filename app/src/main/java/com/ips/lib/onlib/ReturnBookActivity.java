package com.ips.lib.onlib;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ips.lib.onlib.Models.Book;
import com.ips.lib.onlib.Models.BookNotification;
import com.ips.lib.onlib.Models.BookRefined;
import com.ips.lib.onlib.Models.Librarian;
import com.ips.lib.onlib.Models.User;
import com.ips.lib.onlib.utils.BookHelper;
import com.ips.lib.onlib.utils.DateHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

public class ReturnBookActivity extends AppCompatActivity {

    private EditText bookID;
    private Button returnBookBtn;
    private ProgressBar progressBar;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private static final String TAG = "ReturnBookActivity";
    private Book book;
    private User user;
    private BookRefined bookRefined;
    private Librarian librarian;
    private Date returnDate;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return);
        new DateTask().execute();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        bookID = findViewById(R.id.bookIdET);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        returnBookBtn = findViewById(R.id.returnBtn);
        returnBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bookID.getText().toString().equals("")){
                    Toast.makeText(ReturnBookActivity.this, "Book ID cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    closeKeyBoard();
                    progressBar.setVisibility(View.VISIBLE);
                    validate();
                }
            }
        });

    }

    private void validate(){
        String book_id = bookID.getText().toString();
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
                    if (book != null) {
                        if(book.getIs_issued().equals("true")) {
                            Log.d(TAG, " book: " + book.toString());
                            returnBook();
                        }
                        else
                        {
                            Toast.makeText(ReturnBookActivity.this, "Book is not issued", Toast.LENGTH_SHORT).show();
                        }
                    }


                } else {
                    Toast.makeText(ReturnBookActivity.this, "Invalid Book ID", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void returnBook(){
        int days = 30;
        int lateFees = 0;
        final String refined_key = BookHelper.getRefinedKey(book);
        Log.d(TAG, "returnBook: refined key " + refined_key);
        String issueDateStr = book.getIssue_date();
        Date issueDate = DateHelper.getFormattedDate(issueDateStr);
        int dayDifference = Integer.parseInt(DateHelper.getDaysDifference(issueDate, returnDate));
        Log.d(TAG, "returnBook: days = " + dayDifference);
        int dayOffset = dayDifference - days;
        if(dayOffset > 0){
            lateFees = dayOffset * 5;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(ReturnBookActivity.this);
        builder.setTitle(getString(R.string.label_confirm_latefee));

        builder.setMessage("Late fee: Rs." + lateFees + "\nClick comfirm if payment recieved, otherwise cancel");
        String positiveText = getString(R.string.label_confirm);
        final int finalLateFees = lateFees;
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("myTag", "positive button clicked");
                        Query query = myRef.child(getString(R.string.dbname_users))
                                .orderByChild(getString(R.string.field_computer_code))
                                .equalTo(book.getIssued_to());

                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Log.d(TAG, "onDataChange: user exists");
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        user = ds.getValue(User.class);
                                    }
                                    if(user != null){
                                        String target = book.getBook_id()+"!";
                                        String replacement = user.getIssued_books().replace(target, "");
                                        Log.d(TAG, "onDataChange: replaced " + replacement);
                                        user.setIssued_books(replacement);
                                        if(user.getBooks_issued_count()>0){
                                            user.setBooks_issued_count(user.getBooks_issued_count() - 1);
                                        }
                                        myRef.child(getString(R.string.dbname_users))
                                                .child(user.getUser_id())
                                                .setValue(user);
                                    }
                                }
                                else
                                {
                                    Log.d(TAG, "onDataChange: user is null");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        
                        book.setIssue_date("");
                        book.setIs_issued("false");
                        book.setIssued_to("");
                        myRef.child(getString(R.string.dbname_books))
                                       .child(book.getBook_id())
                                       .setValue(book);
                        Query query1 = myRef.child(getString(R.string.dbname_refined_books))
                                       .child(refined_key);
                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    bookRefined = dataSnapshot.getValue(BookRefined.class);
                                    Log.d(TAG, "onDataChange: refined book " + bookRefined.toString());
                                    int bookAvailable = Integer.parseInt(bookRefined.getAvailable()) + 1;
                                    bookRefined.setAvailable(String.valueOf(bookAvailable));
                                    myRef.child(getString(R.string.dbname_refined_books))
                                            .child(refined_key)
                                            .setValue(bookRefined);
                                }
                                else
                                {
                                    Log.d(TAG, "onDataChange: refined book not found");
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        Query query2 = myRef.child(getString(R.string.dbname_librarians))
                                            .child(mAuth.getCurrentUser().getUid());
                        query2.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    librarian = dataSnapshot.getValue(Librarian.class);
                                    Log.d(TAG, "onDataChange: librarian " + librarian.toString());
                                    int feesCollected = Integer.parseInt(librarian.getFees_collected()) + finalLateFees;
                                    librarian.setFees_collected(String.valueOf(feesCollected));
                                    myRef.child(getString(R.string.dbname_librarians))
                                            .child(mAuth.getCurrentUser().getUid())
                                            .setValue(librarian);
                                    BookNotification bookNotification = new BookNotification();
                                    bookNotification.setContent("Book returned successfully title: " + book.getName());
                                    bookNotification.setDate(DateHelper.getDateString(returnDate));
                                    bookNotification.setType("notification");
                                    myRef.child(getString(R.string.dbname_notifications))
                                            .child(user.getUser_id())
                                            .setValue(bookNotification);
                                    Toast.makeText(ReturnBookActivity.this, "Book returned successfully", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Log.d(TAG, "onDataChange: Librarian doesn't exist");
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }
                });
        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // dismiss dialog, start counter again
                        dialog.dismiss();
                        Log.d("myTag", "negative button clicked");
                    }
                });

        AlertDialog dialog = builder.create();
// display dialog
        dialog.show();

    }

    private class DateTask extends AsyncTask<Void, Void, Long> {

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
            returnDate = new Date(result);
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
