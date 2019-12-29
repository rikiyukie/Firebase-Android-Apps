package com.cetakin.anterin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cetakin.anterin.Model.Order;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class CetakinPage extends AppCompatActivity {

    private Button pickFile, sendOrder;
    private EditText edtAlamat, edtNoHp, edtKeterangan;
    private TextView txt_filepath, txt_nama;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private Uri filePath;
    private String emailUser, namaUser, namefile;
    private Dialog statusDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cetakin_page);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        edtAlamat = (EditText)findViewById(R.id.cetakin_alamat);
        edtNoHp = (EditText)findViewById(R.id.cetakin_NoHP);
        edtKeterangan = (EditText)findViewById(R.id.cetakin_keterangan);
        txt_filepath = (TextView)findViewById(R.id.file_patch);
        txt_nama = (TextView)findViewById(R.id.cetakin_nama);
        sendOrder = (Button)findViewById(R.id.cetakin_send);
        pickFile = (Button)findViewById(R.id.btn_tambah);

        sendOrder.setOnClickListener(BuatOrder);
        pickFile.setOnClickListener(filePicker);

        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    emailUser = dataSnapshot.child("Email").getValue().toString();
                    namaUser = dataSnapshot.child("Nama").getValue().toString();
                    txt_nama.setText(namaUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DialogStatus();
    }

    private View.OnClickListener BuatOrder = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String alamat = edtAlamat.getText().toString();
            String nohp = edtNoHp.getText().toString();
            String keterangan = edtKeterangan.getText().toString();
            if (!falidateForm(alamat, nohp, keterangan)){
                return;
            }

            cekStatusToko();
        }
    };

    private void cekStatusToko() {
        DatabaseReference refStatus = mDatabase.child("ADMIN").child("MyToko").child("Status");
        refStatus.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String statustoko = dataSnapshot.getValue().toString();
                if (statustoko.equalsIgnoreCase("buka")){
                    uploadFile();
                }else {
                    statusDialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadFile() {
        if (filePath != null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading File...");
            progressDialog.show();
            progressDialog.setCancelable(false);

            SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());
            String namaFile = currentDateandTime+"_"+namefile;
            final StorageReference ref = storageReference.child("file/"+ namaFile);
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(CetakinPage.this, "Uploaded", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(CetakinPage.this, "Failed", Toast.LENGTH_SHORT).show();
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
                        String fileUrl = downUri.toString();
                        UploadToDatabase(fileUrl);
                    }

                }
            });
        }else {
            Toast.makeText(this, "Pilih File Dulu", Toast.LENGTH_SHORT).show();
        }
    }

    private void UploadToDatabase(String fileUrl) {
        String alamat = edtAlamat.getText().toString();
        String nohp = edtNoHp.getText().toString();
        String keterangan = edtKeterangan.getText().toString();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        String orderStatus = "antri";

        Order order = new Order(emailUser, namaUser, alamat, nohp, keterangan, fileUrl, currentDateandTime, orderStatus);
        mDatabase.child("Order").child("order"+currentDateandTime).setValue(order).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(CetakinPage.this, "Pesanan Berhasil Dibuat", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean falidateForm(String alamat, String nohp, String keterangan){
        boolean result = true;
            if (TextUtils.isEmpty(alamat)){
                edtAlamat.setError("Wajib Diisi");
                result = false;
            }else {
                edtAlamat.setError(null);
            }

            if (TextUtils.isEmpty(nohp)){
                edtNoHp.setError("Wajib Diisi");
                result = false;
            }else {
                edtNoHp.setError(null);
            }

            if (TextUtils.isEmpty(keterangan)){
                edtKeterangan.setText("-");
                result = true;
            }else {
                result = true;
            }
        return result;
    }

    public static final int PICK_FILE= 10;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == RESULT_OK && data != null && data.getData() != null){
            filePath = data.getData();
            try {
                String path = filePath.getPath();
                File file = new File(path);
                namefile = file.getName();
                txt_filepath.setText(namefile);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private View.OnClickListener filePicker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_FILE);
        }
    };

    private void DialogStatus() {
        statusDialog = new Dialog(this);
        statusDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        statusDialog.setContentView(R.layout.dialogstatustoko);
        statusDialog.setCancelable(true);
    }

    public void onBackPressed(){
        //kembali ke home page
        Intent back = new Intent(CetakinPage.this, UserPage.class);
        startActivity(back);
        finish();
    }
}
