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
import android.widget.ImageView;
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
import com.ips.lib.onlib.Models.BookRefined;
import com.ips.lib.onlib.Models.IssueHistoryBook;
import com.ips.lib.onlib.Models.User;
import com.ips.lib.onlib.utils.BooksAdapter;
import com.ips.lib.onlib.utils.IssueHistoryAdapter;
import com.ips.lib.onlib.utils.SharedPrefManager;
import com.ips.lib.onlib.utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyBooksActivity extends AppCompatActivity implements BooksAdapter.OnClickListerner {

    private static final String TAG = "MyBooksActivity";
    private DrawerLayout drawerLayout;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private SharedPrefManager sharedPrefManager;
    private CircleImageView profileImageView;
    private TextView name, computerCode;
    private User currentUser;
    private ProgressBar progressBar;
    private ArrayList<BookRefined> books;
    private RecyclerView recyclerView;
    private BooksAdapter adapter;
    private ArrayList<String>bookIDs;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_books);
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
                        intent = new Intent(MyBooksActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;

                    case R.id.issued:
                        intent = new Intent(MyBooksActivity.this, IssueHistoryActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.logout:
                        mAuth.signOut();
                        sharedPrefManager.clearUserType();
                        intent = new Intent(MyBooksActivity.this, LoginActivity.class);
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
                Intent in = new Intent(MyBooksActivity.this, ProfileActivity.class);
                in.putExtra(getString(R.string.bundle_user), currentUser);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Apply activity transition
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            MyBooksActivity.this,
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
        bookIDs = new ArrayList<>();
        books = new ArrayList<>();
        getWishlist();
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

    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(MyBooksActivity.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
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

    private void setUpProfileWidgets(){
        UniversalImageLoader.setImage(currentUser.getProfile_pic(), profileImageView, null, "");
        name.setText(currentUser.getName());
        computerCode.setText(currentUser.getComputer_code());
    }

    private void getWishlist(){
        Query query = myRef.child(getString(R.string.dbname_wishlist))
                           .child(mAuth.getCurrentUser().getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: " + ds.toString());
                    Map<String, Object> dsMap = (Map<String, Object>) ds.getValue();
                    bookIDs.add(dsMap.get(getString(R.string.refined_key)).toString());
                }
                Log.d(TAG, "onDataChange: history size: " + bookIDs.size());
                setupAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupAdapter(){
        for(int i=0; i<bookIDs.size(); i++){
            Log.d(TAG, "onDataChange: inside for: " + bookIDs.get(i));
            Query query = myRef.child(getString(R.string.dbname_refined_books))
                               .child(bookIDs.get(i));
            final int finalI = i;
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onDataChange: " + dataSnapshot.toString());
                    books.add(dataSnapshot.getValue(BookRefined.class));
                    if(finalI == bookIDs.size()-1){
                        Log.d(TAG, "setupAdapter: books size " + books.size());
                        progressBar.setVisibility(View.GONE);
                        adapter = new BooksAdapter(MyBooksActivity.this, books, TAG, MyBooksActivity.this);
                        recyclerView.setAdapter(adapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }

    }

    @Override
    public void onClick(int position, ImageView view) {
        Log.d(TAG, "onClick: item clicked at " + position + " position");
        Intent intent = new Intent(this, BookDetailsActivity.class);
        intent.putExtra(getString(R.string.refined_book_extra), books.get(position));
        intent.putExtra(getString(R.string.transition_name), ViewCompat.getTransitionName(view));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Apply activity transition
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    MyBooksActivity.this,
                    view,
                    ViewCompat.getTransitionName(view));
            startActivity(intent, options.toBundle());
        } else {
            // Swap without transition
            startActivity(intent);
        }
    }
}
