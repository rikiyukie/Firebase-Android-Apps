package com.fans.edu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fans.edu.Model.Artikel;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Form_Artikel_Edit extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView bannerEdit;
    private EditText judulEdit, isiEdit;
    private Button saveEdit;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri filePath;
    private DatabaseReference mDatabase;
    private String gambarUrl, txtjudul;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_artikel_edit);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        bannerEdit = (ImageView)findViewById(R.id.gambar_edit);
        judulEdit = (EditText)findViewById(R.id.judulartikel_edit);
        isiEdit = (EditText)findViewById(R.id.isiartikel_edit);
        saveEdit = (Button)findViewById(R.id.btnsave_edit);
        progressBar = (ProgressBar)findViewById(R.id.progressBar_Artikel);
        progressBar.setVisibility(View.GONE);

        txtjudul = getIntent().getExtras().getString("iJudul");
        String txtisi = getIntent().getExtras().getString("iIsi");
        gambarUrl = getIntent().getExtras().getString("iBanner");

        judulEdit.setText(txtjudul);
        isiEdit.setText(txtisi);
        Picasso.with(this).load(gambarUrl).into(bannerEdit);

        bannerEdit.setOnClickListener(picknewbanner);
        saveEdit.setOnClickListener(validatesave);
    }

    private View.OnClickListener validatesave = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String judul = judulEdit.getText().toString();
            String isi = isiEdit.getText().toString();
            if (!validateform(judul, isi)){
                return;
            }

            if (filePath != null){
                storageReference.getStorage().getReferenceFromUrl(gambarUrl).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            uploadnewbanner();
                        }
                    }
                });
            }else {
                uploadtodatabase();
            }
        }
    };

    private void uploadnewbanner() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Gambar Artikel...");
        progressDialog.show();

        final StorageReference ref = storageReference.child("gambar/"+ UUID.randomUUID().toString());
        ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(Form_Artikel_Edit.this, "Gambar Uploaded", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progres = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setMessage("Uploaded "+(int)progres+"%");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(Form_Artikel_Edit.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()){
                    throw task.getException();
                }
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()){
                    Uri downUri = task.getResult();
                    gambarUrl = downUri.toString();
                    uploadtodatabase();
                }
            }
        });

    }


    private void uploadtodatabase() {
        progressBar.setVisibility(View.VISIBLE);
        String judul = judulEdit.getText().toString();
        String isi = isiEdit.getText().toString();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());

        final Artikel artikel = new Artikel(judul, isi, gambarUrl, currentDateandTime);
        mDatabase.child("ARTIKEL").orderByChild("judul").equalTo(txtjudul).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()){
                    data.getRef().setValue(artikel).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Form_Artikel_Edit.this, "Data Berhasil Diperbarui", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private boolean validateform(String judul, String isi) {
        boolean result = true;
        if (TextUtils.isEmpty(judul)){
            judulEdit.setError("harus diisi");
            result = false;
        }else {
            judulEdit.setError(null);
        }

        if (TextUtils.isEmpty(isi)){
            isiEdit.setError("harus diisi");
            result = false;
        }else {
            isiEdit.setError(null);
        }

        return result;
    }

    public static final int PICK_IMAGE = 1;
    private View.OnClickListener picknewbanner = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Banner"), PICK_IMAGE);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){

            filePath = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                bannerEdit.setImageBitmap(bitmap);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void onBackPressed(){
        //kembali ke home page
        Intent back = new Intent(Form_Artikel_Edit.this, Kelola_Artikel.class);
        startActivity(back);
        finish();
    }
}
