package com.smart.reyog;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.DocumentsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.smart.reyog.Model.Pentas;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class tab1 extends Fragment {

    private RecyclerView list_pentas;
    private static DatabaseReference mDatabase;
    private Query mQueryCurrent;
    private FirebaseRecyclerAdapter<Pentas, tab1.EntryViewHolder> firebaseRecyclerAdapter;

    public tab1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tab1,container,false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("PENTAS");
        mDatabase.keepSynced(true);
        mQueryCurrent = mDatabase.orderByChild("uploadDate");

        list_pentas = (RecyclerView)rootView.findViewById(R.id.recycler_view);
        list_pentas.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        list_pentas.setLayoutManager(layoutManager);

        return rootView;
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

                String updateDate = pentas.getUploadDate();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = null;
                try {
                    date = dateFormat.parse(updateDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat newdateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
                String finalFormat = newdateFormat.format(date);

                entryViewHolder.setUpdate(finalFormat);
                entryViewHolder.setTitle(pentas.getNamaPentas());
                entryViewHolder.setContent(pentas.getLokasiPentas());
                entryViewHolder.setTime(pentas.getJadwalPentas());
            }

            @NonNull
            @Override
            public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pentas_card, parent, false);
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
        TextView e_update;
        public EntryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getActivity(), "oke click (urung tak gawe)", Toast.LENGTH_SHORT).show();
                }
            });
        }
        public void setTitle(String title){
            e_title = (TextView) mView.findViewById(R.id.nama_pentas);
            e_title.setText(title);

        }

        public void setContent(String content){
            e_content = (TextView) mView.findViewById(R.id.lokasi_pentas);
            e_content.setText(content);

        }

        public void setTime (String time){
            e_time = (TextView) mView.findViewById(R.id.waktu_pentas);
            e_time.setText(time);
        }

        public void setUpdate (String update){
            e_update = (TextView) mView.findViewById(R.id.last_update);
            e_update.setText(update);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

}
