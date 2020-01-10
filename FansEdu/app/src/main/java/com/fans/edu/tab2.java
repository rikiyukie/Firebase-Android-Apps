package com.fans.edu;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.fans.edu.Model.Video;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Vector;


/**
 * A simple {@link Fragment} subclass.
 */
public class tab2 extends Fragment {

    private RecyclerView list_video;
    private DatabaseReference mDatabase;
    private Query mQueryCurrent;
    private FirebaseRecyclerAdapter<Video, tab2.EntryViewHolder> firebaseRecyclerAdapter;

    public tab2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tab2, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("VIDEO");
        mDatabase.keepSynced(true);

        list_video = (RecyclerView)rootView.findViewById(R.id.videoRecyclerView);
        list_video.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        list_video.setLayoutManager(layoutManager);

        return rootView;
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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_card, parent, false);
                return new EntryViewHolder(view);
            }
        };

        list_video.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public class EntryViewHolder extends RecyclerView.ViewHolder {
        View mView;
        WebView videoView;

        public EntryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

        }

        public void setVideo (String url){
            videoView = (WebView) mView.findViewById(R.id.videoWebView);
            videoView.setWebChromeClient(new WebChromeClient());
            WebSettings webSettings = videoView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            videoView.loadData(url, "text/html", "utf-8");
            //videoView.loadUrl(url);

        }

    }
}
