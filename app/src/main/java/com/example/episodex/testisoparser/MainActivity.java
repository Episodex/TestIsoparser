package com.example.episodex.testisoparser;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void mergeVideos(View view) {
        Movie result = new Movie();
        List<Track> videoTracks = new LinkedList<>();
        List<Track> audioTracks = new LinkedList<>();
        String[] videoUris = new String[] {
                "content://media/external/video/media/53999","content://media/external/video/media/54002","content://media/external/video/media/54004","content://media/external/video/media/54005","content://media/external/video/media/54006","content://media/external/video/media/55445","content://media/external/video/media/56938","content://media/external/video/media/56939","content://media/external/video/media/56940","content://media/external/video/media/58420","content://media/external/video/media/58421","content://media/external/video/media/59932","content://media/external/video/media/59933","content://media/external/video/media/59934"
        };



        try {
            for (String clipUriPath : videoUris) {
                Uri clipUri = Uri.parse(clipUriPath);
                String filePath = getRealPathFromUri(this, clipUri);

                File file = new File(filePath);

                FileDataSourceImpl dataSource = new FileDataSourceImpl(file);
                Movie part = MovieCreator.build(dataSource);

                Track videoTrack = null;
                Track  audioTrack = null;

                for (Track t : part.getTracks()) {
                    if (t.getHandler().equals("soun")) {
                        audioTrack = new CroppedTrack(t, 0, t.getSamples().size() - 12);
                        audioTracks.add(audioTrack);
                    }
                    if (t.getHandler().equals("vide")) {
                        videoTrack = t;
                        videoTracks.add(videoTrack);
                    }
                }
            }

            if (videoTracks.size() > 0) {
                result.addTrack(new AppendTrack(videoTracks
                        .toArray(new Track[videoTracks.size()])));
            }

            if (audioTracks.size() > 0) {
                result.addTrack(new AppendTrack(audioTracks
                        .toArray(new Track[audioTracks.size()])));
            }

            try {
                String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/IsoparserTest/";
                new File(dirPath).mkdir();
                FileChannel outputStream = new FileOutputStream(new File(dirPath + "test_output.mp4"), false).getChannel();
                Container mp4 = new DefaultMp4Builder().build(result);
                mp4.writeContainer(outputStream);
                outputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("IsoparserTest", e.getMessage());
            } catch (IOException e) {
                Log.e("IsoparserTest", "" + e.getMessage());
            } catch (NoSuchElementException e) {
                Log.e("IsoparserTest", "" + e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("IsoparserTest", "" + e.getMessage());
        }
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {
                    MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);

            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            cursor.moveToFirst();

            return cursor.getString(column_index);
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }
    }
}
