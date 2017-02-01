package pl.edu.pwr.speakit.commands;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pl.edu.pwr.speakit.R;

/**
 * Created by Steru on 2017-01-28.
 */
public class PlayMusicCommand {
    private static String TAG = "PlayMusicCommand";
    private final Context mContext;
    private List<Song> mSongList = new ArrayList();
    private final Handler handler;

    private Song mChosenOne;

    public PlayMusicCommand(Context context) {
        mContext = context;
        handler = new Handler(context.getMainLooper());
    }

    private void runOnUiThread(Runnable r) {
        handler.post(r);
    }

    public void playSpecificSong(String phrase) {
        matchSongTitle(phrase, getAllSongs());
        if (mSongList.isEmpty()) {
            runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  Toast.makeText(mContext,
                                          R.string.song_not_found,
                                          Toast.LENGTH_LONG).show();
                              }
                          });
        } else if (mSongList.size() == 1) {
            mChosenOne = mSongList.get(0);
            launchPlayingIntent();
        } else {
            showDialogChooser();
        }
    }

    private void launchPlayingIntent() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        File file = new File(mChosenOne.path);
        intent.setDataAndType(Uri.fromFile(file), "audio/*");
        mContext.startActivity(intent);
    }

    private void showDialogChooser() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<Song> arrayAdapter = new ArrayAdapter<>(mContext,
                        android.R.layout.select_dialog_singlechoice);

                for (Song s : mSongList){
                    arrayAdapter.add(s);
                }

                AlertDialog.Builder builderSingle = new AlertDialog.Builder(mContext);
                builderSingle.setTitle(R.string.select_song);

                builderSingle.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //String strName = mSongList.get(which).toString();
                        mChosenOne = mSongList.get(which);
                        Log.d(TAG, "Chosen -> " + mChosenOne);
                        launchPlayingIntent();
                    }
                });
                builderSingle.show();
            }
        });
    }

    private void matchSongTitle(String phrase, List<Song> allSongs) {
        StringSimilarityService similarityService =
                new StringSimilarityServiceImpl(new JaroWinklerStrategy());

        for (Song s : allSongs){
            double titleSimilarity  = similarityService.score(phrase, s.title);
            double artistSimilarity = similarityService.score(phrase, s.artist);
            if(titleSimilarity > 0.8 || artistSimilarity > 0.8) {
                Log.d(TAG, "It's a match!");
                mSongList.add(s);
            }
        }
    }

    private List<Song> getAllSongs() {
        List<Song> allSongs = new ArrayList<>();
        ContentResolver musicResolver = mContext.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.DATA);

            do {
                long thisId         = musicCursor.getLong(idColumn);
                String thisTitle    = musicCursor.getString(titleColumn);
                String thisArtist   = musicCursor.getString(artistColumn);
                String thisPath     = musicCursor.getString(pathColumn);
                allSongs.add(new Song(thisId, thisTitle, thisArtist, thisPath));
            }
            while (musicCursor.moveToNext());
        }
        return allSongs;
    }

    class Song {
        public long id;
        public String title;
        public String artist;
        public String path;

        public Song(long songID, String songTitle, String songArtist, String thisPath) {
            id = songID;
            title = songTitle;
            artist = songArtist;
            path = thisPath;
        }

        @Override
        public String toString() {
            return artist + " - " + title;
        }
    }
}