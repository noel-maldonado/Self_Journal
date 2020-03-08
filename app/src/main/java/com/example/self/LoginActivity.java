package com.example.self;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import Model.Journal;
import util.JournalApi;

public class LoginActivity extends AppCompatActivity {
    //Tag used for Loh
    private static final String TAG = "LoginActivity";

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //FireStore Connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    //User Collection Reference
    private CollectionReference userReference = db.collection("Users");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initCreateAccountBtn();
        initLoginBtn();


        firebaseAuth = FirebaseAuth.getInstance();
    }


    private void initLoginBtn() {
        Button sign_in_button = (Button) findViewById(R.id.email_sign_in_button);
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AutoCompleteTextView tvEmail = (AutoCompleteTextView) findViewById(R.id.email);
                TextView tvPassword = (TextView) findViewById(R.id.password);
                String password = tvPassword.getText().toString();
                String email = tvEmail.getText().toString();
                loginEmailPasswordUser(email, password);

            }
        });

    }





    private void initCreateAccountBtn() {
        Button create_account_button = (Button) findViewById(R.id.create_acc_button);
        create_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void loginEmailPasswordUser(String email, String pwd) {
        //ensure fields arent empty
        if(!validateForm()) {
            return;
        }
        // Make the progress bar visible
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.loginProgress);
        progressBar.setVisibility(View.VISIBLE);

        //use the Fire Base sign in method for email and password
        firebaseAuth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        assert user != null;
                        final String currentUserId = user.getUid();

                        userReference
                                .whereEqualTo("userId", currentUserId)
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                        if (e != null) {

                                        }
                                        assert queryDocumentSnapshots != null;
                                        if (!queryDocumentSnapshots.isEmpty()) {

                                            progressBar.setVisibility(View.INVISIBLE);
                                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                JournalApi journalApi = JournalApi.getInstance();
                                                journalApi.setUsername(snapshot.getString("username"));
                                                journalApi.setUserId(snapshot.getString("userId"));
                                                // go to ListActivity
                                                Intent intent = new Intent(LoginActivity.this, JournalListActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                            }
                                        }
                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });



    }


    //method used to ensure User has Input'd text into Email & Password
    private boolean validateForm() {
        EditText mEmailField = (EditText) findViewById(R.id.email);
        EditText mPasswordField = (EditText) findViewById(R.id.password);
        boolean valid = true;

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
