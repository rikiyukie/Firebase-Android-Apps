package com.smart.reyog;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.google.firebase.database.core.Tag;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.smart.reyog.Model.Notification;
import com.smart.reyog.Model.Pentas;
import com.smart.reyog.Model.Respon;
import com.smart.reyog.Model.Sender;
import com.smart.reyog.Retrofit.APIService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Form_Pentas extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private EditText edtNama, edtLokasi, edtJadwal, edtInfo;
    private Button btnUpload;
    private ImageView imgBanner;
    private Uri filePath;
    private String imgURL;
    private ProgressBar progressBar;

    FirebaseStorage storage;
    StorageReference storageReference;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_pentas_layout);

        //firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mService = Common.getFCMClient();

        edtNama = (EditText)findViewById(R.id.edit_namaPentas);
        edtLokasi = (EditText)findViewById(R.id.edit_lokasiPentas);
        edtJadwal = (EditText)findViewById(R.id.edit_jadwalPentas);
        edtInfo = (EditText)findViewById(R.id.edit_infoPentas);
        btnUpload = (Button)findViewById(R.id.upload_pentas);
        imgBanner = (ImageView)findViewById(R.id.banner);
        progressBar = (ProgressBar)findViewById(R.id.progressBar_form);
        progressBar.setVisibility(View.GONE);

        imgBanner.setOnClickListener(pilihImage);
        btnUpload.setOnClickListener(klikUpload);
    }

    private View.OnClickListener pilihImage = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Banner"), PICK_IMAGE);

        }
    };

    public static final int PICK_IMAGE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null){

            filePath = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imgBanner.setImageBitmap(bitmap);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private View.OnClickListener klikUpload = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String nama = edtNama.getText().toString();
            String lokasi = edtLokasi.getText().toString();
            String jadwal = edtJadwal.getText().toString();
            String info = edtInfo.getText().toString();

            if (!validateForm(nama, lokasi, jadwal, info)){
                return;
            }

            if (!validateBanner()){
                return;
            }
        }
    };

    private boolean validateBanner() {
        boolean result = true;
        if (filePath != null){

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading Banner...");
            progressDialog.show();

            final StorageReference ref = storageReference.child("banner/"+ UUID.randomUUID().toString());
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(Form_Pentas.this, "Banner Uploaded", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded "+(int)progress+"%");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(Form_Pentas.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        Uri downUri = task.getResult();
                        imgURL = downUri.toString();

                        uploadToDatabase();
                    }

                }
            });

        }else {
            Toast.makeText(this, "Tambahkan Banner..!!", Toast.LENGTH_SHORT).show();
            result = false;
        }

        return result;
    }

    private void uploadToDatabase() {
        final String nama = edtNama.getText().toString();
        final String lokasi = edtLokasi.getText().toString();
        String jadwal = edtJadwal.getText().toString();
        String info = edtInfo.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());

        Pentas pentas = new Pentas(nama,lokasi,jadwal,info, imgURL, currentDateandTime);
        String post_id = mDatabase.push().getKey();
        mDatabase.child("PENTAS").child(post_id).setValue(pentas).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    Toast.makeText(Form_Pentas.this, "Upload Berhasil", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);

                    Notification notification = new Notification(lokasi,nama);
                    Sender sender = new Sender("/topics/Pentas", notification);
                    mService.sendNotification(sender).enqueue(new Callback<Respon>() {
                        @Override
                        public void onResponse(Call<Respon> call, Response<Respon> response) {

                        }

                        @Override
                        public void onFailure(Call<Respon> call, Throwable t) {

                        }
                    });

                    //back to home admin
                    onBackPressed();
                }else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Form_Pentas.this, "Upload Gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateForm(String nama, String lokasi, String jadwal, String info) {
        boolean result = true;
        if (TextUtils.isEmpty(nama)){
            edtNama.setError("Harus Diisi");
            result = false;
        }else {
            edtNama.setError(null);
        }

        if (TextUtils.isEmpty(lokasi)){
            edtLokasi.setError("Harus Diisi");
            result = false;
        }else {
            edtNama.setError(null);
        }

        if (TextUtils.isEmpty(jadwal)){
            edtJadwal.setError("Harus Diisi");
            result = false;
        }else {
            edtJadwal.setError(null);
        }

        if (TextUtils.isEmpty(info)){
            edtInfo.setError("Harus Diisi");
            result = false;
        }else  {
            edtInfo.setError(null);
        }

        return result;
    }

    public void onBackPressed(){
        //kembali ke home page
        Intent back = new Intent(Form_Pentas.this, Admin_Page.class);
        startActivity(back);
        finish();
    }
}
