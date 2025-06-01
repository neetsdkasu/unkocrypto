package neetsdkasu.misc;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;

public final class Utils {

    private Utils() {}

    public static boolean inRange(int value, int min, int max) {
        return min <= value && value <= max;
    }

    public static boolean inSize(int value, int size) {
        return Utils.inRange(value, 0, size - 1);
    }

    public static void internalError(Context c, Object ie) {
        Utils.alertShort(c, "Internal Error: " + ie.toString());
    }

    public static void alertShort(Context c, int msgResId) {
        Toast.makeText(c, msgResId, Toast.LENGTH_SHORT).show();
    }

    public static void alertShort(Context c, String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }

    @android.annotation.SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyMMdd:HHmm");

    public static String formatDate(long time) {
        if (time == 0L) {
            return "000000:0000";
        }
        return DATE_FMT.format(new java.util.Date(time));
    }

    public static void setSecure(Activity activity) {
        Window window = activity.getWindow();
        if (window != null) {
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);
        }
    }

    public static void clearFocus(Activity activity) {
        View focus = activity.getCurrentFocus();
        if (focus != null && focus.isInTouchMode()) {
            focus.clearFocus();
        }
    }

    public static void hideInputMethod(Activity activity, TextView... views) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            for (TextView view : views) {
                if (imm.isActive(view) && view.isInTouchMode()) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
        Utils.clearFocus(activity);
    }

    // private static final int BUILD_VERSIONS_CODES_S_V2 = 32;

    public static void copyToClipboard(Context context, boolean secret, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newPlainText("", text);

        if (secret) {
            android.os.PersistableBundle bundle = new android.os.PersistableBundle();
            bundle.putBoolean("android.content.extra.IS_SENSITIVE", true);
            clip.getDescription().setExtras(bundle);
        }

        clipboard.setPrimaryClip(clip);

        // if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
        // if (android.os.Build.VERSION.SDK_INT <= BUILD_VERSIONS_CODES_S_V2) {
        //     Utils.alertShort(context, R.string.msg_copied);
        // }
    }

    public static byte[] decodeBase64(String src) {
        try {
            // return java.util.Base64.getMimeDecoder().decode(src);
            return android.util.Base64.decode(src, android.util.Base64.DEFAULT);
        } catch (Exception ex) {
            // wrong src
            return null;
        }
    }

    public static String encodeBase64(byte[] src) {
        try {
            // return java.util.Base64.getMimeEncoder().encodeToString(src);
            return android.util.Base64.encodeToString(src, android.util.Base64.DEFAULT);
        } catch (Exception ex) {
            // wrong src
            return null;
        }
    }

    public static String ifNullToDefault(String str, String defaultStr) {
        return str == null ? defaultStr.toString() : str;
    }

    public static String ifNullToBlank(String s) {
        return Utils.ifNullToDefault(s, "");
    }

    public static boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static boolean isNotBlank(String s) {
        return !Utils.isNullOrBlank(s);
    }
}
