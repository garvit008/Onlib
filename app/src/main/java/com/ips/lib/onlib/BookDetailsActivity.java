package com.ips.lib.onlib;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ips.lib.onlib.Models.BookRefined;
import com.ips.lib.onlib.utils.BookHelper;
import com.ips.lib.onlib.utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BookDetailsActivity extends AppCompatActivity {

    private static final String TAG = "BookDetailsActivity";
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private ImageView cover;
    private TextView name, author, edition, availability, shelf;
    private Button addTowishlist;
    private BookRefined book;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        collapsingToolbarLayout = findViewById(R.id.collapsingToolbar);
        Toolbar toolbar = findViewById(R.id.toolbar);
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
                    collapsingToolbarLayout.setTitle(getString(R.string.book_details));
                    isShow = true;
                } else if(isShow) {
                    collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        initImageLoader();
        cover = findViewById(R.id.bookCoverIv);
        name = findViewById(R.id.bookName);
        author = findViewById(R.id.bookAuthor);
        edition = findViewById(R.id.bookEdition);
        availability = findViewById(R.id.bookAvailability);
        shelf = findViewById(R.id.bookShelf);
        addTowishlist = findViewById(R.id.wishlistBtn);
        addTowishlist.setEnabled(true);
        addTowishlist.setAlpha(1f);
        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.refined_book_extra))){
            try {
                book = intent.getParcelableExtra(getString(R.string.refined_book_extra));
                setupWidgets();
            }
            catch (NullPointerException e){
                Log.e(TAG, "onCreate: book is null" + e.getMessage());
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imageTransitionName = intent.getStringExtra(getString(R.string.transition_name));
            cover.setTransitionName(imageTransitionName);
        }

        toggleButtonEnable();
        addTowishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBookToWishList();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            default:
                break;
        }
        return true;
    }

    private void setupWidgets(){
        UniversalImageLoader.setImage(book.getCover(), cover, null, "");
        name.setText(book.getName());
        author.setText("By " + book.getAuthor());
        edition.setText("Edition: " + book.getEdition());
        availability.setText("Availability: " + book.getAvailable());
    }

    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(BookDetailsActivity.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    private void toggleButtonEnable(){
        Query query = myRef.child(getString(R.string.dbname_wishlist))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(BookHelper.getRefinedKey(book));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.d(TAG, "onDataChange: book exists in wishlist");
                    addTowishlist.setEnabled(false);
                    addTowishlist.setAlpha(0.5f);
                }
                else {
                    Log.d(TAG, "onDataChange: book doesn't exists in wishlist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addBookToWishList(){
        myRef.child(getString(R.string.dbname_wishlist))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(BookHelper.getRefinedKey(book))
                .child(getString(R.string.refined_key))
                .setValue(BookHelper.getRefinedKey(book));
        Toast.makeText(this, "Book added to wishlist", Toast.LENGTH_SHORT).show();
        toggleButtonEnable();
    }
}
