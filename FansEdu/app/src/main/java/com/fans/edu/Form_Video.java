package com.fans.edu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fans.edu.Model.Video;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class Form_Video extends AppCompatActivity {

    private EditText edtVideo;
    private Button addVideo;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form__video);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("VIDEO");

        edtVideo = (EditText)findViewById(R.id.editVideo);
        addVideo = (Button)findViewById(R.id.btnAddVideo);

        addVideo.setOnClickListener(addVideoFb);
    }

    private View.OnClickListener addVideoFb = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String video = edtVideo.getText().toString();
            String urlVideo = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/"+video+"\" frameborder=\"0\" allow=\"accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>";

            Video add = new Video(urlVideo);
            String uuid = UUID.randomUUID().toString();
            mDatabase.child(uuid).setValue(add).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(Form_Video.this, "Success", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            });
        }
    };

    public void onBackPressed(){
        //kembali ke home page
        Intent back = new Intent(Form_Video.this, Halaman_Admin.class);
        startActivity(back);
        finish();
    }
}
