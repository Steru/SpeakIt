package pl.edu.pwr.speakit.commands;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by Steru on 2016-12-07.
 */
public class CallCommand {

    public static void makeCall(Context context, String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "No permission to make a call!", Toast.LENGTH_SHORT).show();
            return;
        }
        context.startActivity(intent);
    }
}
