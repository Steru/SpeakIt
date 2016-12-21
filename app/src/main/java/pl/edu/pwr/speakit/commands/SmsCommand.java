package pl.edu.pwr.speakit.commands;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Steru on 2016-12-08.
 */
public class SmsCommand {

    public static void sendSms(Context context, String number, String message){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + number));
        intent.putExtra("sms_body", message);
        context.startActivity(intent);
    }
}
