package com.cetakin.anterin.Model;

public class Order {
    private String Email, Nama, Alamat, NoHp, Keterangan, FileUrl, orderDate, Status;

    public Order() {
    }

    public Order(String email, String nama, String alamat, String noHp, String keterangan, String fileUrl, String orderDate, String status) {
        Email = email;
        Nama = nama;
        Alamat = alamat;
        NoHp = noHp;
        Keterangan = keterangan;
        FileUrl = fileUrl;
        this.orderDate = orderDate;
        Status = status;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getNama() {
        return Nama;
    }

    public void setNama(String nama) {
        Nama = nama;
    }

    public String getAlamat() {
        return Alamat;
    }

    public void setAlamat(String alamat) {
        Alamat = alamat;
    }

    public String getNoHp() {
        return NoHp;
    }

    public void setNoHp(String noHp) {
        NoHp = noHp;
    }

    public String getKeterangan() {
        return Keterangan;
    }

    public void setKeterangan(String keterangan) {
        Keterangan = keterangan;
    }

    public String getFileUrl() {
        return FileUrl;
    }

    public void setFileUrl(String fileUrl) {
        FileUrl = fileUrl;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
