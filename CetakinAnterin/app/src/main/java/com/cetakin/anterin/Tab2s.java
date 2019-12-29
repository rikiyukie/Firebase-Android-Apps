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
import android.widget.TextView;

import com.cetakin.anterin.Model.HistoriOrder;
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
public class Tab2s extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, mOrder;
    private Query mQuery;
    private FirebaseStorage storage;
    private FirebaseRecyclerAdapter<HistoriOrder, Tab2s.EntryViewHolder> firebaseRecyclerAdapter;
    private String getUrl;
    private Dialog mViewDialog;

    public Tab2s() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tab2s, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mOrder = mDatabase.child("HistoriOrder");
        mQuery = mOrder.orderByChild("orderdate");
        mDatabase.keepSynced(true);
        storage = FirebaseStorage.getInstance();

        recyclerView = (RecyclerView)rootView.findViewById(R.id.RecyclerView2s);
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

        FirebaseRecyclerOptions<HistoriOrder> options = new FirebaseRecyclerOptions.Builder<HistoriOrder>()
                .setQuery(mQuery, HistoriOrder.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<HistoriOrder, EntryViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final EntryViewHolder entryViewHolder, int i, @NonNull final HistoriOrder historiOrder) {

                if (historiOrder.getStatus().equalsIgnoreCase("waiting")){
                    TextView text = entryViewHolder.mView.findViewById(R.id.status2s);
                    text.setTextColor(getResources().getColor(R.color.diantar));
                    entryViewHolder.setStatus("Pesanan diterima, menunggu konfirmasi");
                    entryViewHolder.getStatus("Pesanan diterima, menunggu konfirmasi");
                }else {
                    TextView text = entryViewHolder.mView.findViewById(R.id.status2s);
                    text.setTextColor(getResources().getColor(R.color.selesai));
                    entryViewHolder.setStatus("Pesanan diterima, telah dikonfirmasi");
                    entryViewHolder.getStatus("Pesanan diterima, menunggu konfirmasi");
                }

                String getTanggal = historiOrder.getOrderdate();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date date = null;
                try {
                    date = dateFormat.parse(getTanggal);
                }catch (ParseException e){
                    e.printStackTrace();
                }
                SimpleDateFormat newdateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
                String finalformat = newdateFormat.format(date);

                entryViewHolder.setNamaUser(historiOrder.getNama());
                entryViewHolder.setNamaFile(historiOrder.getNamafile());
                entryViewHolder.setOrderDate(finalformat);

                //getValueToString
                String tglDiterima = historiOrder.getDiterima();
                SimpleDateFormat diterimaFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date datediterima = null;
                try {
                    datediterima = diterimaFormat.parse(tglDiterima);
                }catch (ParseException e){
                    e.printStackTrace();
                }
                SimpleDateFormat newditerimaFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
                String finalditerimaformat = newditerimaFormat.format(datediterima);

                entryViewHolder.getNama(historiOrder.getNama());
                entryViewHolder.getNohp(historiOrder.getNohp());
                entryViewHolder.getAlamat(historiOrder.getAlamat());
                entryViewHolder.getNamaFile(historiOrder.getNamafile());
                entryViewHolder.getTglOrder(finalformat);
                entryViewHolder.getTglditerima(finalditerimaformat);
                entryViewHolder.getTglkonfirm(historiOrder.getDikonfirmasi());
                entryViewHolder.getKeterangan(historiOrder.getKeterangan());

            }

            @NonNull
            @Override
            public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tab2s_card, parent, false);
                return new EntryViewHolder(view);
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public class EntryViewHolder extends RecyclerView.ViewHolder {
        View mView;
        TextView namauser, namafile, orderdate, status;
        String snama, snohp, salamat, snamafile, sstatus, stglorder, stglditerima, stglkonfirmasi, sketerangan;

        public EntryViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mViewCustomDialog();

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewDialog.show();

                    TextView nama = (TextView)mViewDialog.findViewById(R.id.nama2s);
                    TextView nohp = (TextView)mViewDialog.findViewById(R.id.nohp2s);
                    TextView alamat = (TextView)mViewDialog.findViewById(R.id.alamat2s);
                    TextView namafile = (TextView)mViewDialog.findViewById(R.id.namafile2s);
                    TextView status = (TextView)mViewDialog.findViewById(R.id.status2s);
                    TextView tglorder = (TextView)mViewDialog.findViewById(R.id.tglorder2s);
                    TextView tglditerima = (TextView)mViewDialog.findViewById(R.id.tglditerima2s);
                    TextView tglkonfirmasi = (TextView)mViewDialog.findViewById(R.id.tglkonfirmasi2s);
                    TextView keterangan = (TextView)mViewDialog.findViewById(R.id.keterangan2s);

                    nama.setText(snama);
                    nohp.setText(snohp);
                    alamat.setText(salamat);
                    namafile.setText(snamafile);
                    status.setText(sstatus);
                    tglorder.setText(stglorder);
                    tglditerima.setText(stglditerima);
                    keterangan.setText(sketerangan);

                    if (stglkonfirmasi.equalsIgnoreCase("Belum Dikonfirmasi")){
                        tglkonfirmasi.setText(stglkonfirmasi);
                    }else {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                        Date date = null;
                        try {
                            date = dateFormat.parse(stglkonfirmasi);
                        }catch (ParseException e){
                            e.printStackTrace();
                        }
                        SimpleDateFormat newdateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
                        String finalformat = newdateFormat.format(date);
                        tglkonfirmasi.setText(finalformat);
                    }

                }
            });

        }


        public void setNamaUser (String nama){
            namauser = (TextView)mView.findViewById(R.id.nama2s);
            namauser.setText(nama);
        }

        public void setNamaFile (String file){
            namafile = (TextView)mView.findViewById(R.id.namafile2s);
            namafile.setText(file);
        }

        public void setOrderDate (String date){
            orderdate = (TextView)mView.findViewById(R.id.tglorder2s);
            orderdate.setText(date);
        }

        public void setStatus (String statusfinal){
            status = (TextView)mView.findViewById(R.id.status2s);
            status.setText(statusfinal);
        }

        public void getNama (String gNama){
            snama = gNama;
        }

        public void getNohp (String gNohp){
            snohp = gNohp;
        }

        public void getAlamat (String gAlamat){
            salamat = gAlamat;
        }

        public void getNamaFile (String gNamafile){
            snamafile = gNamafile;
        }

        public void getStatus (String gStatus){
            sstatus = gStatus;
        }

        public void getTglOrder (String gTglOrder){
            stglorder = gTglOrder;
        }

        public void getTglditerima (String gTglditerima){
            stglditerima = gTglditerima;
        }

        public void getTglkonfirm (String gTglkonfirm){
            stglkonfirmasi = gTglkonfirm;
        }

        public void getKeterangan (String gKeterangan){
            sketerangan = gKeterangan;
        }
    }

    private void mViewCustomDialog() {
        mViewDialog = new Dialog(getActivity());
        mViewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mViewDialog.setContentView(R.layout.dialog2s);
        mViewDialog.setCancelable(true);
    }
}
