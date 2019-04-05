package com.ips.lib.onlib;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ips.lib.onlib.Models.Book;
import com.ips.lib.onlib.Models.IssueHistoryBook;
import com.ips.lib.onlib.Models.User;
import com.ips.lib.onlib.utils.IssueHistoryAdapter;
import com.ips.lib.onlib.utils.IssuedBooksAdapter;
import com.ips.lib.onlib.utils.SharedPrefManager;
import com.ips.lib.onlib.utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class IssueHistoryActivity extends AppCompatActivity {

    private static final String TAG = "IssueHistoryActivity";
    private DrawerLayout drawerLayout;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private SharedPrefManager sharedPrefManager;
    private CircleImageView profileImageView;
    private TextView name, computerCode;
    private User currentUser;
    private ProgressBar progressBar;
    private ArrayList<Book> books;
    private RecyclerView recyclerView;
    private ArrayList<IssueHistoryBook> issueHistoryBooks;
    private IssueHistoryAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_history);
        drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.issue_history_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer_menu);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        sharedPrefManager = new SharedPrefManager(this);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                Intent intent;
                switch(menuItem.getItemId()){

                    case R.id.home:
                        intent = new Intent(IssueHistoryActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.myBooks:
                        intent = new Intent(IssueHistoryActivity.this, MyBooksActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.logout:
                        mAuth.signOut();
                        sharedPrefManager.clearUserType();
                        intent = new Intent(IssueHistoryActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }
                drawerLayout.closeDrawers();
                return false;

            }
        });
        initImageLoader();
        progressBar = findViewById(R.id.progressbar);
        recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);
        View headerview = navigationView.getHeaderView(0);
        profileImageView = headerview.findViewById(R.id.profileImageView);
        name = headerview.findViewById(R.id.nameTv);
        computerCode = headerview.findViewById(R.id.headerComputerCodeTv);
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if we're running on Android 5.0 or higher
                Intent in = new Intent(IssueHistoryActivity.this, ProfileActivity.class);
                in.putExtra(getString(R.string.bundle_user), currentUser);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Apply activity transition
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            IssueHistoryActivity.this,
                            profileImageView,
                            ViewCompat.getTransitionName(profileImageView));
                    startActivity(in, options.toBundle());
                } else {
                    // Swap without transition
                    startActivity(in);
                }


            }
        });
        getUserDetails();
        issueHistoryBooks = new ArrayList<>();
        books = new ArrayList<>();
        getHistoryDetails();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserDetails();
    }

    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(IssueHistoryActivity.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    private void setUpProfileWidgets(){
        UniversalImageLoader.setImage(currentUser.getProfile_pic(), profileImageView, null, "");
        name.setText(currentUser.getName());
        computerCode.setText(currentUser.getComputer_code());
    }

    private void getUserDetails(){
        Query query = myRef.child(getString(R.string.dbname_users))
                .child(mAuth.getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
//                Log.d(TAG, "onDataChange: user detials " + currentUser.toString());
                if(currentUser != null)
                    setUpProfileWidgets();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void getHistoryDetails(){
        Query query = myRef.child(getString(R.string.dbname_issue_history))
                .child(mAuth.getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    IssueHistoryBook historyBook = new IssueHistoryBook();
                    Map<String, Object> dsMap = (Map<String, Object>) ds.getValue();
                    Log.d(TAG, "onDataChange: map = " + dsMap.toString());
                    historyBook.setBook_id(dsMap.get(getString(R.string.field_book_id)).toString());
                    historyBook.setIssue_date(dsMap.get(getString(R.string.field_issue_date)).toString());
                    issueHistoryBooks.add(historyBook);
                }
                Log.d(TAG, "onDataChange: history size: " + issueHistoryBooks.size());
                setupAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setupAdapter(){
        for(int i=0; i<issueHistoryBooks.size(); i++){
            Log.d(TAG, "onDataChange: inside for: " + issueHistoryBooks.get(i).getBook_id());
            Query query1 =myRef.child(getString(R.string.dbname_books))
                    .orderByChild(getString(R.string.field_book_id))
                    .equalTo(issueHistoryBooks.get(i).getBook_id());
            final int finalI = i;
            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        for(DataSnapshot ds1 : dataSnapshot.getChildren()){
                            Log.d(TAG, "onDataChange: book = " + ds1.toString());
                            Book book;
                            book = ds1.getValue(Book.class);
                            book.setIssue_date(issueHistoryBooks.get(finalI).getIssue_date());
                            books.add(book);
                        }
                        if(finalI == issueHistoryBooks.size() - 1){
                            Log.d(TAG, "onDataChange: books size "+ books.size());
                            adapter = new IssueHistoryAdapter(books, IssueHistoryActivity.this);
                            recyclerView.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
