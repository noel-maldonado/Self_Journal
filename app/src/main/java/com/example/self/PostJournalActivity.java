package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.Timestamp;

import java.util.Date;

import Model.Journal;

import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity {
    //used for Log
    private static final String TAG = "PostJournalActivity";


    private static final int GALLERY_CODE = 1;
    private Uri imageUri;


    private String currentUserId;
    private String currentUserName;

    //Declaration to FireBase Authentication Server
    private FirebaseAuth firebaseAuth;
    //Declaration to Authentication State Listener to monitor changes with the User
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //connection to FireStore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //reference to storage
    private StorageReference storageReference;

    //Collection Reference
    private CollectionReference journalReference = db.collection("Journal");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);
        initPostbtn();
        initAddPhotoBtn();


        // Firebase user Authentication connection (required on onCreate)
        firebaseAuth = FirebaseAuth.getInstance();

        if (JournalApi.getInstance() != null) {
            currentUserId = JournalApi.getInstance().getUserId();
            currentUserName = JournalApi.getInstance().getUsername();
            //sets UserName to the TextView associated
            TextView tvCurrentUsername = (TextView) findViewById(R.id.post_username_textview);
            tvCurrentUsername.setText(currentUserName);
        }
        //Instantiating Authentication Listener
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {

                }else {

                }
            }
        };

    }


    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null) {
            //removes the listener when no longer using the activity; saves battery/ processing power on the phone
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }




    private void initPostbtn() {

        Button btnSave = (Button) findViewById(R.id.post_save_journal_button);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etTitle = (EditText) findViewById(R.id.post_title_et);
                EditText etThoughts = (EditText) findViewById(R.id.post_description_et);
                String title = etTitle.getText().toString().trim();
                String thoughts = etThoughts.getText().toString().trim();
                //calls the save method with the current parameters inserted
                saveJournal(title, thoughts);
            }
        });





    }


private void initAddPhotoBtn() {

    ImageView ivAddPhotoBtn = (ImageView) findViewById(R.id.postCameraButton);
    ivAddPhotoBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //get Image from gallery/phone
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            //sets the type to anything image related/folder
            intent.setType("image/*");
            startActivityForResult(intent, GALLERY_CODE);
        }
    });



}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Checks to see if the Intent is from Add Photo Btn method (GALLER_CODE) and that is was successful (RESULT_OK)
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            //data received from intent
            if (data != null) {
                imageUri = data.getData(); //GALLERY Code is associated with Uri data being received // path to the image
                ImageView ivImage = (ImageView) findViewById(R.id.imageView);
                ivImage.setImageURI(imageUri); //show image
            }
        }
    }


    //method used to save data and add into FireStore
    private void saveJournal(final String title, final String thoughts) {
        if(!validateForm()) {
            return;
        }
        final ProgressBar postProgress = (ProgressBar) findViewById(R.id.post_progressBar);
        postProgress.setVisibility(View.VISIBLE);

        final StorageReference filepath = storageReference //the path to the where the Image is saved
                .child("journal_images")
                .child("my_image_" + Timestamp.now().getSeconds());

        filepath.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                //Todo: create a Journal Object - model
                                Journal journal = new Journal();
                                journal.setTitle(title);
                                journal.setThought(thoughts);
                                journal.setImageUrl(imageUrl);
                                journal.setTimeAdded(new Timestamp(new Date()));
                                journal.setUserName(currentUserName);
                                journal.setUserId(currentUserId);

                                //Todo: invoke our collectionReferece
                                journalReference.add(journal)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                postProgress.setVisibility(View.INVISIBLE);
                                                Intent intent = new Intent(PostJournalActivity.this, JournalListActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: " + e.getMessage());

                                            }
                                        });
                                //Todo: and save a Journal instance


                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    //ensures the correct data has been inserted and not left blank
    private boolean validateForm() {
        EditText etTitle = (EditText) findViewById(R.id.post_title_et);
        EditText etThoughts = (EditText) findViewById(R.id.post_description_et);
        final String title = etTitle.getText().toString().trim();
        final String thoughts = etThoughts.getText().toString().trim();


        boolean valid = true;

        if(TextUtils.isEmpty(title)) {
            etTitle.setError("Required.");
            valid = false;
        } else {
            etTitle.setError(null);
        }

        if(TextUtils.isEmpty(thoughts)) {
            etThoughts.setError("Required.");
            valid = false;
        } else {
            etThoughts.setError(null);
        }

        if(imageUri == null) {
            Toast.makeText(this, "Please add an Image", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;

    }




}
