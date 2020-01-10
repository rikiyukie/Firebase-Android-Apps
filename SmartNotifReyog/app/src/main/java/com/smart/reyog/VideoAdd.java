package com.smart.reyog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.smart.reyog.Model.Video;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class VideoAdd extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText edtVideo;
    private Button addVideo;
    private Query mQueryCurrent;
    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Video, VideoAdd.EntryViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_add);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("VIDEO");
        mDatabase.keepSynced(true);
        mQueryCurrent = mDatabase.orderByChild("uploadDate");

        edtVideo = (EditText)findViewById(R.id.editVideo);
        addVideo = (Button)findViewById(R.id.btnAddVideo);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_admin_video);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        addVideo.setOnClickListener(addVideoFb);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Video> options = new FirebaseRecyclerOptions.Builder<Video>()
                .setQuery(mQueryCurrent, Video.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Video, EntryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull EntryViewHolder entryViewHolder, int i, @NonNull Video video) {

                entryViewHolder.setVideo(video.getUrlVideo());

            }

            @NonNull
            @Override
            public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_video_card, parent, false);
                return new EntryViewHolder(view);
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public class EntryViewHolder extends RecyclerView.ViewHolder {
        View mView;
        WebView videoView;
        String video_url;
        //TextView e_title;

        public EntryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            ImageView imgDelete = (ImageView)mView.findViewById(R.id.delete_video);

            imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String videoFUrl = video_url;
                    Context context = mView.getContext();
                    new AlertDialog.Builder(context)
                            .setTitle("Delete")
                            .setMessage("Apakah anda yakin akan menghapus video ini?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    mDatabase.orderByChild("urlVideo").equalTo(videoFUrl).addListenerForSingleValueEvent(new ValueEventListener() {
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

        public void setVideo (final String videoUrl){
            videoView = (WebView) mView.findViewById(R.id.videoWebViewAdmin);
            videoView.setWebChromeClient(new WebChromeClient());
            WebSettings webSettings = videoView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            videoView.setInitialScale(0);
            videoView.loadData(videoUrl, "text/html", "utf-8");

            video_url = videoUrl;
        }

    }


    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    //--
    private View.OnClickListener addVideoFb = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String video = edtVideo.getText().toString();

            if (!validateForm(video)){
                return;
            }

            String urlVideo = "<iframe width=\"100%\" height=\"100%\" src=\"https://www.youtube.com/embed/"+video+"\" frameborder=\"0\" allow=\"accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>";

            String uuid = UUID.randomUUID().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
            String currentDateandTime = sdf.format(new Date());

            Video add = new Video(urlVideo, currentDateandTime);
            mDatabase.child(uuid).setValue(add).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(VideoAdd.this, "Video Berhasil Ditambahkan", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }else {
                        Toast.makeText(VideoAdd.this, "Gagal Menambahkan Video", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
    };

    private boolean validateForm(String video) {
        boolean result = true;

        if (TextUtils.isEmpty(video)){
            edtVideo.setError("harus diisi");
            result = false;
        }else {
            edtVideo.setError(null);
        }

        return result;
    }

    public void onBackPressed(){
        //kembali ke home page
        Intent back = new Intent(VideoAdd.this, Admin_Page.class);
        startActivity(back);
        finish();
    }
}
