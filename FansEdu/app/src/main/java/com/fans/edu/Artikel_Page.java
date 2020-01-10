package com.fans.edu;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class Artikel_Page extends AppCompatActivity {

    private TextView judul, isi;
    private ImageView gambar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artikel_page_layout);

        judul = (TextView)findViewById(R.id.artikel_judul);
        isi = (TextView)findViewById(R.id.artikel_isi);
        gambar = (ImageView)findViewById(R.id.artikel_img);

        String txtJudul = getIntent().getExtras().getString("iJudul");
        String txtIsi = getIntent().getExtras().getString("iIsi");
        String imgUrl = getIntent().getExtras().getString("iImg");

        judul.setText(txtJudul);
        isi.setText(txtIsi);
        Picasso.with(this).load(imgUrl).into(gambar);
    }
}
