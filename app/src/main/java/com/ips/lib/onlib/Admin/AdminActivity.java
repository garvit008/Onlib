package com.ips.lib.onlib.Admin;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ips.lib.onlib.LoginActivity;
import com.ips.lib.onlib.MainActivity;
import com.ips.lib.onlib.Models.Book;
import com.ips.lib.onlib.Models.BookNotification;
import com.ips.lib.onlib.Models.User;
import com.ips.lib.onlib.R;
import com.ips.lib.onlib.ReturnBookActivity;
import com.ips.lib.onlib.utils.BookHelper;
import com.ips.lib.onlib.utils.DateHelper;
import com.ips.lib.onlib.utils.SharedPrefManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class AdminActivity extends AppCompatActivity {

    private static final String TAG = "AdminActivity";
    private Button logout, sendReminders;
    private SharedPrefManager sharedPrefManager;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private ProgressBar progressBar;
    private ArrayList<Book> books;
    private Date currentDate;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        new GetDateTask().execute();
        sharedPrefManager = new SharedPrefManager(AdminActivity.this);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                sharedPrefManager.clearUserType();
                Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        books = new ArrayList<>();
        sendReminders = findViewById(R.id.sendRemBtn);
        sendReminders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                sendReminders.setAlpha(0.5f);
                getIssuedBooks();
            }
        });
    }

    private void getIssuedBooks(){
        Query query = myRef.child(getString(R.string.dbname_books))
                           .orderByChild(getString(R.string.field_is_issued))
                           .equalTo("true");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: book " + ds.toString());
                    books.add(ds.getValue(Book.class));
                }
                progressBar.setVisibility(View.GONE);
                sendReminders.setAlpha(1f);
                createNotificationList(books);
                Log.d(TAG, "onDataChange: book size " + books.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private class GetDateTask extends AsyncTask<Void, Void, Long> {

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
            Log.d(TAG, "onPostExecute: date = " + result );
            currentDate = new Date(result);
            Log.d(TAG, "onPostExecute: current date " + currentDate);
        }

    }

    private void createNotificationList(final ArrayList<Book> books){

        for(int i=0; i<books.size(); i++){
            Date returnDate = DateHelper.getFormattedDate(books.get(i).getIssue_date());
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(returnDate);
            calendar.add(Calendar.DATE, 30);
            final int daysLeft = Integer.parseInt(DateHelper.getDaysDifference(currentDate, calendar.getTime()));
            Log.d(TAG, "createNotificationList: daysLeft = " + daysLeft);
            if(daysLeft<=5 && daysLeft>=0){
                Query query = myRef.child(getString(R.string.dbname_users))
                                   .orderByChild(getString(R.string.field_computer_code))
                                   .equalTo(books.get(i).getIssued_to());
                final int finalI = i;
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user;
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            user = ds.getValue(User.class);
                            BookNotification bookNotification = new BookNotification();
                            bookNotification.setDate(DateHelper.getDateString(calendar.getTime()));
                            if(daysLeft == 0){
                                bookNotification.setContent("You need to return the book " + books.get(finalI).getName()+
                                        " Today!");
                            }
                            else {
                                bookNotification.setContent("You need to return the book " + books.get(finalI).getName()+
                                        " within " + daysLeft + " days");
                            }

                            bookNotification.setType("reminder");
                            myRef.child(getString(R.string.dbname_notifications))
                                     .child(user.getUser_id())
                                     .setValue(bookNotification);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }
}
