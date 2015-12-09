package com.embedonix.pslogger.helpres;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.embedonix.pslogger.MainActivity;
import com.embedonix.pslogger.SensorLoggerApplication;
import com.embedonix.pslogger.data.user.UserRegistration;
import com.embedonix.pslogger.settings.AppPreferences;
import com.embedonix.pslogger.settings.SettingsActivity;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by Saeid on 28-3-2014.
 * <p/>
 * These class contains static methods for appliction MobileHealth
 */
public class AppHelpers {

    private static final String TAG = "AppHelpers";

    private static void showInvalidBaseUrlDialog(Activity activity, String message) {
        AppHelpers.showErrorDialog(activity
                , "Error validating base url",
                message
                , "change", "later"
                , DialogAction.SHOW_PREFERENCES_SCREEN
                , DialogAction.SET_BASE_URL_AS_BAD);
    }

    public static void showErrorDialog(final Activity activity, String title, String message,
                                       String positiveButtonText,
                                       String negativeButtonText,
                                       final DialogAction positiveAction,
                                       final DialogAction negativeAction) {

        new AlertDialog.Builder(activity).setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doDialogAction(activity, positiveAction);
                    }
                }).setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doDialogAction(activity, negativeAction);
            }
        }).show();
    }

    public static boolean isOnWifiInternet(Context context) {
        ConnectivityManager connManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return (info.isAvailable() && info.isConnected());
    }

    /**
     * Checks if a service does exist in service pool
     *
     * @param context
     * @param className
     * @return
     */
    public static ComponentName isServiceExist(Context context, String className) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> listServices =
                am.getRunningServices(Integer.MAX_VALUE);

        if (listServices.size() <= 0) {
            return null;
        }

        for (ActivityManager.RunningServiceInfo serviceInfo : listServices) {
            ComponentName serviceName = serviceInfo.service;

            if (serviceName.getClassName().equals(className)) {
                Log.d("isServiceExist()", "Searching for " + className
                        + " DID found it running!");
                return serviceName;
            }
        }

        Log.d("isServiceExist()", "Searching for " + className
                + " NOT found it running!");
        return null;
    }

    /**
     * Shows a long toast message
     *
     * @param context Context that toast should be visible in
     * @param message String to be shown
     */
    public static void showToastLong(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showToastShort(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * This method checks if a value is within a bound
     *
     * @param a lower range
     * @param b higher range
     * @param c number ot be checked
     * @return
     */
    public static boolean isBetween(int a, int b, int c) {
        return b > a ? c > a && c < b : c > b && c < a;
    }

    /**
     * Checks if a given <b>CharSequence</b> is an valid email
     *
     * @param target the <b>CharSequence</b> to be verified
     * @return true if valid email, flase if not!
     */
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target)
                && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isOnMobileInternet(Context context) {
        if (isInternetAvailable(context)) {
            ConnectivityManager cm =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static void showErrorDialog(final Activity activity, String title, String message,
                                       String positiveButtonText,
                                       final DialogAction positiveAction) {

        new AlertDialog.Builder(activity).setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doDialogAction(activity, positiveAction);
                    }
                }).show();
    }

    public static void doDialogAction(Activity activity, DialogAction action) {
        switch (action) {
            case NO_ACTION:
                break;
            case EXIT_APP:
                Intent intent = new Intent(activity.getApplicationContext(), MainActivity.class);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(AppConstants.EXTRAS_EXIT_COMPLETELY, true);
                activity.startActivity(intent);
                break;
            case FINISH_ACTIVITY:
                finishActivity(activity);
                break;
            case REDIRECT_LOGIN:
                Toast.makeText(activity.getApplicationContext(), "redirecting to login page...",
                        Toast.LENGTH_SHORT).show();
                break;
            case REDIRECT_REGISTRATION:
                Toast.makeText(activity.getApplicationContext(), "redirecting to " +
                        "registration page...", Toast.LENGTH_SHORT).show();
                break;
            case SET_BASE_URL_AS_BAD:
                AppHelpers.getAppPreferences(activity.getApplicationContext())
                        .setServerBaseUrlValidity(false);
                break;
            case SHOW_PREFERENCES_SCREEN:
                Intent prefIntent = new Intent(activity.getApplicationContext()
                        , SettingsActivity.class);
                activity.startActivity(prefIntent);
                break;
            default:
                Toast.makeText(activity.getApplicationContext(), "DialogAction is not defined",
                        Toast.LENGTH_SHORT).show();
        }
    }

    public static void finishActivity(Activity activity) {
        activity.moveTaskToBack(true);
        activity.finish();
    }

    public static AppPreferences getAppPreferences(Context context) {
        //TODO decide which method is better!
        //return ((SensorLoggerApplication) context.getApplicationContext()).getAppPreferences();
        return new AppPreferences(context);
    }

    public static void showErrorDialog(final Activity activity, String title, String message,
                                       String positiveButtonText,
                                       String negativeButtonText,
                                       String neutralButtonText,
                                       final DialogAction positiveAction,
                                       final DialogAction negativeAction,
                                       final DialogAction neutralAction) {

        new AlertDialog.Builder(activity).setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        doDialogAction(activity, positiveAction);
                    }
                }).setNegativeButton(negativeButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doDialogAction(activity, negativeAction);
            }
        }).setNeutralButton(neutralButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                doDialogAction(activity, neutralAction);
            }
        }).show();
    }

    /**
     * Check if the given integer is odd
     *
     * @param value integer to be checked
     * @return true if value is odd
     */
    public static boolean isOdd(int value) {
        return ((value & 1) == 0);
    }

    /**
     * Check if the given integer is even
     *
     * @param value integer to be checked
     * @return true if value is even
     */
    public static boolean isEven(int value) {
        return ((value & 1) != 0);
    }

    /**
     * Get width of device display in pixels
     * Assume that the width is device in portrait mode
     *
     * @param context
     * @return
     */
    public static int getDeviceScreenWidthAsPixel(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        // the results will be higher than using the activity context object
        // or the getWindowManager() shortcut
        WindowManager wm = (WindowManager) context.getApplicationContext().
                getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics.widthPixels;
    }

    public static int getDeviceScreenHeightAsPixel(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        // the results will be higher than using the activity context object
        // or the getWindowManager() shortcut
        WindowManager wm = (WindowManager) context.getApplicationContext().
                getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics.heightPixels;
    }

    public static String getDateTimeMySqlFormat() {
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(dt);
    }

    public static void showNoInternetConnectionDialog(Context applicationContext) {
        showErrorDialog(applicationContext
                , "no connectivity"
                , "internet connection is not available at this time, please try again later!"
                , "return");
    }

    /**
     * Shows an AlertDialog with title and message and a button. The button will does nothing except
     * returning to the context.
     *
     * @param context    the activity which the dialog should pop in
     * @param title      title of the dialog
     * @param message    the message to be shown
     * @param buttonText the text to be shown on the button
     */
    public static void showErrorDialog(Context context, String title, String message,
                                       String buttonText) {

        new AlertDialog.Builder(context).setTitle(title)
                .setMessage(message)
                .setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Do nothing!
                    }
                }).show();
    }

    public static HttpResponse sendHttpPostRequest(List<NameValuePair> postPairs, String url) {
        HttpResponse response = null;
        HttpPost httppost = new HttpPost(url);

        try {
            httppost.setEntity(new UrlEncodedFormEntity(postPairs));
            HttpClient client = new DefaultHttpClient();
            response = client.execute(httppost);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return response;
    }

    public static ArrayList<ArrayList<String>> parseHtmlBreakSeparatedAsStringLines(String toParse)
            throws IOException {

        ArrayList<ArrayList<String>> parsed = new ArrayList<ArrayList<String>>();

        String formattedResults = toParse.replace("<br>", "\n");
        InputStream is = new ByteArrayInputStream(formattedResults.getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;
        while ((line = br.readLine()) != null) {
            List<String> items = Arrays.asList(line.split("\\s*,\\s*"));

            ArrayList<String> tempList = new ArrayList<String>();

            for (String item : items) {
                tempList.add(item);
            }

            parsed.add(tempList);
        }

        return parsed;
    }

    /**
     * This method down-samples and resize the given bitmap
     *
     * @param context
     * @param imageFile
     * @param newHeight
     * @return
     * @throws java.io.FileNotFoundException
     */
    public static Bitmap resizeBitmap(Context context, Uri imageFile, int newHeight)
            throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageFile)
                , null, o);

        // The new size we want to scale to

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < newHeight
                    || height_tmp / 2 < newHeight) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageFile)
                , null, o2);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static UserRegistration getUserRegistration(Context context, boolean newReference) {
        return ((SensorLoggerApplication) context)
                .getUserRegistration(newReference);
    }

    public static String getServerBaseUrl(Context context) {
        return getAppPreferences(context.getApplicationContext()).getServerBaseUrl();
    }
}
