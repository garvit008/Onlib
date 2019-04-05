package com.ips.lib.onlib;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ips.lib.onlib.utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";
    private CircleImageView profileImageView;
    private CircleImageView cameraIcon;
    private EditText name;
    private EditText email;
    private Button updateProfile;
    private static final int RequestCode = 100;
    private ProgressBar progressBar;
    private RelativeLayout relativeLayout;
    private String filePath;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;
    HashMap<String, Object> values;
    private String actiityName;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar toolbar = findViewById(R.id.edit_profile_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        relativeLayout = findViewById(R.id.mainRelativeLayout);
        relativeLayout.setAlpha(1f);
        profileImageView = findViewById(R.id.profileImageView);
        cameraIcon = findViewById(R.id.iv_camera);
        name = findViewById(R.id.nameEt);
        email = findViewById(R.id.emailEditText);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        updateProfile = findViewById(R.id.updateProfileBtn);
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                relativeLayout.setAlpha(0.5f);
                uploadProfilePhoto();
            }
        });
        initImageLoader();
        cameraIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Pix.start(EditProfileActivity.this,
                        RequestCode);
            }
        });
       values = new HashMap<>();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
            default:
                break;
        }
        return true;
    }

    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(EditProfileActivity.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == RequestCode) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            Log.d(TAG, "onActivityResult: " + Uri.parse(returnValue.get(0)));
            profileImageView.setImageURI(Uri.parse(returnValue.get(0)));
            filePath = returnValue.get(0);
            toggleButtonEnable();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(EditProfileActivity.this,
                            RequestCode);
                } else {
                    Toast.makeText(this, "Approve permissions to open image Picker", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void uploadProfilePhoto(){
        if(filePath != null){
            Uri file = Uri.fromFile(new File(filePath));
            final StorageReference imgReference = storageRef.child("photos/" + "profilePhotos/" + mAuth.getCurrentUser().getUid() + "/profilePic");

            UploadTask uploadTask = imgReference.putFile(file);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return imgReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri photoUrl = task.getResult();
                        values.put("profile_pic", photoUrl.toString());
                        Log.d(TAG, "onComplete: book cover uploaded successfully: " + photoUrl.toString());
                        updateProfileInfo();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: photo upload failure due to: " + e.getMessage());
                }
            });

        }
        else {
            updateProfileInfo();
        }
    }

    private void switchActivity(){
        Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish();
    }

    private void updateProfileInfo(){
        if(filePath==null && name.getText().toString().equals("") && email.getText().toString().equals("")){
            Toast.makeText(this, "Choose at least one field to update", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            relativeLayout.setAlpha(1f);
        }
        else
        {
            Log.d(TAG, "updateProfileInfo: profile photo = " + values.get(getString(R.string.field_profile_pic)));
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_users))
                    .child(mAuth.getCurrentUser().getUid());
            if(!name.getText().toString().equals("")){
                values.put(getString(R.string.field_name),name.getText().toString());
            }

            if(!email.getText().toString().equals("")){
                values.put(getString(R.string.field_email), email.getText().toString());
            }
            try {
                reference.updateChildren(values);
            }
            catch (Exception e){
                Log.d(TAG, "onComplete: " + e.getMessage());
            }
            progressBar.setVisibility(View.GONE);
            relativeLayout.setAlpha(1f);
            Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            switchActivity();
        }


    }

    private void toggleButtonEnable(){
        updateProfile.setEnabled(true);
    }

}
