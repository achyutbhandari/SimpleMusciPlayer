package com.achyut.simplemusicplayer;

/**
 * Created by bhand on 2/11/2017.
 */

public class MusicDetail {

    private int musicId;
    private String musicTitle;
    private String artistName;

    public MusicDetail(int musicId, String musicTitle, String artistName) {
        this.musicId = musicId;
        this.musicTitle = musicTitle;
        this.artistName = artistName;
    }

    public int getMusicId() {
        return musicId;
    }

    public String getMusicTitle() {
        return musicTitle;
    }

    public String getArtistName() {
        return artistName;
    }

}
