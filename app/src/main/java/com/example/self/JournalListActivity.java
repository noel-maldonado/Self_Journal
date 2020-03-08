package com.example.self;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import Model.Journal;
import ui.JournalRecyclerAdapter;
import util.JournalApi;

public class JournalListActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private List<Journal> journalList;

    private RecyclerView recyclerView;
    private JournalRecyclerAdapter adapter;

    //Journal Collection Reference
    private CollectionReference jornalReference = db.collection("Journal");
    private TextView noJournalEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal_list);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        noJournalEntry = findViewById(R.id.list_no_thoughts);

        journalList = new ArrayList<>();


        recyclerView = findViewById(R.id.recyclerView);
        //ensures that the size is fixed; dont forget to do that
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



    }

    //adds the custom Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // adds the functionality of the buttons in the Menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_add:
                //take users to add Journal
                if (user != null && firebaseAuth != null) {
                    Intent intent = new Intent(JournalListActivity.this, PostJournalActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    //finish();
                }
                break;
            case R.id.action_signout:
                //Signs Out the User
                if (user != null && firebaseAuth != null) {
                    firebaseAuth.signOut();
                    Intent intent = new Intent(JournalListActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

                break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //need to querry collection on onStart              //JournalApi gets current UserID and references the docuemnts that contain the same userId
        jornalReference.whereEqualTo("userId", JournalApi.getInstance().
                getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.isEmpty()) {

                            for(QueryDocumentSnapshot journals : queryDocumentSnapshots) {
                                //maps the objects we get to a Journal Object
                                Journal journal = journals.toObject(Journal.class);
                                journalList.add(journal);

                            }

                            //Invoke recyclerView
                            adapter = new JournalRecyclerAdapter(JournalListActivity.this,
                                    journalList);
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();


                        }else {
                            noJournalEntry.setVisibility(View.VISIBLE);


                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }







}
