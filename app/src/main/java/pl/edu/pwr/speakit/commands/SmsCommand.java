package pl.edu.pwr.speakit.commands;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Steru on 2016-12-08.
 */
public class SmsCommand {

    public static void sendSms(Context context, String number, String message){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.putExtra("sms_body", message);
        intent.setData(Uri.parse("smsto:" + number));
        context.startActivity(intent);
    }
}
