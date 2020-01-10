package com.smart.reyog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

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

        FirebaseMessaging.getInstance().subscribeToTopic("Pentas");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("NotifPentas", "NotifPentas",
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

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
                                Intent a = new Intent(MainActivity.this, Admin_Page.class);
                                startActivity(a);
                                finish();
                            }else {

                                //login sebagai user
                                Intent home = new Intent(MainActivity.this, Home_page.class);
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
