package com.cetakin.anterin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

public class CekPesanan extends AppCompatActivity {

    private RecyclerView list_pesanan;
    private DatabaseReference mDatabase, mOrder;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private Query mQuery;
    private FirebaseRecyclerAdapter<Order, CekPesanan.EntryViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cek_pesanan);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mOrder = mDatabase.child("Order");
        mDatabase.keepSynced(true);
        mQuery = mOrder.orderByChild("email");
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        list_pesanan = (RecyclerView)findViewById(R.id.recycler_cekpesanan);
        list_pesanan.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //layoutManager.setReverseLayout(true);
        //layoutManager.setStackFromEnd(true);
        list_pesanan.setLayoutManager(layoutManager);
    }

    public void onStart() {
        super.onStart();

        String emailuser = mAuth.getCurrentUser().getEmail();
        FirebaseRecyclerOptions<Order> options = new FirebaseRecyclerOptions.Builder<Order>()
                .setQuery(mQuery.equalTo(emailuser), Order.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Order, EntryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final EntryViewHolder entryViewHolder, int i, @NonNull final Order order) {

                //String getEmail = mAuth.getCurrentUser().getEmail();
                String getOrderDate = order.getOrderDate();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = null;
                try {
                    date = dateFormat.parse(getOrderDate);
                }catch (ParseException e){
                    e.printStackTrace();
                }
                SimpleDateFormat newdateFormat = new SimpleDateFormat("dd MMM yyyy");
                String finalformat = newdateFormat.format(date);

                String getFileUrl = order.getFileUrl();
                StorageReference refFile = storage.getReferenceFromUrl(getFileUrl);
                String namaFile = refFile.getName();

                String getNoHp = order.getNoHp();

                entryViewHolder.setOrderDate(finalformat);
                entryViewHolder.setNoHp(getNoHp);
                entryViewHolder.setFileName(namaFile);
                entryViewHolder.setAlamat(order.getAlamat());
                entryViewHolder.setKeterangan(order.getKeterangan());
                entryViewHolder.setStataus(order.getStatus());

                if (order.getStatus().equalsIgnoreCase("antri")){
                    TextView status = entryViewHolder.mView.findViewById(R.id.cek_status);
                    status.setTextColor(getResources().getColor(R.color.antri));
                }else if (order.getStatus().equalsIgnoreCase("sedang dikerjakan")){
                    TextView status = entryViewHolder.mView.findViewById(R.id.cek_status);
                    status.setTextColor(getResources().getColor(R.color.dikerjakan));
                }else if (order.getStatus().equalsIgnoreCase("selesai")){
                    TextView status = entryViewHolder.mView.findViewById(R.id.cek_status);
                    status.setTextColor(getResources().getColor(R.color.selesai));
                }else if (order.getStatus().equalsIgnoreCase("diantar")){
                    TextView status = entryViewHolder.mView.findViewById(R.id.cek_status);
                    status.setTextColor(getResources().getColor(R.color.diantar));
                }


            }

            @NonNull
            @Override
            public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cek_card, parent, false);
                return new EntryViewHolder(view);
            }
        };

        list_pesanan.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    //viewholder
    public class EntryViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView e_orderdate, e_nohp, e_filename, e_alamat, e_keterangan, e_status;

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

        public void setOrderDate (String orderDate){
            e_orderdate = (TextView)mView.findViewById(R.id.cek_tanggal);
            e_orderdate.setText(orderDate);
        }

        public void setNoHp (String noHp){
            e_nohp = (TextView)mView.findViewById(R.id.cek_nohp);
            e_nohp.setText(noHp);
        }

        public void setFileName (String fileName){
            e_filename = (TextView)mView.findViewById(R.id.cek_file);
            e_filename.setText(fileName);
        }

        public void setAlamat (String alamat){
            e_alamat = (TextView)mView.findViewById(R.id.cek_alamat);
            e_alamat.setText(alamat);
        }

        public void setKeterangan (String keterangan){
            e_keterangan = (TextView)mView.findViewById(R.id.cek_keterangan);
            e_keterangan.setText(keterangan);
        }

        public void setStataus (String stataus){
            e_status = (TextView)mView.findViewById(R.id.cek_status);
            e_status.setText(stataus);
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public void onBackPressed(){
        //kembali ke home page
        Intent back = new Intent(CekPesanan.this, UserPage.class);
        startActivity(back);
        finish();
    }
}
