package com.cetakin.anterin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoriPesanan extends AppCompatActivity {

    private DatabaseReference mDatabase, mHistori;
    private FirebaseAuth mAuth;
    private Query mQuery;
    private RecyclerView listHistori;
    private FirebaseRecyclerAdapter<HistoriOrder, HistoriPesanan.EntryViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histori_pesanan);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mHistori = mDatabase.child("HistoriOrder");
        mQuery = mHistori.orderByChild("email");
        mDatabase.keepSynced(true);

        listHistori = (RecyclerView)findViewById(R.id.recycler_historiuser);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        listHistori.setLayoutManager(layoutManager);

    }

    public void onStart() {
        super.onStart();

        String emailuser = mAuth.getCurrentUser().getEmail();
        FirebaseRecyclerOptions<HistoriOrder> options = new FirebaseRecyclerOptions.Builder<HistoriOrder>()
                .setQuery(mQuery.equalTo(emailuser), HistoriOrder.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<HistoriOrder, EntryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final EntryViewHolder entryViewHolder, int i, @NonNull final HistoriOrder historiOrder) {

                final String tglOrder = historiOrder.getOrderdate();

                if (historiOrder.getStatus().equalsIgnoreCase("waiting")){
                    entryViewHolder.setStatus("Pesanan diterima, menunggu konfirmasi");
                    TextView txtStatus = entryViewHolder.mView.findViewById(R.id.user_status);
                    txtStatus.setTextColor(getResources().getColor(R.color.diantar));

                    Button btnKonfirm = entryViewHolder.mView.findViewById(R.id.btn_konfirmasi);
                    btnKonfirm.setVisibility(View.VISIBLE);

                    /*btnKonfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DatabaseReference refStatus = mHistori.child("status");
                            refStatus.orderByChild("orderdate").equalTo(tglOrder).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot data:dataSnapshot.getChildren()){
                                        data.getRef().child("status").setValue("confirmed").addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                btnKonfirm.setVisibility(View.GONE);
                                                Toast.makeText(HistoriPesanan.this, "Pesanan Dikonfirmasi", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });*/
                }else {
                    Button btnKonfirm = entryViewHolder.mView.findViewById(R.id.btn_konfirmasi);
                    btnKonfirm.setVisibility(View.GONE);
                    entryViewHolder.setStatus("Pesanan diterima, telah dikonfirmasi");
                    TextView txtStatus = entryViewHolder.mView.findViewById(R.id.user_status);
                    txtStatus.setTextColor(getResources().getColor(R.color.selesai));
                }


                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = null;
                try {
                    date = dateFormat.parse(tglOrder);
                }catch (ParseException e){
                    e.printStackTrace();
                }
                SimpleDateFormat newdateFormat = new SimpleDateFormat("dd MMM yyyy");
                String finaltglOrder = newdateFormat.format(date);

                entryViewHolder.setTglOrder(finaltglOrder);
                entryViewHolder.setNamaFile(historiOrder.getNamafile());

                //copyValue
                entryViewHolder.setStglorder(historiOrder.getOrderdate());
                entryViewHolder.setStglterima(historiOrder.getDiterima());

            }

            @NonNull
            @Override
            public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.histori_user_card, parent, false);
                return new EntryViewHolder(view);
            }
        };

        listHistori.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    //viewholder
    public class EntryViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView namafile, status, tglorder;
        String snama, semail, snohp, snamafile, sketerangan, stglorder, stglterima, stglkonfirm, sstatus, salamat;

        public EntryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            Button konfirmasi = (Button)mView.findViewById(R.id.btn_konfirmasi);
            konfirmasi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new AlertDialog.Builder(HistoriPesanan.this)
                            .setTitle("Konfirmasi Pesanan")
                            .setMessage("Apakah pesanan sudah diterima?")
                            .setNegativeButton(android.R.string.no, null)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mHistori.orderByChild("diterima").equalTo(stglterima).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot data:dataSnapshot.getChildren()){
                                                data.getRef().child("status").setValue("confirmed").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(HistoriPesanan.this, "Pesanan Dikonfirmasi", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
                                                String currentDateandTime = sdf.format(new Date());
                                                data.getRef().child("dikonfirmasi").setValue(currentDateandTime);
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

        public void setNamaFile (String Namafile){
            namafile = (TextView)mView.findViewById(R.id.user_namafile);
            namafile.setText(Namafile);
        }

        public void setStatus (String setstatus){
            status = (TextView)mView.findViewById(R.id.user_status);
            status.setText(setstatus);
        }

        public void setTglOrder (String TglOrder){
            tglorder = (TextView)mView.findViewById(R.id.user_tglorder);
            tglorder.setText(TglOrder);
        }

        public void setSnama(String gnama) {
            snama = gnama;
        }

        public void setSemail(String gemail) {
            semail = gemail;
        }

        public void setSnohp(String gnohp) {
            snohp = gnohp;
        }

        public void setSnamafile(String gnamafile) {
            snamafile = gnamafile;
        }

        public void setSketerangan(String gketerangan) {
            sketerangan = gketerangan;
        }

        public void setStglorder(String gtglorder) {
            stglorder = gtglorder;
        }

        public void setStglterima(String gtglterima) {
            stglterima = gtglterima;
        }

        public void setStglkonfirm(String gtglkonfirm) {
            stglkonfirm = gtglkonfirm;
        }

        public void setSstatus(String gstatus) {
            sstatus = sstatus;
        }

        public void setSalamat(String galamat) {
            salamat = galamat;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public void onBackPressed(){
        //kembali ke home page
        Intent back = new Intent(HistoriPesanan.this, UserPage.class);
        startActivity(back);
        finish();
    }
}
