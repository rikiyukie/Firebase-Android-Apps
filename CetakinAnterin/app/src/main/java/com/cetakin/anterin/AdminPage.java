package com.cetakin.anterin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminPage extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private TextView statusToko, txtPengumuman;
    private EditText inputNews;
    private Switch statusSwitch;
    private Button btnOrderan,btnOrderanEnd,btnPengumuman, logout;
    private Dialog newsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_page);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        txtPengumuman = (TextView) findViewById(R.id.txt_Pengumuman);
        statusToko = (TextView)findViewById(R.id.status_toko);

        btnPengumuman = (Button)findViewById(R.id.btn_pengumuman);
        btnOrderan = (Button)findViewById(R.id.btn_orderan);
        btnOrderanEnd = (Button)findViewById(R.id.btn_selesai);
        logout = (Button)findViewById(R.id.logout_admin);
        statusSwitch = (Switch)findViewById(R.id.switch1);

        mDatabase.child("ADMIN").child("MyToko").child("News").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String getNews = dataSnapshot.getValue().toString();
                txtPengumuman.setText(getNews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnOrderanEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent os = new Intent(AdminPage.this, Admin_Orderan_Selesai.class);
                startActivity(os);
                finish();
            }
        });

        btnOrderan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent o = new Intent(AdminPage.this, Admin_Orderan.class);
                startActivity(o);
                finish();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent out = new Intent(AdminPage.this, LoginPage.class);
                startActivity(out);
                finish();
            }
        });

        btnPengumuman.setOnClickListener(viewDialogPengumuman);
        getStatusToko();
        createNewsDialog();
    }

    private void getStatusToko() {

        DatabaseReference refToko = mDatabase.child("ADMIN").child("MyToko");
        refToko.keepSynced(true);
        refToko.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String status = dataSnapshot.child("Status").getValue().toString();
                if (status.equalsIgnoreCase("buka")){
                    statusToko.setText("Buka");
                    statusSwitch.setChecked(true);
                }else {
                    statusToko.setText("Tutup");
                    statusSwitch.setChecked(false);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        changeStatus();
    }

    private void changeStatus() {
        statusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DatabaseReference refStatus = mDatabase.child("ADMIN").child("MyToko").child("Status");
                if (isChecked){
                    refStatus.setValue("buka");
                }else {
                    refStatus.setValue("tutup");
                }
            }
        });
    }

    private View.OnClickListener viewDialogPengumuman = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            newsDialog.show();

            inputNews = (EditText)newsDialog.findViewById(R.id.input_news);
            mDatabase.child("ADMIN").child("MyToko").child("News").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String CurrentNews = dataSnapshot.getValue().toString();

                    if (CurrentNews.equalsIgnoreCase("kosong")){
                        inputNews.setText(null);
                    }else {
                        inputNews.setText(CurrentNews);
                    }

                    clearNews();
                    saveNewsToDatabase();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    };

    private void clearNews() {
        Button clear = (Button)newsDialog.findViewById(R.id.btn_newsClear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputNews.setText(null);
            }
        });
    }

    private void saveNewsToDatabase() {

        Button save = (Button)newsDialog.findViewById(R.id.btn_newsSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtNews = inputNews.getText().toString();

                DatabaseReference ref = mDatabase.child("ADMIN").child("MyToko").child("News");
                if (TextUtils.isEmpty(txtNews)){
                    ref.setValue("kosong").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            newsDialog.dismiss();
                        }
                    });
                }else {
                    ref.setValue(txtNews).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            newsDialog.dismiss();
                            Toast.makeText(AdminPage.this, "Success", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

    }

    private void createNewsDialog() {
        newsDialog = new Dialog(this);
        newsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        newsDialog.setContentView(R.layout.dialog_news_admin);
        newsDialog.setCancelable(true);
    }
}
