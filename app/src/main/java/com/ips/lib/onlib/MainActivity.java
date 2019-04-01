package com.ips.lib.onlib;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.ips.lib.onlib.Models.User;
import com.ips.lib.onlib.utils.SharedPrefManager;
import com.ips.lib.onlib.utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private TextView searchTv;
    private SharedPrefManager sharedPrefManager;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private CircleImageView profileImageView;
    private TextView name, computerCode;
    private User currentUser;
    private CardView cs, me, ec, ce, ex, ft, ch, ge;
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
        initImageLoader();
        drawerLayout = findViewById(R.id.drawerLayout);
        getUserDetails();
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
        name = headerview.findViewById(R.id.nameTv);
        computerCode = headerview.findViewById(R.id.headerComputerCodeTv);
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if we're running on Android 5.0 or higher
                Intent in = new Intent(MainActivity.this, ProfileActivity.class);
                in.putExtra(getString(R.string.bundle_user), currentUser);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // Apply activity transition
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            MainActivity.this,
                            profileImageView,
                            ViewCompat.getTransitionName(profileImageView));
                    startActivity(in, options.toBundle());
                } else {
                    // Swap without transition
                    startActivity(in);
                }


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

        cs = findViewById(R.id.cs);
        cs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CatalogueActivity.class);
                intent.putExtra(getString(R.string.intent_query), "CS");
                startActivity(intent);

            }
        });
        me = findViewById(R.id.me);
        me.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CatalogueActivity.class);
                intent.putExtra(getString(R.string.intent_query), "ME");
                startActivity(intent);
            }
        });

        ec = findViewById(R.id.ec);
        ec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CatalogueActivity.class);
                intent.putExtra(getString(R.string.intent_query), "EC");
                startActivity(intent);
            }
        });

        ce = findViewById(R.id.ce);
        ce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CatalogueActivity.class);
                intent.putExtra(getString(R.string.intent_query), "CE");
                startActivity(intent);
            }
        });

        ex = findViewById(R.id.ex);
        ex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CatalogueActivity.class);
                intent.putExtra(getString(R.string.intent_query), "EX");
                startActivity(intent);
            }
        });

        ft = findViewById(R.id.ft);
        ft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CatalogueActivity.class);
                intent.putExtra(getString(R.string.intent_query), "FT");
                startActivity(intent);
            }
        });
        ch = findViewById(R.id.ch);
        ch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CatalogueActivity.class);
                intent.putExtra(getString(R.string.intent_query), "CH");
                startActivity(intent);
            }
        });
        ge = findViewById(R.id.ge);
        ge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CatalogueActivity.class);
                intent.putExtra(getString(R.string.intent_query), "GE");
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
        else
        {
            getUserDetails();
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
            //initFCM();
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
                boolean userFound = false;
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: user found " + ds.toString());
                        userFound = true;
                        break;
                    }
                    if(userFound){
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

    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(MainActivity.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    private void setUpProfileWidgets(){
        UniversalImageLoader.setImage(currentUser.getProfile_pic(), profileImageView, null, "");
        name.setText(currentUser.getName());
        computerCode.setText(currentUser.getComputer_code());
    }


}
