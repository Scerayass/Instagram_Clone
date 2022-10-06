package com.example.instaclonejava;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.instaclonejava.databinding.ActivityFeedBinding;
import com.example.instaclonejava.databinding.RecyclerowBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {
    private ActivityFeedBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;

    ArrayList<Post> postArrayList;
    PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        postArrayList = new ArrayList<>();

        getDataFromFirestore();
        binding.RecycleView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter= new PostAdapter(postArrayList);
        binding.RecycleView.setAdapter(postAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.feedmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void getDataFromFirestore(){
       // CollectionReference collectionReference = firebaseFirestore.collection("Posts");

        firebaseFirestore.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Toast.makeText(getApplicationContext(),error.getLocalizedMessage(),Toast.LENGTH_LONG);
                }
                if (value != null){

                    for (DocumentSnapshot snapshot : value.getDocuments()){
                        Map<String,Object> data = snapshot.getData();

                        String userEmail = (String) data.get("userEmail");
                        String dowloadedUrl = (String) data.get("dowloadedUrl");
                        String comment = (String) data.get("comment");

                        Post post = new Post(comment,dowloadedUrl,userEmail);
                        postArrayList.add(post);
                    }
                    postAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.addPost){
            Intent intent = new Intent(getApplicationContext(),UploadActivity.class);
            startActivity(intent);
        }
        else if(item.getItemId() == R.id.logOut){
            auth.signOut();

            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}