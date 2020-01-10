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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register_Page extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private EditText editNama, editEmail, editPass, editUsername;
    private Button btnRegister;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page_layout);

        //firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        //deskripsi sesuai id
        editNama = (EditText)findViewById(R.id.reg_nama);
        editUsername = (EditText)findViewById(R.id.reg_username);
        editEmail = (EditText)findViewById(R.id.reg_email);
        editPass = (EditText)findViewById(R.id.reg_password);
        btnRegister = (Button)findViewById(R.id.btn_register);
        progressBar = (ProgressBar)findViewById(R.id.progressBar_register);
        progressBar.setVisibility(View.GONE);

        //onClick
        btnRegister.setOnClickListener(register);
    }

    private View.OnClickListener register = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //get text
            final String nama = editNama.getText().toString();
            final String email = editEmail.getText().toString();
            final String username = editUsername.getText().toString();
            String password = editPass.getText().toString();

            //memastikan form tidak kosong
            if (!validateForm(nama,email,password)){
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            //registrasi user baru
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        //input data user baru ke database
                        String userUid = task.getResult().getUser().getUid().toString();
                        DatabaseReference newUser = mDatabase.child("Users").child(userUid);
                        newUser.child("Nama").setValue(nama);
                        newUser.child("Username").setValue(username);
                        newUser.child("Email").setValue(email);
                        newUser.child("Userphoto").setValue("unknown");
                        Toast.makeText(Register_Page.this, "Registrasi Berhasil", Toast.LENGTH_SHORT).show();

                        mAuth.signOut();
                        //kembali ke login page
                        Intent lp = new Intent(Register_Page.this, Login_Page.class);
                        startActivity(lp);
                        progressBar.setVisibility(View.GONE);
                        finish();
                    }else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Register_Page.this, "Registrasi Gagal", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };

    private boolean validateForm(String nama, String email, String password){
        boolean result = true;
        if (TextUtils.isEmpty(nama)){
            editNama.setError("Harus Diisi");
            result = false;
        }else {
            editNama.setError(null);
        }

        if (TextUtils.isEmpty(email)){
            editEmail.setError("Harus Diisi");
            result = false;
        }else {
            editEmail.setError(null);
        }

        if (TextUtils.isEmpty(password)){
            editPass.setError("Harus Diisi");
            result = false;
        }else if (password.trim().length() < 8){
            editPass.setError("Minimal 8 Digit");
            result = false;
        }else {
            editPass.setError(null);
        }

        return result;
    }

    public void onBackPressed(){
        //kembali ke home page
        Intent back = new Intent(Register_Page.this, Login_Page.class);
        startActivity(back);
        finish();
    }
}
