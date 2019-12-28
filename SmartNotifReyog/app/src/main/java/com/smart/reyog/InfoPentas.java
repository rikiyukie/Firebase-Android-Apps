package com.smart.reyog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class InfoPentas extends AppCompatActivity {
    private TextView judul, lokasi, jadwal, info;
    private ImageView banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_pentas_layout);

        judul = (TextView)findViewById(R.id.info_judulPentas);
        lokasi = (TextView)findViewById(R.id.info_lokasi);
        jadwal = (TextView)findViewById(R.id.info_jadwal);
        info = (TextView)findViewById(R.id.info_info);
        banner = (ImageView)findViewById(R.id.info_banner);

        String textJudul = getIntent().getExtras().getString("iJudul");
        String textLokasi = getIntent().getExtras().getString("iLokasi");
        String textJadwal = getIntent().getExtras().getString("iJadwal");
        String textInfo = getIntent().getExtras().getString("iInfo");
        String bannerUrl = getIntent().getExtras().getString("iBanner");

        judul.setText(textJudul);
        lokasi.setText(textLokasi);
        jadwal.setText(textJadwal);
        info.setText(textInfo);
        Picasso.with(this).load(bannerUrl).into(banner);
    }

}
