package pl.edu.pwr.speakit.commands;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

import java.util.List;

/**
 * Created by Steru on 2016-12-21.
 */
public class LaunchAppCommand {
    private static final String TAG = "LaunchAppCommand";
    public static final double SIMILARITY_VALUE = 0.85;

    public static void launchApp(Context context, String requestedAppName) {
        searchForRequestedApp(context, requestedAppName);
        //TODO launch the app
    }

    private static void searchForRequestedApp(Context context, String requestedAppName) {
        StringSimilarityService similarityService =
                new StringSimilarityServiceImpl(new JaroWinklerStrategy());
        final PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            Intent launchIntentForPackage = pm.getLaunchIntentForPackage(packageInfo.packageName);
            if (launchIntentForPackage != null) {
                try {
                    ApplicationInfo app = pm.getApplicationInfo(packageInfo.packageName, 0);
                    String appName = pm.getApplicationLabel(app).toString();
                    //Drawable icon = pm.getApplicationIcon(app);
                    double appNameSimilarityScore = similarityService.score(requestedAppName, appName);

                    //TODO figure a way to select an app which is most similar to given name
                    if (appNameSimilarityScore < SIMILARITY_VALUE) {
                        continue;
                    }
                    Log.d(TAG, "Installed package :" + packageInfo.packageName);
                    Log.d(TAG, "Launch Activity :" + launchIntentForPackage);
                    Log.d(TAG, "App name = " + appName + ", similarity :" + appNameSimilarityScore);

                    Intent intent = new Intent(launchIntentForPackage);
                    context.startActivity(intent);
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e(TAG, "error in getting app name");
                    e.printStackTrace();
                }
            }
        }
    }
}
