package com.fans.edu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login_Page extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private EditText editEmail, editPass;
    private Button btnLogin;
    private TextView txtRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page_layout);

        //firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        //deskripsi sesuai id
        editEmail = (EditText)findViewById(R.id.login_email);
        editPass = (EditText)findViewById(R.id.login_password);
        btnLogin = (Button)findViewById(R.id.btn_login);
        txtRegister = (TextView)findViewById(R.id.txt_register);
        progressBar = (ProgressBar)findViewById(R.id.progressBar_login);
        progressBar.setVisibility(View.GONE);

        //onClick
        btnLogin.setOnClickListener(login);
        txtRegister.setOnClickListener(register);
    }

    private View.OnClickListener login = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //get text
            String email = editEmail.getText().toString();
            String password = editPass.getText().toString();


            //memastikan form tidak kosong
            if (!validateForm(email, password)){
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            //login
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        String admin = mAuth.getCurrentUser().getEmail().toString();
                        //cek email adalah admin
                        mDatabase.child("ADMIN").orderByChild("email").equalTo(admin).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    //jika iya, login admin
                                    Intent a = new Intent(Login_Page.this, Halaman_Admin.class);
                                    startActivity(a);
                                    progressBar.setVisibility(View.GONE);
                                    finish();
                                    //Toast.makeText(Login_Page.this, "iki admin", Toast.LENGTH_SHORT).show();
                                }else {
                                    //jika tidak login user
                                    Intent u = new Intent(Login_Page.this, Home_Page.class);
                                    startActivity(u);
                                    progressBar.setVisibility(View.GONE);
                                    finish();
                                    //Toast.makeText(Login_Page.this, "koe sopo?", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Login_Page.this, "Login Gagal", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    };

    private boolean validateForm(String email, String password) {
        boolean result = true;
        if (TextUtils.isEmpty(email)){
            editEmail.setError("Harus Diisi");
            result = false;
        }else {
            editEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)){
            editPass.setError("Harus Diisi");
            result = false;
        }else if (password.trim().length()< 8){
            editPass.setError("Minimal 8 Digit");
            result = false;
        }else {
            editPass.setError(null);
        }

        return result;
    }

    private View.OnClickListener register = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent r = new Intent(Login_Page.this, Register_Page.class);
            startActivity(r);
            finish();
        }
    };
}
