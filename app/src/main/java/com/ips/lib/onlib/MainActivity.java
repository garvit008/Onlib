package com.ips.lib.onlib;

import android.Manifest;
import android.app.DownloadManager;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ips.lib.onlib.utils.SharedPrefManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private TextView searchTv;
    private SharedPrefManager sharedPrefManager;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private boolean userFound;
    private CircleImageView profileImageView;
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
        mAuth = FirebaseAuth.getInstance();
        FirebaseApp.initializeApp(this);
        updateUI(mAuth.getCurrentUser());
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        Intent intent = getIntent();
        sharedPrefManager = new SharedPrefManager(this);

        if(intent.hasExtra(getString(R.string.user_type))){
            String userType = intent.getStringExtra(getString(R.string.user_type));
            Log.d(TAG, "onCreate: userType = " + userType);
            sharedPrefManager.saveUserType(userType);
            if(userType.equals("Librarian")){
                checkUserTypeInDB(mAuth.getCurrentUser());
            }
        }

        setContentView(R.layout.activity_main);
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
        View headerview = navigationView.getHeaderView(0);
        profileImageView = headerview.findViewById(R.id.profileImageView);
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(in);
            }
        });
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

    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: called");
        super.onStart();
        FirebaseApp.initializeApp(this);
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

//        String userType = sharedPrefManager.getUserType();
//        if(userType.equals("Librarian")){
//            if(checkUserTypeInDB(currentUser)){
//                Intent in = new Intent(MainActivity.this, LibrarianMainActivity.class);
//                startActivity(in);
//                finish();
//            }
//            else {
//                Toast.makeText(this, "Invalid User. Check the selected user type", Toast.LENGTH_SHORT).show();
//                Intent in = new Intent(MainActivity.this, LoginActivity.class);
//                startActivity(in);
//                finish();
//            }
//
//        }
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

    /**
     * function to check if user exists in the Librarian DB
     * @param currentUser Firebase user
     * @return true if user exists, false otherwise
     */
    private void checkUserTypeInDB(FirebaseUser currentUser){
        if(currentUser!=null){
            Query query = myRef.child(getString(R.string.dbname_librarians))
                    .child(currentUser.getUid());

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                boolean localUserFound = false;
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: user found " + ds.toString());
                        localUserFound = true;
                        break;
                    }
                    if(localUserFound){
                        Log.d(TAG, "onCreate: user found in DB");
                        Intent in = new Intent(MainActivity.this, LibrarianMainActivity.class);
                        startActivity(in);
                        finish();
                    }
                    else {
                        Log.d(TAG, "onCreate: user not found in DB");
                        Toast.makeText(MainActivity.this, "Invalid User. Check the selected user type", Toast.LENGTH_SHORT).show();
                        Intent in = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(in);
                        finish();
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        Log.d(TAG, "checkUserTypeInDB: userFound = " + userFound);

    }
}
