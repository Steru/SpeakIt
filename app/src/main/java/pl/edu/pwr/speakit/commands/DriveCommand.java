package pl.edu.pwr.speakit.commands;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Steru on 2017-02-01.
 */
public class DriveCommand {
    public static void go(Context context, String subject) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("google.navigation:q="+ URLEncoder.encode(subject, "UTF-8")));
            context.startActivity(intent);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
