package com.cetakin.anterin;


import android.Manifest;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cetakin.anterin.Model.Order;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 */
public class Tab1 extends Fragment {

    private static final int PER_STORAGE_CODE = 100;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, mOrder;
    private Query mQuery;
    private FirebaseStorage storage;
    private FirebaseRecyclerAdapter<Order, Tab1.EntryViewHolder> firebaseRecyclerAdapter;
    private String getUrl;

    public Tab1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab1, container, false);
        // Inflate the layout for this fragment

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mOrder = mDatabase.child("Order");
        mQuery = mOrder.orderByChild("status");
        mDatabase.keepSynced(true);
        storage = FirebaseStorage.getInstance();

        recyclerView = (RecyclerView)rootView.findViewById(R.id.antrianRecyclerView);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //layoutManager.setReverseLayout(true);
        //layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Order> options = new FirebaseRecyclerOptions.Builder<Order>()
                .setQuery(mQuery.equalTo("antri"), Order.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Order, EntryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final EntryViewHolder entryViewHolder, int i, @NonNull final Order order) {

                String orderEmail = order.getEmail();
                mDatabase.child("Users").orderByChild("Email").equalTo(orderEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot data:dataSnapshot.getChildren()){
                            String nama = data.child("Nama").getValue().toString();
                            entryViewHolder.setNamaUser(nama);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                String getTanggal = order.getOrderDate();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = null;
                try {
                    date = dateFormat.parse(getTanggal);
                }catch (ParseException e){
                    e.printStackTrace();
                }
                SimpleDateFormat newdateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
                String finalformat = newdateFormat.format(date);

                String getFileUrl = order.getFileUrl();
                StorageReference refFile = storage.getReferenceFromUrl(getFileUrl);
                String namaFile = refFile.getName();

                entryViewHolder.setTanggal(finalformat);
                entryViewHolder.setFile(namaFile);
                entryViewHolder.setNoHp(order.getNoHp());
                entryViewHolder.setAlamat(order.getAlamat());
                entryViewHolder.setKeterangan(order.getKeterangan());
                entryViewHolder.setUrlFile(order.getFileUrl());

            }

            @NonNull
            @Override
            public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.orderan_antrian_card, parent, false);
                return new EntryViewHolder(view);
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public class EntryViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView e_tanggal, e_file, e_nohp, e_alamat, e_keterangan, e_namauser;
        String fileUrl;

        public EntryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            Button kerjakan = (Button)mView.findViewById(R.id.btnKerjakan);
            kerjakan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mOrder.orderByChild("fileUrl").equalTo(fileUrl).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot data:dataSnapshot.getChildren()){
                                data.getRef().child("status").setValue("sedang dikerjakan");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });

            TextView down_file = (TextView)mView.findViewById(R.id.download_file);
            down_file.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "Downloading File", Toast.LENGTH_SHORT).show();

                    getUrl = fileUrl;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                PackageManager.PERMISSION_DENIED){
                            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permissions, PER_STORAGE_CODE);
                        }else {
                            donload();
                        }
                    }else {
                        donload();
                    }
                }
            });

        }

        public void setTanggal (String tanggal){
            e_tanggal = (TextView)mView.findViewById(R.id.antrian_tanggal);
            e_tanggal.setText(tanggal);
        }

        public void setFile (String file){
            e_file = (TextView)mView.findViewById(R.id.antrian_file);
            e_file.setText(file);
        }

        public void setNoHp (String noHp){
            e_nohp = (TextView)mView.findViewById(R.id.antrian_nohp);
            e_nohp.setText(noHp);
        }

        public void setAlamat (String alamat){
            e_alamat = (TextView)mView.findViewById(R.id.antrian_alamat);
            e_alamat.setText(alamat);
        }

        public void setKeterangan (String keterangan){
            e_keterangan = (TextView)mView.findViewById(R.id.antrian_keterangan);
            e_keterangan.setText(keterangan);
        }

        public void setNamaUser (String namaUser){
            e_namauser = (TextView)mView.findViewById(R.id.antrian_nama_user);
            e_namauser.setText(namaUser);
        }

        public void setUrlFile (String urlFile){
            fileUrl = urlFile;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PER_STORAGE_CODE:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    donload();
                }else {
                    Toast.makeText(getActivity(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void donload() {
        //Toast.makeText(getActivity(), getUrl, Toast.LENGTH_SHORT).show();
        StorageReference ref = storage.getReferenceFromUrl(getUrl);
        String filename = ref.getName();

        Uri downloadUrl = Uri.parse(getUrl);
        DownloadManager manager = (DownloadManager)getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(downloadUrl);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setTitle(filename);//this description apears inthe android notification
        request.setDescription("downloading"); //this description apears inthe android notification

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename); //set destination
        request.setMimeType(getMimeType(downloadUrl));


        manager.enqueue(request);
    }

    private String getMimeType (Uri uri){
        ContentResolver resolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(resolver.getType(uri));
    }

}
