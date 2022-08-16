package com.example.mymusicplayer;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    Button play,next,prev,fnext,fprev;
    TextView name,start,stop;
    SeekBar seekBar;
    ImageView imageView;
    //BarVisualizer blast;

    String sname;
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<AudioData> List;
    Thread updateSeekbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        prev = findViewById(R.id.prev);
        fnext = findViewById(R.id.fnext);
        fprev = findViewById(R.id.fprev);
        name = findViewById(R.id.name);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        seekBar = findViewById(R.id.seekbar);
        imageView = findViewById(R.id.imageview);
//        blast = findViewById(R.id.blast);

        List = loadAudio(this);

        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle =  i.getExtras();
        position=bundle.getInt("pos",0);

        sname = List.get(position).getName();
        Uri uri = Uri.parse(List.get(position).getPath());
        
        name.setText(sname);
        name.setSelected(true);
        
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(PlayerActivity.this,uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.start();

       setUpdateSeekbar();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mediaPlayer.isPlaying())
                {
                    play.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
                    mediaPlayer.pause();
                }
                else
                {
                    play.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                    mediaPlayer.start();
                }

            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                    next.performClick();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayer.stop();
                mediaPlayer.release();

                position = ((position+1)%List.size());
                Uri u = Uri.parse(List.get(position).getPath());

                sname = List.get(position).getName();
                name.setText(sname);
                name.setSelected(true);

                mediaPlayer = new MediaPlayer();

                try {
                    mediaPlayer.setDataSource(PlayerActivity.this,u);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                setUpdateSeekbar();
                play.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                startAnimation(imageView);
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayer.stop();
                mediaPlayer.release();

                position = ((position-1)<0)?(List.size()-1):(position-1);

                Uri uri = Uri.parse(List.get(position).getPath());

                sname = List.get(position).getName();
                name.setText(sname);
                name.setSelected(true);

                mediaPlayer = new MediaPlayer();

                try {
                    mediaPlayer.setDataSource(PlayerActivity.this,uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                setUpdateSeekbar();

                play.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                startAnimation(imageView);
            }
        });

        fnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        fprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });

    }

    public void startAnimation(View view){
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView,"rotation",0f,360f);
        animator.setDuration(1000);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public void setUpdateSeekbar(){

        updateSeekbar = new Thread(){
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentposition = 0;

                while(currentposition < totalDuration){
                    try{
                        sleep(500);
                        currentposition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currentposition);
                    } catch(InterruptedException | IllegalStateException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        seekBar.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.MULTIPLY);
        seekBar.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());

                if(mediaPlayer.getDuration()==mediaPlayer.getCurrentPosition()){
                    next.performClick();
                }
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        stop.setText(endTime);
        final Handler handler=new Handler();
        final int delay=1000;

        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                String currentTime=createTime(mediaPlayer.getCurrentPosition());
                start.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);


    }

    public String createTime(int duration) {
        String time = "";

        int min = duration / 1000 / 60;
        int sec = duration / 1000 % 60;
        time += min + ":";

        if (sec < 10) {
            time += "0";
        }

        time += sec;
        return time;
    }

    public static ArrayList<AudioData> loadAudio(Context context){

        ArrayList<AudioData> tmpList = new ArrayList<>();


        String Type_ = MediaStore.Files.FileColumns.MIME_TYPE + "=?";
        String mp3 = MimeTypeMap.getSingleton().getMimeTypeFromExtension("mp3");
        String[] Arguments = new String[]{mp3};


        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection ={
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.ALBUM
        };

        Cursor cursor = context.getContentResolver().query(uri,projection,Type_,Arguments,null);


        if(cursor!=null){
            while(cursor.moveToNext()){
                tmpList.add(new AudioData(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2)
                ));
            }
            cursor.close();
        }
        return tmpList;
    }

}