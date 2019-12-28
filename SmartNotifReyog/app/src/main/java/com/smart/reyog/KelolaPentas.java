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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.smart.reyog.Model.Pentas;

public class KelolaPentas extends AppCompatActivity {

    private RecyclerView list_pentas;
    private static DatabaseReference mDatabase;
    private Query mQueryCurrent;
    private FirebaseRecyclerAdapter<Pentas, KelolaPentas.EntryViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelola_pentas);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("PENTAS");
        mDatabase.keepSynced(true);
        mQueryCurrent = mDatabase.orderByChild("uploadDate");

        list_pentas = (RecyclerView)findViewById(R.id.recycler_view_kelolapentas);
        list_pentas.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        list_pentas.setLayoutManager(layoutManager);
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Pentas> options = new FirebaseRecyclerOptions.Builder<Pentas>()
                .setQuery(mQueryCurrent, Pentas.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Pentas, EntryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull EntryViewHolder entryViewHolder, int i, @NonNull Pentas pentas) {
                entryViewHolder.setTitle(pentas.getNamaPentas());
                entryViewHolder.setContent(pentas.getLokasiPentas());
                entryViewHolder.setTime(pentas.getJadwalPentas());

                //copy data
                entryViewHolder.setDjudul(pentas.getNamaPentas());
                entryViewHolder.setDlokasi(pentas.getLokasiPentas());
                entryViewHolder.setDwaktu(pentas.getJadwalPentas());
                entryViewHolder.setDisi(pentas.getInfoPentas());
                entryViewHolder.setDbanner(pentas.getImgUrl());
            }

            @NonNull
            @Override
            public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kelola_pentas_card, parent, false);
                return new EntryViewHolder(view);
            }
        };

        list_pentas.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    //viewholder
    public class EntryViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView e_title;
        TextView e_content;
        TextView e_time;

        String d_title, d_lokasi, d_waktu, d_isi, d_bannerUrl;

        public EntryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            Button btnEdit = (Button)mView.findViewById(R.id.btnKelola_edit);
            Button btnDelete = (Button)mView.findViewById(R.id.btnKelola_delete);

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent edit = new Intent(KelolaPentas.this, EditPentas.class);
                    edit.putExtra("iJudul", d_title);
                    edit.putExtra("iLokasi", d_lokasi);
                    edit.putExtra("iJadwal", d_waktu);
                    edit.putExtra("iInfo", d_isi);
                    edit.putExtra("iBanner", d_bannerUrl);
                    startActivity(edit);
                    edit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    finish();
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String judul = e_title.getText().toString();
                    Context context = mView.getContext();
                    new AlertDialog.Builder(context)
                            .setTitle("Delete")
                            .setMessage("Apakah anda yakin akan menghapus data ini?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    mDatabase.orderByChild("namaPentas").equalTo(judul).addListenerForSingleValueEvent(new ValueEventListener() {
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
        public void setTitle(String title){
            e_title = (TextView) mView.findViewById(R.id.kelola_judul);
            e_title.setText(title);

        }

        public void setContent(String content){
            e_content = (TextView) mView.findViewById(R.id.kelola_lokasi);
            e_content.setText(content);

        }

        public void setTime (String time){
            e_time = (TextView) mView.findViewById(R.id.kelola_waktu);
            e_time.setText(time);
        }

        public void setDjudul(String dJudul){
            d_title = dJudul;
        }

        public void  setDwaktu(String dWaktu){
            d_waktu = dWaktu;
        }

        public void  setDlokasi(String dLokasi){
            d_lokasi = dLokasi;
        }

        public void  setDisi(String dIsi){
            d_isi = dIsi;
        }

        public void setDbanner (String dBanner){
            d_bannerUrl = dBanner;
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public void onBackPressed(){
        Intent back = new Intent(KelolaPentas.this, Admin_Page.class);
        startActivity(back);
        finish();
    }
}
