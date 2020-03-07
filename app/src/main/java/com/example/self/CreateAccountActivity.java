package com.example.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import util.JournalApi;

public class CreateAccountActivity extends AppCompatActivity {
    //Tag used for Log
    private static final String TAG = "CreateAccountActivity";

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //FireStore Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //Users Collection Refernce
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);
        initCreateAccountBtn();

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if (currentUser != null) {
                    //user is already logged in

                } else{
                    //no user yet..

                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    private void initCreateAccountBtn() {
        Button create_account_button = (Button) findViewById(R.id.create_acc_button_account);
        create_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText eUsername = (EditText) findViewById(R.id.username_account);
                AutoCompleteTextView eEmail = (AutoCompleteTextView) findViewById(R.id.email_account);
                EditText ePassword = (EditText) findViewById(R.id.password_account);
                String username = eUsername.getText().toString();
                String email = eEmail.getText().toString();
                String password = ePassword.getText().toString();

                createUserEmailAccount(email, password, username);



            }
        });
    }

    private void createUserEmailAccount(String email, String password, final String username) {
        Log.d(TAG, "createAccount: " + email);
        if(!validateForm()) {
            return;
        }

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.create_acct_progress);

        progressBar.setVisibility(View.VISIBLE);

        //Start creation of User with Email
        firebaseAuth .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Chekcs to see if task was a success
                        if(task.isSuccessful()) {
                            //sign up is successful
                            Log.d(TAG, "createUserWithEmail: success");
                            currentUser = firebaseAuth.getCurrentUser();
//                            sendEmailVerification();

                            EditText mFullNameField = (EditText) findViewById(R.id.fullname_account);
                            final String fullName = mFullNameField.getText().toString().trim();
                            //creates profile change request to place Full Name as Display Name
                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName).build();

                            currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Display Name updated: " + fullName);
                                    }
                                }
                            });


                            //getUid needs to assert that it is not null
                            assert currentUser != null;
                            //Create unique Id for User
                            final String currentUserID = currentUser.getUid();

                            //Create a user Map we can create a user in the User Collection
                            Map<String, String> userObj = new HashMap<>();
                            userObj.put("userId", currentUserID);
                            userObj.put("username", username);

                            //save to FireStore Database
                            collectionReference.add(userObj)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            documentReference.get()
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (Objects.requireNonNull(task.getResult().exists())) {
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                                String name = task.getResult()
                                                                        .getString("username");

                                                                JournalApi journalApi = JournalApi.getInstance(); //Global API
                                                                journalApi.setUserId(currentUserID);
                                                                journalApi.setUsername(name);

                                                                Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                intent.putExtra("username", name);
                                                                intent.putExtra("userId", currentUserID);
                                                                startActivity(intent);




                                                            } else {
                                                                progressBar.setVisibility(View.INVISIBLE);

                                                            }
                                                        }
                                                    });


                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });







                        } else {
                            //sign up failed
                            Log.w(TAG, "createUserWithEmail: failure", task.getException());
                            Toast.makeText(CreateAccountActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "createUserWithEmail: method error" + e.toString());
            }
        });





    }


    //method used to ensure User has Input'd text into Email & Password
    private boolean validateForm() {
        EditText mUsername = (EditText) findViewById(R.id.username_account);
        AutoCompleteTextView mEmailField = (AutoCompleteTextView) findViewById(R.id.email_account);
        EditText mPasswordField = (EditText) findViewById(R.id.password_account);

        boolean valid = true;

        String username = mUsername.getText().toString();
        if(TextUtils.isEmpty(username)) {
            mUsername.setError("Required.");
            valid = false;
        } else {
            mUsername.setError(null);
        }

        String email = mEmailField.getText().toString();
        if(TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if(TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

}
