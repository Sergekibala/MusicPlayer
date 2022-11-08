package com.example.musicplayer;

import static android.bluetooth.BluetoothGattDescriptor.PERMISSION_READ;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.model.Model;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Vars Global

    RecyclerView recyclerView;
    TextView tvSongTitle, tvCurrentPos, tvTotalDuration;
    ImageView btnPrev, btnPlay, btnNext;
    SeekBar sbPosition;


    MediaPlayer mediaPlayer;

    ArrayList<ModelSong> songArrayList;

    int currentArrayPos = 0;

    public static final int PERMISSION_READ = 0;

    double currentPos, totalDuration;

    // Initialisation des composants graphiques

    private void initUI() {

        recyclerView = findViewById(R.id.recyclerView);
        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvCurrentPos = findViewById(R.id.tvCurrentPos);
        tvTotalDuration = findViewById(R.id.tvTotalDuration);
        btnPrev = findViewById(R.id.ivBtnPrev);
        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        sbPosition = findViewById(R.id.sbPosition);


        mediaPlayer = new MediaPlayer();

        songArrayList = new ArrayList<>();
        // Init du recycler

        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

// Methode pour vérifier l'acces aux données situées sur l'espace de stockage externe

    public boolean checkPermission() {
        int READ_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (READ_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_READ);
            return false;
        }
        return true;


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_READ: {
                if (grantResults.length > 0 && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(getApplicationContext(), "Please allow storage permission",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        setSong();

                    }
                }
            }
        }
    }

    private void setSong() {

        getAudioFiles();

        // Gestion de seekbar

        sbPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                currentPos = seekBar.getProgress();
                mediaPlayer.seekTo((int) currentPos);

            }
        });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    currentArrayPos++;

                    if(currentArrayPos < (songArrayList.size())){
                        playSong(currentArrayPos);

                    }else {

                        currentArrayPos = 0;

                        }
                    playSong(currentArrayPos);

                }
            });

        if(!songArrayList.isEmpty()){
            playSong(currentArrayPos);
            prevSong();
            nextSong();
            pauseSong();


        }

    }

    private void playSong(int pos) {

        try {

            mediaPlayer.reset();
            mediaPlayer.setDataSource(this, songArrayList.get(pos).getSongUri());
            mediaPlayer.prepare();
            mediaPlayer.start();
            btnPlay.setImageResource(R.drawable.ic_baseline_music_note_24);
            tvSongTitle.setText(songArrayList.get(pos).getSongTitle());
            currentArrayPos = pos;

        } catch (IOException e) {
            e.printStackTrace();

        }

        setSongProgress();

    }

    private void setSongProgress() {


        currentPos = mediaPlayer.getCurrentPosition();
        totalDuration = mediaPlayer.getDuration();
        tvCurrentPos.setText(timeConversion((long) currentPos));
        tvTotalDuration.setText(timeConversion((long) totalDuration));

        sbPosition.setMax((int) totalDuration);

        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    currentPos = mediaPlayer.getCurrentPosition();
                    tvCurrentPos.setText(timeConversion((long) currentPos));
                    sbPosition.setProgress((int) currentPos);
                    handler.postDelayed(this, 1000);
                } catch (IllegalStateException e) {
                    e.printStackTrace();

                }

            }
        };

        handler.postDelayed(runnable, 1000);

    }

    public String timeConversion(long value) {
        String songDuration;
        int dur = (int) value; // la durée en millis

        int hrs = dur / 3600000;
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            songDuration = String.format("%02d:%02:%02", hrs, mns, scs);
        } else {
            songDuration = String.format("%02d:%02d", mns, scs);
        }
        return songDuration;
    }

    private void prevSong() {
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentArrayPos > 0) {
                    currentArrayPos--;

                } else {

                    currentArrayPos = songArrayList.size() - 1;
                }

                playSong(currentArrayPos);
            }
        });

    }
        private void nextSong(){
            btnNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (currentArrayPos < (songArrayList.size() - 1)) {


                        currentArrayPos++;
                    } else {

                        currentArrayPos = songArrayList.size() - 1;
                    }
                    playSong(currentArrayPos);
                }
        });

}

            private void pauseSong() {
                btnPlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            btnPlay.setImageResource(R.drawable.ic_baseline_music_note_24);
                        } else {
                            mediaPlayer.start();
                            btnPlay.setImageResource(R.drawable.ic_baseline_music_note_24);

                        }
                    }
                });
            }

    public void getAudioFiles() {

                ContentResolver contentResolver = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                Cursor cursor = contentResolver.query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {

                    do {
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                        String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

                        Uri coverFolder = uri.parse("content://media/external/audio/albumart");
                        Uri albumArtUri = ContentUris.withAppendedId(coverFolder, albumId);
                        Log.i("TAG", "getAudioFiles: " + albumArtUri);
                        // rEMPLACE DU MODEL

                        ModelSong modelSong = new ModelSong();
                        modelSong.setSongTitle(title);
                        modelSong.setSongArtist(artist);
                        modelSong.setSongUri(Uri.parse(url));
                        modelSong.setSongDuration(duration);
                        modelSong.setSongCover(albumArtUri);

                        songArrayList.add(modelSong);


                    } while (cursor.moveToNext());

                    AdapterSong adapterSong = new AdapterSong(this, songArrayList);
                    recyclerView.setAdapter(adapterSong);

                    adapterSong.setOnItemClickListener(new AdapterSong.OnItemClickListener() {
                        @Override
                        public void onItemClick(int pos, View view) {
                            playSong(pos);
                        }
                    });

                }
            }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();

        if (checkPermission()) {
            setSong();

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!= null){
            mediaPlayer.release();
        }
    }
}


