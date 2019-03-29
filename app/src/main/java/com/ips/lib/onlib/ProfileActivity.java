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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
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
import com.ips.lib.onlib.Models.User;
import com.ips.lib.onlib.utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

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
                if(user != null)
                    setUpProfileWidgets();
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

}
