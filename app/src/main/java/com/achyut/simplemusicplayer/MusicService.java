package com.achyut.simplemusicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;


public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    private ArrayList<MusicDetail> musicList;
    private MediaPlayer player;

    private boolean shuffle = false;

    private Random rand;

    //current position
    private int songPosn;

    private final IBinder musicBind = new MusicBinder();

    private String songTitle = null;
    private static final int NOTIFY_ID = 1;

    public MusicService() {
    }

    public void setSong(int songPosn) {
        this.songPosn = songPosn;
    }

    public void go() {
        player.start();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();

        rand = new Random();
        initMusicPlayer();

    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return musicBind;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0) {
            player.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        player.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.player_play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);

    }


    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void setMusicList(ArrayList<MusicDetail> songs) {
        musicList = songs;

    }

    //play a song
    public void playSong() {
        //play
        player.reset();
        //get song
        MusicDetail playSong = musicList.get(songPosn);
        //get title
        songTitle = playSong.getMusicTitle();
        //get id
        long currSong = playSong.getMusicId();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);
        //set the data source
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public void playNext() {

        if (shuffle) {
            int song = songPosn;
            while (song == songPosn) {
                song = rand.nextInt(musicList.size());


            }
            songPosn = song;
        } else {
            songPosn++;
            if (songPosn >= musicList.size()) {
                songPosn = 0;
            }
        }
        playSong();

    }

    public void playPrevious() {
        songPosn--;
        if (songPosn < 0) {
            songPosn = musicList.size() - 1;
        }
        playSong();
    }

    public void pauseSong() {
        player.pause();
    }

    //playback methods
    public int getPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPng() {
        return player.isPlaying();
    }

    public void seek(int position) {
        player.seekTo(position);
    }

    public void setShuffle() {
        shuffle = (shuffle) ? false : true;
        String status = (shuffle) ? "ON" : "OFF";
        Toast.makeText(this, "Shuffe is  " + status, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }


}
