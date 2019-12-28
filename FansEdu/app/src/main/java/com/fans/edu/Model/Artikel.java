package com.fans.edu.Model;

public class Artikel {

    private String Judul, Isi, gambarUrl;

    public Artikel() {
    }

    public Artikel(String judul, String isi, String gambarUrl) {
        Judul = judul;
        Isi = isi;
        this.gambarUrl = gambarUrl;
    }

    public String getJudul() {
        return Judul;
    }

    public void setJudul(String judul) {
        Judul = judul;
    }

    public String getIsi() {
        return Isi;
    }

    public void setIsi(String isi) {
        Isi = isi;
    }

    public String getGambarUrl() {
        return gambarUrl;
    }

    public void setGambarUrl(String gambarUrl) {
        this.gambarUrl = gambarUrl;
    }
}
