package com.achyut.simplemusicplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements android.widget.MediaController.MediaPlayerControl {
    private Button play, pause, previous, next;
    ArrayList<MusicDetail> musicDetails;
    MusicAdapter adapter;
    private MusicService musicService = new MusicService();
    private Intent musicIntent;
    private boolean musicBound = false;
    //controller
    private MusicController controller;

    //activity and playback pause flags
    private boolean paused = false, playbackPaused = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);


        adapter = new MusicAdapter(getMusicDetail(this.getContentResolver()), this);
        recyclerView.setAdapter(adapter);


        //setup controller
        setController();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_shuffle:
                musicService.setShuffle();
                break;
            case R.id.action_end: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Confirm")
                        .setMessage("Are you sure want to exit the application")
                        .setCancelable(true)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stopService(musicIntent);
                                musicService = null;
                                System.exit(0);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                controller.show(0);

                            }
                        });
                builder.show();


                break;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    //set the controller up
    private void setController() {
        controller = new MusicController(this);
        //set previous and next button listeners
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        //set and show
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.recycler_view));
        controller.setEnabled(true);
    }


    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setMusicList(musicDetails);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (musicIntent == null) {
            musicIntent = new Intent(this, MusicService.class);
            bindService(musicIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(musicIntent);
        }
    }

    public void songPicked(View view) {
        musicService.setSong((Integer) view.getTag());
        musicService.playSong();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    public ArrayList<MusicDetail> getMusicDetail(ContentResolver contentResolver) {

        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(musicUri, null, null, null, null, null);
        musicDetails = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do {
                int id = cursor.getInt(idColumn);
                String musicTitle = cursor.getString(titleColumn);
                String artistName = cursor.getString(artistColumn);
                musicDetails.add(new MusicDetail(id, musicTitle, artistName));
            }
            while (cursor.moveToNext());
        }
        return musicDetails;
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (paused) {
            setController();
            paused = false;
        }

    }

    @Override
    protected void onDestroy() {

        stopService(musicIntent);
        musicService = null;
        super.onDestroy();
    }


    // Controller Part begins
    public void playNext() {
        musicService.playNext();
        controller.show(0);
    }

    private void playPrev() {
        musicService.playPrevious();
        controller.show(0);
    }

    @Override
    public void start() {
        musicService.go();

    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicService.pauseSong();

    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPng())
            return musicService.getDur();
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicBound && musicService.isPng())
            return musicService.getPosn();
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicService != null && musicBound)
            return musicService.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }


}
