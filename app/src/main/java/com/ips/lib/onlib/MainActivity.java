package com.ips.lib.onlib;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ips.lib.onlib.utils.SharedPrefManager;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private TextView searchTv;
    private SharedPrefManager sharedPrefManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {

            finish();
            return;
        }
        Intent intent = getIntent();
        sharedPrefManager = new SharedPrefManager(this);
        if(intent.hasExtra(getString(R.string.user_type))){
            String userType = intent.getStringExtra(getString(R.string.user_type));
            sharedPrefManager.saveUserType(userType);
            if(userType.equals("Librarian")){
                Intent in = new Intent(MainActivity.this, LibrarianMainActivity.class);
                startActivity(in);
                finish();
            }
        }
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        drawerLayout = findViewById(R.id.drawerLayout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                menuItem.setChecked(true);

                drawerLayout.closeDrawers();
                if(menuItem.getItemId() == R.id.logout){
                    mAuth.signOut();
                    sharedPrefManager.clearUserType();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

                return true;
            }
        });
        mAuth = FirebaseAuth.getInstance();
        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_drawer_menu);
        searchTv = findViewById(R.id.searchTv);
        searchTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
       // mAuth.signOut();

    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: called");
        super.onStart();
        String userType = sharedPrefManager.getUserType();
        if(userType.equals("Librarian")){
            Intent in = new Intent(MainActivity.this, LibrarianMainActivity.class);
            startActivity(in);
            finish();
        }
        FirebaseApp.initializeApp(this);
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String userType = sharedPrefManager.getUserType();
        Log.d(TAG, "onResume: called with userType " + userType);
        if(userType.equals("Librarian")){
            Intent in = new Intent(MainActivity.this, LibrarianMainActivity.class);
            startActivity(in);
            finish();
        }
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

    private void updateUI(FirebaseUser user){
        if(user!=null){
            Log.d(TAG, "updateUI: user logged in");
        }
        else{
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
