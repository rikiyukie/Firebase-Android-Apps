package com.smart.reyog;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;

public class Admin_Page extends AppCompatActivity {

    private Button btnLogout, btnVideo, btnAddPentas, btnKelola;
    private TextView admin;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_page_layout);

        mAuth = FirebaseAuth.getInstance();

        btnLogout = (Button)findViewById(R.id.btn_logout_admin);
        btnVideo = (Button)findViewById(R.id.video_btn);
        btnAddPentas = (Button)findViewById(R.id.add_pentas);
        btnKelola = (Button) findViewById(R.id.kelolaPentas);
        admin = (TextView) findViewById(R.id.adminEmail);

        btnLogout.setOnClickListener(logout);
        btnVideo.setOnClickListener(videoBtn);
        btnAddPentas.setOnClickListener(addPentas);
        btnKelola.setOnClickListener(kelolaPentas);

        String adminEmail = mAuth.getCurrentUser().getEmail();
        admin.setText(adminEmail);
    }

    private View.OnClickListener addPentas = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent add = new Intent(Admin_Page.this, Form_Pentas.class);
            startActivity(add);
            finish();
        }
    };

    private View.OnClickListener videoBtn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent v = new Intent(Admin_Page.this, VideoAdd.class);
            startActivity(v);
            finish();
        }
    };

    private View.OnClickListener kelolaPentas = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent k = new Intent(Admin_Page.this, KelolaPentas.class);
            startActivity(k);
            finish();
        }
    };

    private View.OnClickListener logout = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mAuth.signOut();
            Intent out = new Intent(Admin_Page.this, Login_Page.class);
            startActivity(out);
            finish();
        }
    };

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Apakah anda yakin mau keluar?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create().show();
    }
}
