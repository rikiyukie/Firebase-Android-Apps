package com.fans.edu.Model;

public class Artikel {

    private String Judul, Isi, gambarUrl, lastUpdate;

    public Artikel() {
    }

    public Artikel(String judul, String isi, String gambarUrl, String lastUpdate) {
        Judul = judul;
        Isi = isi;
        this.gambarUrl = gambarUrl;
        this.lastUpdate = lastUpdate;
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

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
