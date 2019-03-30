package com.ips.lib.onlib;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.pix.Pix;
import com.fxn.utility.PermUtil;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ips.lib.onlib.Models.Book;
import com.ips.lib.onlib.Models.BookRefined;
import com.ips.lib.onlib.utils.BookHelper;
import com.ips.lib.onlib.utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.todddavies.components.progressbar.ProgressWheel;

import java.io.File;
import java.util.ArrayList;

public class AddBookActivity extends AppCompatActivity {

    private EditText bookId, name, author, edition, branch;
    private Button addBook;
    private Book book;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private BookRefined bookRefined;
    private static final String TAG = "AddBookActivity";
    private AlphaAnimation buttonClick;
    private ImageView bookCover;
    private ProgressBar progressBar;
    private TextView changeCover;
    private RelativeLayout relativeLayout;
    private static final int RequestCode = 100;
    private StorageReference storageRef;
    private String filePath;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);
        Toolbar toolbar = findViewById(R.id.add_book_toolbar);
        setSupportActionBar(toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        bookId = findViewById(R.id.bookID);
        name = findViewById(R.id.bookName);
        author = findViewById(R.id.bookAuthor);
        edition = findViewById(R.id.bookEdition);
        addBook = findViewById(R.id.addBookBtn);
        branch = findViewById(R.id.bookBranch);
        bookCover = findViewById(R.id.bookCoverIv);
        changeCover = findViewById(R.id.changeCoverTv);
        initImageLoader();
        changeCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Pix.start(AddBookActivity.this,
                        RequestCode);
            }
        });
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);
        relativeLayout = findViewById(R.id.mainRelativeLayout);
        relativeLayout.setAlpha(1f);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        book = new Book();
        buttonClick =  new AlphaAnimation(1F, 0.8F);
        addBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                if(bookId.getText().toString().equals("") ||
                        name.getText().toString().equals("") ||
                        author.getText().toString().equals("") ||
                        edition.getText().toString().equals("") ||
                        branch.getText().toString().equals("")){
                    Toast.makeText(AddBookActivity.this, "Book details cannot be Empty", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    closeKeyBoard();
                    book.setBook_id(bookId.getText().toString());
                    book.setName(name.getText().toString());
                    book.setAuthor(author.getText().toString());
                    book.setEdition(edition.getText().toString());
                    book.setBranch(branch.getText().toString());
                    uploadCover();
                    book.setIs_issued("false");
                    book.setIssue_date("");
                    book.setIssued_to("");
                    Query query = myRef.child(getString(R.string.dbname_books))
                            .child(book.getBook_id());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                Log.d(TAG, "onDataChange: book id exists");
                                Toast.makeText(AddBookActivity.this, "Book with this ID already exists!", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Log.d(TAG, "onDataChange: book id doesn't exist");
                                progressBar.setVisibility(View.VISIBLE);
                                relativeLayout.setAlpha(0.5f);
                                addBookDetails(book);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

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

    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(AddBookActivity.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == RequestCode) {
            ArrayList<String> returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            Log.d(TAG, "onActivityResult: " + Uri.parse(returnValue.get(0)));
            bookCover.setImageURI(Uri.parse(returnValue.get(0)));
            filePath = returnValue.get(0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(AddBookActivity.this,
                            RequestCode);
                } else {
                    Toast.makeText(this, "Approve permissions to open image Picker", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void addBookDetails(final Book book){

            Log.d(TAG, "addBookDetails: called");
        Uri file = Uri.fromFile(new File(filePath));
        final StorageReference imgReference = storageRef.child("photos/" + "covers/" + book.getBook_id() + "/cover");

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
                    book.setCover(photoUrl.toString());
                    Log.d(TAG, "onComplete: book cover uploaded successfully: " + photoUrl.toString());
                    //adding book to database
                    final String key = book.getBook_id();
                    myRef.child(getString(R.string.dbname_books))
                            .child(key).setValue(book);
                    final String refined_key = BookHelper.getRefinedKey(book);
                    Query query1 = myRef.child(getString(R.string.dbname_refined_books))
                            .child(refined_key);


                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                bookRefined = dataSnapshot.getValue(BookRefined.class);
                                updateRefinedBook(bookRefined, refined_key);
                            } else {
                                updateRefinedBook(null, refined_key);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: photo upload failure due to: " + e.getMessage());
            }
        });




    }

    private void updateRefinedBook(BookRefined bookRefined1, String key){
        if(bookRefined1 != null){
          int a =   Integer.parseInt(bookRefined1.getAvailable()) + 1;
          int t =   Integer.parseInt(bookRefined1.getTotal()) + 1;
          bookRefined1.setAvailable(String.valueOf(a));
          bookRefined1.setTotal(String.valueOf(t));
            myRef.child(getString(R.string.dbname_refined_books))
                    .child(key).setValue(bookRefined1);
        }
        else
        {   BookRefined bookRefined2 = new BookRefined();
            bookRefined2.setAvailable("1");
            bookRefined2.setTotal("1");
            bookRefined2.setName(book.getName());
            bookRefined2.setAuthor(book.getAuthor());
            bookRefined2.setEdition(book.getEdition());
            bookRefined2.setCover(book.getCover());
            bookRefined2.setBranch(book.getBranch());
            myRef.child(getString(R.string.dbname_refined_books))
                    .child(key).setValue(bookRefined2);
        }

        Toast.makeText(this, "Book added successfully!", Toast.LENGTH_SHORT).show();
        bookId.getText().clear();
        progressBar.setVisibility(View.GONE);
        relativeLayout.setAlpha(1);

    }

    private void uploadCover(){
        Uri file = Uri.fromFile(new File(filePath));
        final StorageReference imgReference = storageRef.child("photos/" + "covers/" + book.getBook_id() + "/cover");

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
                    book.setCover(photoUrl.toString());
                    Log.d(TAG, "onComplete: book cover uploaded successfully: " + photoUrl.toString());
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: photo upload failure due to: " + e.getMessage());
            }
        });
    }

    private void closeKeyBoard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}
