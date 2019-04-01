package com.ips.lib.onlib;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ips.lib.onlib.Models.Book;
import com.ips.lib.onlib.Models.BookRefined;
import com.ips.lib.onlib.Models.User;
import com.ips.lib.onlib.utils.BooksAdapter;
import com.ips.lib.onlib.utils.IssuedBooksAdapter;
import com.ips.lib.onlib.utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private CircleImageView profileImageView;
    private TextView name, computerCode;
    private User user;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth mAuth;
    private TextView issuedBooksTv;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private List<Book> books;
    private IssuedBooksAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileImageView = findViewById(R.id.profileImageView);
        name = findViewById(R.id.nameTv);
        computerCode = findViewById(R.id.computerCodeTv);
        initImageLoader();
        try {
            user = (User) getIntent().getParcelableExtra(getString(R.string.bundle_user));
            setUpWidgets();
        }
        catch (NullPointerException e){
            Log.d(TAG, "onCreate: user is null");
        }
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);

        // show title only when toolbar is collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = true;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle("User Profile");
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView = findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        issuedBooksTv = findViewById(R.id.issuedBooksTv);
    }

    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(ProfileActivity.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    private void setUpWidgets(){
        UniversalImageLoader.setImage(user.getProfile_pic(), profileImageView, null, "");
        name.setText(user.getName());
        computerCode.setText(user.getComputer_code());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.edit_profile:
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
                return true;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        getUserDetails();
    }

    private void getUserDetails(){
        Query query = myRef.child(getString(R.string.dbname_users))
                .child(mAuth.getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
//                Log.d(TAG, "onDataChange: user detials " + currentUser.toString());
                if(user != null){
                    setUpProfileWidgets();
                    getIssueBookDetails();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void setUpProfileWidgets(){
        UniversalImageLoader.setImage(user.getProfile_pic(), profileImageView, null, "");
        name.setText(user.getName());
        computerCode.setText(user.getComputer_code());
    }

    private void getIssueBookDetails(){
        String issuedBooks = user.getIssued_books();
        Log.d(TAG, "getIssueBookDetails: issuedBooks " + issuedBooks);
        books = new ArrayList<>();
        if(issuedBooks.equals("")){
            progressBar.setVisibility(View.GONE);
            issuedBooksTv.setText("No books issued currently");
        }
        else
        {
            final String[] issuedBooksID = issuedBooks.split("!");
            for(int i=0; i<issuedBooksID.length; i++){
                Query query = myRef.child(getString(R.string.dbname_books))
                        .orderByChild(getString(R.string.field_book_id))
                        .equalTo(issuedBooksID[i]);
                final int finalI = i;
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for(DataSnapshot ds: dataSnapshot.getChildren()){
                                Log.d(TAG, "onDataChange: book " + ds.toString());
                                books.add(ds.getValue(Book.class));
                            }

                            if(finalI ==issuedBooksID.length - 1){
                                issuedBooksTv.setText(getString(R.string.currently_issued_books));
                                adapter = new IssuedBooksAdapter(ProfileActivity.this, books);
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

}
