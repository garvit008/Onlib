package com.ips.lib.onlib;

import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ips.lib.onlib.Models.BookRefined;
import com.ips.lib.onlib.Models.HitsList;
import com.ips.lib.onlib.Models.HitsObject;
import com.ips.lib.onlib.SearchSuggestionProvider;
import com.ips.lib.onlib.utils.BooksAdapter;
import com.ips.lib.onlib.utils.ElasticSearchAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchActivity extends AppCompatActivity implements BooksAdapter.OnClickListerner {
    
    
    private SearchView searchView;
    private ImageView backArrow;
    private static final String TAG = "SearchActivity";
    private SearchRecentSuggestions suggestions;
    private String password;
    private ArrayList<BookRefined> books;
    private BooksAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private RelativeLayout relativeLayout;
    private static final String BASE_URL = "http://35.188.211.192//elasticsearch/refined_books/refined_books/";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        searchView = findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryRefinementEnabled(true);
        searchView.requestFocus(1);
        searchView.setQueryHint(getString(R.string.search_hint));
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.recyclerView);
        getElasticSearchPassword();
        //changing searchView's hint and text color
        EditText editText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        editText.setTextColor(getResources().getColor(R.color.text_white));
        editText.setHintTextColor(getResources().getColor(R.color.material_secondary_text_grey));

        backArrow = findViewById(R.id.back_arrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if(searchManager != null){
          searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        }
        setupSearchView();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            Log.d(TAG, "onNewIntent: started ");
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "onNewIntent: query = "+ query);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY,
                    SearchSuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.clear_suggestions:
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY,
                        SearchSuggestionProvider.MODE);
                suggestions.clearHistory();
                Toast.makeText(this, "Cleared search suggestions", Toast.LENGTH_SHORT).show();
                return true;
            default:
                break;
        }
        return true;
    }

    /**
     * method to setup searchView Widget
     */
    private void setupSearchView(){
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                progressBar.setVisibility(View.VISIBLE);
                getResults(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                CursorAdapter selectedView = searchView.getSuggestionsAdapter();
                Cursor cursor = (Cursor) selectedView.getItem(i);
                int index = cursor.getColumnIndexOrThrow(SearchManager.SUGGEST_COLUMN_TEXT_1);
                searchView.setQuery(cursor.getString(index), true);
                return true;
            }
        });
    }

    private void getElasticSearchPassword(){
        Query query = FirebaseDatabase.getInstance().getReference()
                      .child(getString(R.string.elasticsearch))
                      .orderByValue();


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot ds = dataSnapshot.getChildren().iterator().next();
                password = ds.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void getResults(String s){
        if(books == null){
            books = new ArrayList<>();
        }
        else {
            books.clear();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ElasticSearchAPI searchAPI = retrofit.create(ElasticSearchAPI.class);
        HashMap<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", Credentials.basic("user", password));
        Call<HitsObject> call = searchAPI.search(headerMap,"AND", s+"*");
        call.enqueue(new Callback<HitsObject>() {
            @Override
            public void onResponse(Call<HitsObject> call, Response<HitsObject> response) {

                HitsList hitsList = new HitsList();
                String jsonResponse = "";
                try{
                    Log.d(TAG, "onResponse: server response " + response.toString());
                    if(response.isSuccessful()){
                        hitsList = response.body().getHitsList();
                    }
                    else {
                        jsonResponse = response.errorBody().string();
                    }
                    Log.d(TAG, "onResponse: hits " + hitsList);
                    for(int i=0; i<hitsList.getBookSources().size(); i++){
                        books.add(hitsList.getBookSources().get(i).getBook());
                    }
                    Log.d(TAG, "onResponse: size = " + books.size());
                    //setup list of books
                    adapter = new BooksAdapter(SearchActivity.this, books, TAG, SearchActivity.this);
                    recyclerView.setAdapter(adapter);
                    progressBar.setVisibility(View.GONE);
                }
                catch (NullPointerException e){
                    Log.d(TAG, "onResponse: ERROR " + e.getMessage());
                }
                catch (IndexOutOfBoundsException e){
                    Log.d(TAG, "onResponse: ERROR " + e.getMessage());
                }
                catch (IOException e){
                    Log.d(TAG, "onResponse: ERROR " + e.getMessage());
                }

            }

            @Override
            public void onFailure(Call<HitsObject> call, Throwable t) {
                Log.e(TAG, "onFailure: "+t.getMessage());
                Toast.makeText(SearchActivity.this, "Search Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onClick(int position, ImageView view) {
        Log.d(TAG, "onClick: item clicked at " + position );
        Intent intent = new Intent(this, BookDetailsActivity.class);
        intent.putExtra(getString(R.string.refined_book_extra), books.get(position));
        intent.putExtra(getString(R.string.transition_name), ViewCompat.getTransitionName(view));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Apply activity transition
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    SearchActivity.this,
                    view,
                    ViewCompat.getTransitionName(view));
            startActivity(intent, options.toBundle());
        } else {
            // Swap without transition
            startActivity(intent);
        }
    }
}
