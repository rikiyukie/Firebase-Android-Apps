package com.cetakin.anterin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class UserPage extends AppCompatActivity {

    private Button btnCetak, btnCek, btnLogout, btnHistori;
    private TextView txtToko, txtSub, txtNews;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Dialog statusDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        txtToko = (TextView) findViewById(R.id.txtTokoTitle);
        txtSub = (TextView) findViewById(R.id.txtTokoSubTitle);
        txtNews = (TextView) findViewById(R.id.txt_userNews);
        btnCetak = (Button) findViewById(R.id.btn_Cetakin);
        btnCek = (Button) findViewById(R.id.btn_CekStatus);
        btnLogout = (Button) findViewById(R.id.logout_user);
        btnHistori = (Button)findViewById(R.id.btn_historiorder);

        getStatusToko();

        btnCek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cek = new Intent(UserPage.this, CekPesanan.class);
                startActivity(cek);
                finish();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent out = new Intent(UserPage.this, LoginPage.class);
                startActivity(out);
                finish();
            }
        });

        btnHistori.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent h = new Intent(UserPage.this, HistoriPesanan.class);
                startActivity(h);
                finish();
            }
        });

        DialogStatus();
        viewNews();
    }

    private void getStatusToko() {
        DatabaseReference refToko = mDatabase.child("ADMIN").child("MyToko");
        refToko.keepSynced(true);
        refToko.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status = dataSnapshot.child("Status").getValue().toString();
                if (status.equalsIgnoreCase("buka")){
                    txtToko.setText("Toko Buka");
                    txtSub.setText("Silahkan Buat Pesanan");
                    btnCetak.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent cetak = new Intent(UserPage.this, CetakinPage.class);
                            startActivity(cetak);
                            finish();
                        }
                    });
                }else {
                    txtToko.setText("Toko Tutup");
                    txtSub.setText("Mohon Maaf Toko Sedang Tutup");
                    btnCetak.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            statusDialog.show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DialogStatus() {
        statusDialog = new Dialog(this);
        statusDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        statusDialog.setContentView(R.layout.dialogstatustoko);
        statusDialog.setCancelable(true);
    }

    private void viewNews() {
        DatabaseReference ref = mDatabase.child("ADMIN").child("MyToko").child("News");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String pengumuman = dataSnapshot.getValue().toString();
                if (pengumuman.equalsIgnoreCase("kosong")){
                    txtNews.setVisibility(View.GONE);
                }else {
                    txtNews.setVisibility(View.VISIBLE);
                    txtNews.setText(pengumuman);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
