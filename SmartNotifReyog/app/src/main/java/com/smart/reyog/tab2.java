package com.smart.reyog;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.smart.reyog.Model.Notification;
import com.smart.reyog.Model.Pentas;
import com.smart.reyog.Model.Respon;
import com.smart.reyog.Model.Sender;
import com.smart.reyog.Retrofit.APIService;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class tab2 extends Fragment {

    private RecyclerView recyclerView;
    private DatabaseReference mDatabase;
    private Query mQueryCurrent;
    private FirebaseRecyclerAdapter<Pentas, tab2.EntryViewHolder> firebaseRecyclerAdapter;

    public tab2() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tab2, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("PENTAS");
        mDatabase.keepSynced(true);
        mQueryCurrent = mDatabase.orderByChild("NamaPentas");

        recyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view_02);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Pentas> options = new FirebaseRecyclerOptions.Builder<Pentas>()
                .setQuery(mDatabase, Pentas.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Pentas, EntryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull EntryViewHolder entryViewHolder, int i, @NonNull Pentas pentas) {

                entryViewHolder.setTitle(pentas.getNamaPentas());
                entryViewHolder.setImg(pentas.getImgUrl());

                //copyValue
                entryViewHolder.setLokasi(pentas.getLokasiPentas());
                entryViewHolder.setJadwal(pentas.getJadwalPentas());
                entryViewHolder.setInfo(pentas.getInfoPentas());
                entryViewHolder.setBannerUrl(pentas.getImgUrl());

            }

            @NonNull
            @Override
            public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.info_card, parent, false);
                return new EntryViewHolder(view);
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    //viewholder
    public class EntryViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView e_title;
        ImageView e_banner;
        String e_lokasi, e_jadwal, e_info, e_bannerUrl;

        public EntryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String judul = String.valueOf(e_title.getText());

                    infoPage(judul, e_lokasi, e_jadwal, e_info, e_bannerUrl);
                    //Toast.makeText(getActivity(), "oke click "+judul, Toast.LENGTH_SHORT).show();
                }
            });
        }
        public void setTitle(String title){
            e_title = (TextView) mView.findViewById(R.id.judul_card);
            e_title.setText(title);

        }
        public void setImg (String imgUrl){
            e_banner = (ImageView) mView.findViewById(R.id.banner_card);
            Picasso.with(getActivity()).load(imgUrl).into(e_banner);
        }

        public void setLokasi (String lokasi){
            e_lokasi = lokasi;
        }

        public void setJadwal (String jadwal){
            e_jadwal = jadwal;
        }

        public void setInfo (String info){
            e_info = info;
        }

        public void setBannerUrl (String bannerUrl){
            e_bannerUrl = bannerUrl;
        }

    }

    private void infoPage(
            String judul,
            String e_lokasi,
            String e_jadwal,
            String e_info,
            String e_bannerUrl)
    {
        Intent i = new Intent(getActivity(), InfoPentas.class);
        i.putExtra("iJudul", judul);
        i.putExtra("iLokasi", e_lokasi);
        i.putExtra("iJadwal", e_jadwal);
        i.putExtra("iInfo", e_info);
        i.putExtra("iBanner", e_bannerUrl);
        startActivity(i);
        //getActivity().finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }
}
