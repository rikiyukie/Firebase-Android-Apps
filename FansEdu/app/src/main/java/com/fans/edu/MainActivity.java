package com.fans.edu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private int loading_time=2000;
    //2000 = 2 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                //cek current user
                FirebaseUser currentUser = mAuth.getCurrentUser();

                if (currentUser == null) {
                    //user dereng login, buka login page
                    Intent login = new Intent(MainActivity.this, Login_Page.class);
                    startActivity(login);
                    finish();
                }else {
                    //user sampun login
                    String admin = currentUser.getEmail().toString();
                    mDatabase.child("ADMIN").orderByChild("email").equalTo(admin).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                //login sebagai admin
                                Intent a = new Intent(MainActivity.this, Halaman_Admin.class);
                                startActivity(a);
                                finish();
                            }else {
                                //login sebagai user
                                Intent home = new Intent(MainActivity.this, Home_Page.class);
                                startActivity(home);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
        }, loading_time);
    }
}
