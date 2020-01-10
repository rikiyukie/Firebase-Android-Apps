package com.smart.reyog.Model;

public class Video {

    private String urlVideo, uploadDate;

    public Video() {
    }

    public Video(String urlVideo, String uploadDate) {
        this.urlVideo = urlVideo;
        this.uploadDate = uploadDate;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }
}
