package com.fans.edu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class Halaman_Admin extends AppCompatActivity {

    private Button btnLogout, btnArtikel, btnVideo, btnChat;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.halaman_admin_layout);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mAuth.getCurrentUser();

        btnLogout = (Button)findViewById(R.id.btn_logout_admin);
        btnArtikel = (Button)findViewById(R.id.btnArtikel);
        btnVideo = (Button)findViewById(R.id.btnVideo);
        btnChat = (Button)findViewById(R.id.btnChat);

        //onClick
        btnLogout.setOnClickListener(logout);
        btnArtikel.setOnClickListener(artikel);
        btnVideo.setOnClickListener(video);
        btnChat.setOnClickListener(chat);
    }

    private View.OnClickListener chat = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent a = new Intent(Halaman_Admin.this, ChatRoom_Admin.class);
            startActivity(a);
            finish();
        }
    };

    private View.OnClickListener artikel = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent a = new Intent(Halaman_Admin.this, Form_Artikel.class);
            startActivity(a);
            finish();
        }
    };

    private View.OnClickListener video = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent v = new Intent(Halaman_Admin.this, Form_Video.class);
            startActivity(v);
            finish();
        }
    };

    private View.OnClickListener logout = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mAuth.signOut();
            Intent out = new Intent(Halaman_Admin.this, Login_Page.class);
            startActivity(out);
            finish();
        }
    };
}
