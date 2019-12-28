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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.smart.reyog.Model.Pentas;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class EditPentas extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView edtBanner;
    private EditText edtNama, edtLokasi, edtJadwal, edtInfo;
    private Button btnSimpan;
    private String bannerUrl, txtNamaRef;
    private Uri filePath;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_pentas_layout);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        edtBanner = (ImageView)findViewById(R.id.edt_banner);
        edtNama = (EditText)findViewById(R.id.edt_edit_namaPentas);
        edtLokasi = (EditText)findViewById(R.id.edt_edit_lokasiPentas);
        edtJadwal = (EditText)findViewById(R.id.edt_edit_jadwalPentas);
        edtInfo = (EditText)findViewById(R.id.edt_edit_infoPentas);
        btnSimpan = (Button)findViewById(R.id.save_pentas);

        progressBar = (ProgressBar)findViewById(R.id.progressBar_form_edt);
        progressBar.setVisibility(View.GONE);

        txtNamaRef = getIntent().getExtras().getString("iJudul");
        String txtNama = getIntent().getExtras().getString("iJudul");
        String txtLokasi = getIntent().getExtras().getString("iLokasi");
        String txtJadwal = getIntent().getExtras().getString("iJadwal");
        String txtInfo = getIntent().getExtras().getString("iInfo");
        String txtUrlBanner = getIntent().getExtras().getString("iBanner");

        edtNama.setText(txtNama);
        edtLokasi.setText(txtLokasi);
        edtJadwal.setText(txtJadwal);
        edtInfo.setText(txtInfo);
        bannerUrl = txtUrlBanner;
        Picasso.with(this).load(bannerUrl).into(edtBanner);

        edtBanner.setOnClickListener(gantiBanner);
        btnSimpan.setOnClickListener(saveButton);
    }

    public static final int PICK_IMAGE = 1;
    private View.OnClickListener gantiBanner = new View.OnClickListener() {
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
                edtBanner.setImageBitmap(bitmap);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private View.OnClickListener saveButton = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String nama = edtNama.getText().toString();
            String lokasi = edtLokasi.getText().toString();
            String jadwal = edtJadwal.getText().toString();
            String info = edtInfo.getText().toString();

            if (!validateForm(nama, lokasi, jadwal, info)){
                return;
            }

            if (filePath != null){
                storageReference.getStorage().getReferenceFromUrl(bannerUrl).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            UploadNewBanner();
                        }
                    }
                });
            }else {
                uploadToDatabase();
            }

        }
    };

    private void UploadNewBanner() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Banner...");
        progressDialog.show();
        final StorageReference ref = storageReference.child("banner/"+ UUID.randomUUID().toString());
        ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(EditPentas.this, "Banner Uploaded", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(EditPentas.this, "Failed", Toast.LENGTH_SHORT).show();
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
                    bannerUrl = downUri.toString();

                    uploadToDatabase();
                }
            }
        });

    }

    private void uploadToDatabase() {
        String nama = edtNama.getText().toString();
        String lokasi = edtLokasi.getText().toString();
        String jadwal = edtJadwal.getText().toString();
        String info = edtInfo.getText().toString();

        progressBar.setVisibility(View.VISIBLE);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());

        final Pentas pentas = new Pentas(nama,lokasi,jadwal,info, bannerUrl, currentDateandTime);
        mDatabase.child("PENTAS").orderByChild("namaPentas").equalTo(txtNamaRef).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data: dataSnapshot.getChildren()){
                    data.getRef().setValue(pentas).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(EditPentas.this, "Data Berhasil Diperbarui", Toast.LENGTH_SHORT).show();
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
            edtLokasi.setError(null);
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
        }else {
            edtInfo.setError(null);
        }

        return result;
    }

    public void onBackPressed(){
        Intent back = new Intent(EditPentas.this, KelolaPentas.class);
        startActivity(back);
        finish();
    }
}
