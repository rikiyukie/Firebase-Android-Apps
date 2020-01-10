package com.fans.edu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fans.edu.Model.Video;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class Form_Video extends AppCompatActivity {

    private EditText edtVideo;
    private Button addVideo;
    private ImageView deleteVideo, addDialog;
    private DatabaseReference mDatabase;
    private RecyclerView list_video;
    private Dialog formvideo;
    private FirebaseRecyclerAdapter<Video, Form_Video.EntryViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form__video);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("VIDEO");

        list_video = (RecyclerView)findViewById(R.id.video_admin_recycler);
        list_video.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        list_video.setLayoutManager(layoutManager);

        addDialog = (ImageView)findViewById(R.id.add_dialog);
        addDialog.setOnClickListener(showCustomDialog);

        customDialog();
    }

    private View.OnClickListener showCustomDialog = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            formvideo.show();

            edtVideo = (EditText)formvideo.findViewById(R.id.input_url_dialog);
            addVideo = (Button)formvideo.findViewById(R.id.add_video_dialog);

            addVideo.setOnClickListener(addVideoFb);
        }
    };

    private void customDialog() {
        formvideo = new Dialog(Form_Video.this);
        formvideo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        formvideo.setContentView(R.layout.video_dialog);
        formvideo.setCancelable(true);
        formvideo.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Video> options = new FirebaseRecyclerOptions.Builder<Video>()
                .setQuery(mDatabase, Video.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Video, EntryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull EntryViewHolder entryViewHolder, int i, @NonNull Video video) {

                entryViewHolder.setVideo(video.getUrlVideo());

            }

            @NonNull
            @Override
            public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_card_admin, parent, false);
                return new EntryViewHolder(view);
            }
        };

        list_video.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public class EntryViewHolder extends RecyclerView.ViewHolder {
        View mView;
        WebView videoView;
        String videoUrl;

        public EntryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            deleteVideo = mView.findViewById(R.id.video_delete);
            deleteVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    new AlertDialog.Builder(Form_Video.this)
                            .setTitle("Delete")
                            .setMessage("Apakah anda yakin akan menghapus video ini?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    mDatabase.orderByChild("urlVideo").equalTo(videoUrl).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot data: dataSnapshot.getChildren()){
                                                data.getRef().removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }).create().show();

                }
            });

        }


        public void setVideo (String url){
            videoView = (WebView) mView.findViewById(R.id.videoWebView);
            videoView.setWebChromeClient(new WebChromeClient());
            WebSettings webSettings = videoView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            videoView.loadData(url, "text/html", "utf-8");
            //videoView.loadUrl(url);
            videoUrl = url;

        }

    }

    private View.OnClickListener addVideoFb = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String video = edtVideo.getText().toString();
            String urlVideo = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/"+video+"\" frameborder=\"0\" allow=\"accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>";

            if (TextUtils.isEmpty(video)){
                edtVideo.setError("null");
            }else {
                edtVideo.setError(null);
                Video add = new Video(urlVideo);
                String uuid = UUID.randomUUID().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                String currentDateandTime = sdf.format(new Date());
                mDatabase.child("video"+currentDateandTime).setValue(add).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        formvideo.dismiss();
                        Toast.makeText(Form_Video.this, "Success", Toast.LENGTH_SHORT).show();

                    }
                });
            }

        }
    };

    public void onBackPressed(){
        //kembali ke home page
        Intent back = new Intent(Form_Video.this, Halaman_Admin.class);
        startActivity(back);
        finish();
    }
}
