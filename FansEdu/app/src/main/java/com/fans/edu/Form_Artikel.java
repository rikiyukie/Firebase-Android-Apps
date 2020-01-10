package com.fans.edu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Form_Artikel extends AppCompatActivity {

    private EditText edtJudul, edtIsi;
    private Button btnUpload;
    private ImageView img;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri filePath;
    private String judul, isi, gambarUrl;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_artikel_layout);

        //firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        edtJudul = (EditText)findViewById(R.id.edit_judulArtikel);
        edtIsi = (EditText)findViewById(R.id.edit_isiArtikel);
        btnUpload = (Button)findViewById(R.id.upArtikel);
        img = (ImageView)findViewById(R.id.gambar);
        progressBar = (ProgressBar)findViewById(R.id.progressBar_Artikel);
        progressBar.setVisibility(View.GONE);

        img.setOnClickListener(selectImg);
        btnUpload.setOnClickListener(Upload);

    }

    public static final int PICK_IMAGE = 1;
    //String judul = edtJudul.getText().toString();
    //String isi = edtIsi.getText().toString();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){

            filePath = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                img.setImageBitmap(bitmap);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private View.OnClickListener selectImg = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Banner"), PICK_IMAGE);
        }
    };

    private View.OnClickListener Upload = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            judul = edtJudul.getText().toString();
            isi = edtIsi.getText().toString();
            if (!validateForm()){
                return;
            }

            if (!validateImg()){
                return;
            }
        }
    };

    private void UploadToDatabase() {
        progressBar.setVisibility(View.VISIBLE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        Artikel artikel = new Artikel(judul,isi,gambarUrl,currentDateandTime);

        //upload firebase
        mDatabase.child("ARTIKEL").child("artikel"+currentDateandTime).setValue(artikel).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Form_Artikel.this, "Upload Success", Toast.LENGTH_SHORT).show();

                    onBackPressed();
                }else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Form_Artikel.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateForm(){
        boolean result = true;
        if (TextUtils.isEmpty(judul)){
            edtJudul.setError("Belum Diisi");
            result = false;
        }else {
            edtJudul.setError(null);
        }

        if (TextUtils.isEmpty(isi)){
            edtIsi.setError("Belum Diisi");
            result = false;
        }else {
            edtIsi.setError(null);
        }

        return result;
    }

    private boolean validateImg(){
        final boolean result = true;

        if (filePath != null){

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading Gambar Artikel...");
            progressDialog.show();

            final StorageReference ref = storageReference.child("gambar/"+ UUID.randomUUID().toString());
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(Form_Artikel.this, "Gambar Uploaded", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(Form_Artikel.this, "Failed", Toast.LENGTH_SHORT).show();
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
                        UploadToDatabase();
                    }
                }
            });
        }else {
            Toast.makeText(this, "Tambahkan Banner Artikel", Toast.LENGTH_SHORT).show();
            //gambarUrl = "null";
            //UploadToDatabase();
        }

        return result;
    }

    public void onBackPressed(){
        //kembali ke home page
        Intent back = new Intent(Form_Artikel.this, Halaman_Admin.class);
        startActivity(back);
        finish();
    }
}
