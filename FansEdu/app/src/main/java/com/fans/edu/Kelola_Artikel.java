package com.fans.edu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fans.edu.Model.Artikel;
import com.fans.edu.Model.Video;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class Kelola_Artikel extends AppCompatActivity {

    private ImageView addnew;
    private RecyclerView list_artikel;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseRecyclerAdapter<Artikel, Kelola_Artikel.EntryViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelola_artikel);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("ARTIKEL");
        mDatabase.keepSynced(true);

        list_artikel = (RecyclerView)findViewById(R.id.artikel_admin_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        list_artikel.setLayoutManager(layoutManager);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        addnew = (ImageView)findViewById(R.id.add_new_artikel);
        addnew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent add = new Intent(Kelola_Artikel.this, Form_Artikel.class);
                startActivity(add);
                finish();
            }
        });
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

                entryViewHolder.setJudul(artikel.getJudul());
                entryViewHolder.setGambar(artikel.getGambarUrl());
                entryViewHolder.setIsi(artikel.getIsi());

            }

            @NonNull
            @Override
            public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kelola_artikel_card, parent, false);
                return new EntryViewHolder(view);
            }
        };

        list_artikel.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public class EntryViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView vJudul;
        ImageView vGambar;
        String sJudul, sGambar, sIsi, sUpdate;

        public EntryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent edt = new Intent(Kelola_Artikel.this, Form_Artikel_Edit.class);
                    edt.putExtra("iJudul", sJudul);
                    edt.putExtra("iIsi", sIsi);
                    edt.putExtra("iBanner", sGambar);
                    startActivity(edt);
                    finish();
                }
            });

            ImageView btnDelete = (ImageView)mView.findViewById(R.id.delete_kelola);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(Kelola_Artikel.this)
                            .setTitle("Delete")
                            .setMessage("Apakah anda yakin akan menghapus data ini?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    storageReference.getStorage().getReferenceFromUrl(sGambar).delete();
                                    mDatabase.orderByChild("judul").equalTo(sJudul).addListenerForSingleValueEvent(new ValueEventListener() {
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

        public void setJudul (String judul){
            sJudul = judul;
            vJudul = (TextView)mView.findViewById(R.id.judul_kelola);
            vJudul.setText(judul);
        }

        public void setGambar (String urlgambar){
            sGambar = urlgambar;
            vGambar = (ImageView)mView.findViewById(R.id.banner_kelola);
            Picasso.with(Kelola_Artikel.this).load(urlgambar).into(vGambar);
        }

        public void setIsi (String isi){
            sIsi = isi;
        }

        public void setUpdate (String update){
            sUpdate = update;
        }

    }

    public void onBackPressed(){
        //kembali ke home page
        Intent back = new Intent(Kelola_Artikel.this, Halaman_Admin.class);
        startActivity(back);
        finish();
    }
}
