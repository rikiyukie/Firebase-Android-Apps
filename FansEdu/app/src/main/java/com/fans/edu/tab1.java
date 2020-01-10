package com.fans.edu;


import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.fans.edu.Model.Artikel;
import com.fans.edu.Model.Video;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class tab1 extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference mDatabase;
    private Query mQueryCurrent;
    private FirebaseRecyclerAdapter<Artikel, tab1.EntryViewHolder> firebaseRecyclerAdapter;

    public tab1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tab1, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("ARTIKEL");
        mDatabase.keepSynced(true);

        recyclerView = (RecyclerView)rootView.findViewById(R.id.artikelRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Artikel> options = new FirebaseRecyclerOptions.Builder<Artikel>()
                .setQuery(mDatabase, Artikel.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Artikel, EntryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull EntryViewHolder entryViewHolder, int i, @NonNull Artikel artikel) {

                //entryViewHolder.setVideo(artikel.getUrlVideo());
                entryViewHolder.setTitle(artikel.getJudul());
                entryViewHolder.setGambar(artikel.getGambarUrl());

                entryViewHolder.setIsi(artikel.getIsi());
                entryViewHolder.setImg(artikel.getGambarUrl());

            }

            @NonNull
            @Override
            public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.artikel_card, parent, false);
                return new EntryViewHolder(view);
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public class EntryViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView e_judul;
        ImageView e_gambar;
        String e_isi, e_img;

        public EntryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String judul = String.valueOf(e_judul.getText());
                    artikelPage(judul, e_img, e_isi);
                    //Toast.makeText(getActivity(), "oke click (urung tak gawe)", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void setGambar (String imgUrl){
            e_gambar = (ImageView) mView.findViewById(R.id.gambarArtikel);
            Picasso.with(getActivity()).load(imgUrl).into(e_gambar);
        }

        public void setTitle(String title){
            e_judul = (TextView) mView.findViewById(R.id.judulArtikel);
            e_judul.setText(title);
        }

        public void setIsi (String isiArtikel){
            e_isi = isiArtikel;
        }

        public void setImg (String img){
            e_img = img;
        }

    }

    private void artikelPage(String judul, String e_img, String e_isi) {
        Intent i = new Intent(getActivity(), Artikel_Page.class);
        i.putExtra("iJudul", judul);
        i.putExtra("iIsi", e_isi);
        i.putExtra("iImg", e_img);
        startActivity(i);
    }
}
