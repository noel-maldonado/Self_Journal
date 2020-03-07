package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import util.JournalApi;

public class PostJournalActivity extends AppCompatActivity {

    private static final int GALLERY_CODE = 1;
    private Uri imgaeUri;


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


        //Connecting Current User TextView with ID

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


    private void initSaveBtn() {

        Button btnSave = (Button) findViewById(R.id.post_save_journal_button);

    }

    private void initPostbtn() {
        ImageView ivImage = (ImageView) findViewById(R.id.imageView);
//        TextView tvUsername = (TextView) findViewById(R.id.post_username_textview);
        TextView tvDate = (TextView) findViewById(R.id.post_date_textview);
        ImageView ivAddPhotoBtn = (ImageView) findViewById(R.id.postCameraButton);

        Button btnSave = (Button) findViewById(R.id.post_save_journal_button);






    }


private void initAddPhotoBtn() {
    ImageView ivImage = (ImageView) findViewById(R.id.imageView);

    ImageView ivAddPhotoBtn = (ImageView) findViewById(R.id.postCameraButton);

    //get Image from gallery/phone
    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
    //sets the type to anything image related/folder
    intent.setType("image/*");
    startActivityForResult(intent, GALLERY_CODE);


}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Checkts to see if the Intent is from Add Photo Btn method (GALLER_CODE) and that is was successful (RESULT_OK)
        if (requestCode == GALLERY_CODE && requestCode == RESULT_OK) {
            //data received from intent
            if (data != null) {
                imgaeUri = data.getData(); //GALLERY Code is associated with Uri data being received // path to the image
                ImageView ivImage = (ImageView) findViewById(R.id.imageView);
                ivImage.setImageURI(imgaeUri); //show image
            }
        }
    }


    private void saveJournal() {
        EditText etTitle = (EditText) findViewById(R.id.post_title_et);
        EditText etThoughts = (EditText) findViewById(R.id.post_description_et);
        final String title = etTitle.getText().toString().trim();
        final String thoughts = etThoughts.getText().toString().trim();

        ProgressBar postProgress = (ProgressBar) findViewById(R.id.post_progressBar);
        postProgress.setVisibility(View.VISIBLE);




    }






}
