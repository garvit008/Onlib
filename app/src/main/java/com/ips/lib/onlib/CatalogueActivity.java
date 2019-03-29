package com.ips.lib.onlib;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ips.lib.onlib.Models.BookRefined;
import com.ips.lib.onlib.utils.BooksAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CatalogueActivity extends AppCompatActivity {

    private static final String TAG = "CatalogueActivity";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private List<BookRefined> books;
    private BooksAdapter adapter;
    private String query;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private SwipeRefreshLayout swipeRefreshLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogue);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        swipeRefreshLayout = findViewById(R.id.pullToRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getResults();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.VISIBLE);
        recyclerView = findViewById(R.id.recyclerView);
        Intent intent = getIntent();
        query = intent.getStringExtra(getString(R.string.intent_query));
        Log.d(TAG, "onCreate: query = " + query);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        getResults();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

    }

    private void getResults(){
        Query query = myRef.child(getString(R.string.dbname_refined_books));

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<BookRefined> refinedBooks = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Map<String, Object> dsMap = (HashMap<String, Object>) ds.getValue();
                    BookRefined book = new BookRefined();
                    Log.d(TAG, "onDataChange: map = " + dsMap.toString());
                    book.setCover(dsMap.get(getString(R.string.field_cover)).toString());
                    book.setAuthor(dsMap.get(getString(R.string.field_author)).toString());
                    book.setName(dsMap.get(getString(R.string.field_name)).toString());
                    book.setEdition(dsMap.get(getString(R.string.field_edition)).toString());
                    book.setAvailable(dsMap.get(getString(R.string.field_available)).toString());
                    refinedBooks.add(book);
                }
                books = refinedBooks;
                Log.d(TAG, "onDataChange: size " + books.size());
                adapter = new BooksAdapter(CatalogueActivity.this, books);
                recyclerView.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
}
