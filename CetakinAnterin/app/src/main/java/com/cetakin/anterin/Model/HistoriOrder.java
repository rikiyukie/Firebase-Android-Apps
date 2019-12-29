package com.cetakin.anterin.Model;

public class HistoriOrder {

    private String nama, email, nohp, namafile, keterangan, orderdate, diterima, dikonfirmasi, status, alamat;

    public HistoriOrder() {
    }

    public HistoriOrder(String nama, String email, String nohp, String namafile, String keterangan, String orderdate, String diterima, String dikonfirmasi, String status, String alamat) {
        this.nama = nama;
        this.email = email;
        this.nohp = nohp;
        this.namafile = namafile;
        this.keterangan = keterangan;
        this.orderdate = orderdate;
        this.diterima = diterima;
        this.dikonfirmasi = dikonfirmasi;
        this.status = status;
        this.alamat = alamat;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNohp() {
        return nohp;
    }

    public void setNohp(String nohp) {
        this.nohp = nohp;
    }

    public String getNamafile() {
        return namafile;
    }

    public void setNamafile(String namafile) {
        this.namafile = namafile;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public String getOrderdate() {
        return orderdate;
    }

    public void setOrderdate(String orderdate) {
        this.orderdate = orderdate;
    }

    public String getDiterima() {
        return diterima;
    }

    public void setDiterima(String diterima) {
        this.diterima = diterima;
    }

    public String getDikonfirmasi() {
        return dikonfirmasi;
    }

    public void setDikonfirmasi(String dikonfirmasi) {
        this.dikonfirmasi = dikonfirmasi;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }
}
