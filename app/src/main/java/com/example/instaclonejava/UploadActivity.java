package com.example.instaclonejava;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.instaclonejava.databinding.ActivityUploadBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {
    private ActivityUploadBinding binding;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    //Bitmap selectedImage;
    Uri imageData;

    FirebaseStorage firebaseStorage;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();





    }

    public void upload(View view){

        if(imageData != null){
            //universal unique id
            UUID uuid = UUID.randomUUID();
            String imageId = "images/"+uuid+".jpg";

            storageReference.child(imageId).putFile(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    //Dowload url
                    StorageReference newStorageReference = FirebaseStorage.getInstance().getReference(imageId);
                    newStorageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String dowloadedUrl = uri.toString();



                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            String email = user.getEmail();

                            String comment = binding.commentText.getText().toString();

                            HashMap<String,Object> postData = new HashMap<>();
                            postData.put("userEmail",email);
                            postData.put("dowloadedUrl",dowloadedUrl);
                            postData.put("comment",comment);
                            postData.put("date", FieldValue.serverTimestamp());// getting current time for firebase

                            firebaseFirestore.collection("Posts").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Intent intent = new Intent(UploadActivity.this,FeedActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG);
                }
            });
        }

    }


    public void selectImage(View view){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //permission Denied
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"need permission for gallery",Snackbar.LENGTH_INDEFINITE).setAction("need Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            }else{
                // request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }else{
            // gallery

            // Pick a photo from gallery
            Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        }

    }

    private void registerLauncher(){

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(intentToGallery);
                }
                else
                    Toast.makeText(UploadActivity.this,"need permission",Toast.LENGTH_LONG).show();
            }
        });

        galleryLauncher =  registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK){
                    // Checking if user picked a photo or not
                    Intent intentResult = result.getData();
                    if (intentResult != null){
                        //intentFromResult.getData(); gives the Uri of the photo that user picked.
                        imageData = intentResult.getData();// URI yeterli oluyor
                        binding.uploadedImage.setImageURI(imageData);
                        /* bunu da kullanmak isteyebilirsin başka projelerde mesela SQlite kullanıp içine bitmap atacaksan
                        try{
                            if(Build.VERSION.SDK_INT >= 28){
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.uploadedImage.setImageBitmap(selectedImage);
                            }else{
                                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(),imageData);
                                binding.uploadedImage.setImageBitmap(selectedImage);
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }*/

                    }
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}