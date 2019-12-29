package com.cetakin.anterin;


import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cetakin.anterin.Model.HistoriOrder;
import com.cetakin.anterin.Model.Order;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Locale;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class Tab1s extends Fragment {

    private static final int PER_STORAGE_CODE = 100;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, mOrder;
    private Query mQuery;
    private FirebaseStorage storage;
    private FirebaseRecyclerAdapter<Order, Tab1s.EntryViewHolder> firebaseRecyclerAdapter;
    private String getUrl;
    private Dialog mViewDialog;

    public Tab1s() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tab1s, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mOrder = mDatabase.child("Order");
        mQuery = mOrder.orderByChild("status");
        mDatabase.keepSynced(true);
        storage = FirebaseStorage.getInstance();

        recyclerView = (RecyclerView)rootView.findViewById(R.id.RecyclerView1s);
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
                .setQuery(mQuery.equalTo("diantar"), Order.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Order, EntryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final EntryViewHolder entryViewHolder, int i, @NonNull final Order order) {

                String orderEmail = order.getEmail();
                mDatabase.child("Users").orderByChild("Email").equalTo(orderEmail).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot data:dataSnapshot.getChildren()){
                            String nama = data.child("Nama").getValue().toString();
                            entryViewHolder.setNamaUser(nama);
                            entryViewHolder.setUserNama(nama);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                String getFileUrl = order.getFileUrl();
                StorageReference refFile = storage.getReferenceFromUrl(getFileUrl);
                String namaFile = refFile.getName();

                entryViewHolder.setAlamat(order.getAlamat());
                entryViewHolder.setUrlFile(order.getFileUrl());
                entryViewHolder.setStatusOrder(order.getStatus());
                entryViewHolder.setKet(order.getKeterangan());
                entryViewHolder.setNoHP(order.getNoHp());
                entryViewHolder.setNamaFile(namaFile);
                entryViewHolder.setEmail(order.getEmail());
                entryViewHolder.setOrderDate(order.getOrderDate());
                entryViewHolder.setAlamatUsr(order.getAlamat());

            }

            @NonNull
            @Override
            public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab1s_card, parent, false);
                return new EntryViewHolder(view);
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public class EntryViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView e_alamat, e_namauser;
        String fileUrl, status, keterangan, nohp, namauser, namafile, email, orderdate, alamat;

        public EntryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mViewCustomDialog();

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewDialog.show();
                    TextView dialognama = (TextView)mViewDialog.findViewById(R.id.dialog1s_nama);
                    TextView dialognohp = (TextView)mViewDialog.findViewById(R.id.dialog1s_nohp);
                    TextView dialogfile = (TextView)mViewDialog.findViewById(R.id.dialog1s_file);
                    TextView dialogket = (TextView)mViewDialog.findViewById(R.id.dialog1s_keterangan);

                    dialogket.setText(keterangan);
                    dialognama.setText(namauser);
                    dialognohp.setText(nohp);
                    dialogfile.setText(namafile);

                    Button dialogditerima = (Button)mViewDialog.findViewById(R.id.dialog_btnditerima);
                    dialogditerima.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                            String currentDateandTime = sdf.format(new Date());

                            String uuid = UUID.randomUUID().toString();
                            HistoriOrder histori = new HistoriOrder(
                                    namauser,
                                    email,
                                    nohp,
                                    namafile,
                                    keterangan,
                                    orderdate,
                                    currentDateandTime,
                                    "Belum Dikonfirmasi",
                                    "waiting", alamat);
                            DatabaseReference ref = mDatabase.child("HistoriOrder").child("histori"+currentDateandTime);
                            ref.setValue(histori);

                            storage.getReference().getStorage().getReferenceFromUrl(fileUrl).delete();
                            mOrder.orderByChild("fileUrl").equalTo(fileUrl).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                   for (DataSnapshot data:dataSnapshot.getChildren()){
                                       data.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                           @Override
                                           public void onSuccess(Void aVoid) {
                                               mViewDialog.dismiss();
                                           }
                                       });
                                   }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                }
            });
        }

        public void setAlamat (String alamat){
            e_alamat = (TextView)mView.findViewById(R.id.alamat1s);
            e_alamat.setText(alamat);
        }

        public void setNamaUser (String namaUser){
            e_namauser = (TextView)mView.findViewById(R.id.nama1s);
            e_namauser.setText(namaUser);
        }

        public void  setNoHP (String noHP){
            nohp = noHP;
        }

        public void setKet (String ket){
            keterangan = ket;
        }

        public void setUrlFile (String urlFile){
            fileUrl = urlFile;
        }

        public void setUserNama (String userNama){
            namauser = userNama;
        }

        public void setStatusOrder (String statusOrder){
            status = statusOrder;
        }

        public void setNamaFile (String NamaFile){
            namafile = NamaFile;
        }

        public void setEmail (String emailUser){
            email = emailUser;
        }

        public void setOrderDate (String order){
            orderdate = order;
        }

        public void setAlamatUsr (String alamatusr){
            alamat = alamatusr;
        }

    }

    private void mViewCustomDialog() {
        mViewDialog = new Dialog(getActivity());
        mViewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mViewDialog.setContentView(R.layout.dialog1s);
        mViewDialog.setCancelable(true);
    }

}
