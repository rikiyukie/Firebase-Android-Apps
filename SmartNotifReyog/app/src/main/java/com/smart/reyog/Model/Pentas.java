package com.smart.reyog.Model;

public class Pentas {
    private String NamaPentas, LokasiPentas, JadwalPentas, InfoPentas, ImgUrl, uploadDate;

    public Pentas() {
    }

    public Pentas(String namaPentas, String lokasiPentas, String jadwalPentas, String infoPentas, String imgUrl, String uploadDate) {
        NamaPentas = namaPentas;
        LokasiPentas = lokasiPentas;
        JadwalPentas = jadwalPentas;
        InfoPentas = infoPentas;
        ImgUrl = imgUrl;
        this.uploadDate = uploadDate;
    }

    public String getNamaPentas() {
        return NamaPentas;
    }

    public void setNamaPentas(String namaPentas) {
        NamaPentas = namaPentas;
    }

    public String getLokasiPentas() {
        return LokasiPentas;
    }

    public void setLokasiPentas(String lokasiPentas) {
        LokasiPentas = lokasiPentas;
    }

    public String getJadwalPentas() {
        return JadwalPentas;
    }

    public void setJadwalPentas(String jadwalPentas) {
        JadwalPentas = jadwalPentas;
    }

    public String getInfoPentas() {
        return InfoPentas;
    }

    public void setInfoPentas(String infoPentas) {
        InfoPentas = infoPentas;
    }

    public String getImgUrl() {
        return ImgUrl;
    }

    public void setImgUrl(String imgUrl) {
        ImgUrl = imgUrl;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }
}
