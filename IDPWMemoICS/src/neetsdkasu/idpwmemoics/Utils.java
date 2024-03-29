package neetsdkasu.idpwmemoics;

import android.os.Environment;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

final class Utils {

    private static final String TAG = "Utils";

    private Utils() {}

    static final String EXTRA_ARGUMENTS = "neetsdkasu.idpwmemoics.EXTRA_ARGUMENTS";

    static final String KEY_MEMO_NAME = "MEMO_NAME";
    static final String KEY_MEMO_PASSWORD = "MEMO_PASSWORD";
    static final String KEY_SERVICE_INDEX = "SERVICE_INDEX";

    static final String MEMO_DIR = "memo";
    static final String MEMO_EXT = ".memo";

    static boolean isValidMemoNameLength(int len) {
        return 0 < len && len <= 50;
    }

    static boolean isValidMemoNameChar(char ch) {
        return ('A' <= ch && ch <= 'Z')
            || ('a' <= ch && ch <= 'z')
            || ('0' <= ch && ch <= '9')
            || ch == '_'
            || ch == '-'
            || ch == '('
            || ch == ')'
            || ch == '['
            || ch == ']';
    }

    static boolean isValidMemoNameChars(CharSequence s, int start, int end) {
        for (int i = start; i < end; i++) {
            if (!isValidMemoNameChar(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static boolean saveFile(File file, byte[] data) {
        OutputStream out = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            out = new BufferedOutputStream(new FileOutputStream(file));
            out.write(data);
            out.flush();
            return true;
        } catch (IOException ex) {
            Log.e(TAG, "saveFile", ex);
            return false;
        } finally {
            if (out != null) { try { out.close(); } catch (IOException ex) {
                Log.e(TAG, "saveFile.out.close", ex);
            }}
        }
    }

    static byte[] loadFile(File file) {
        byte[] buf = new byte[(int)file.length()];
        int pos = 0;
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            int len;
            while (pos < buf.length && (len = in.read(buf, pos, buf.length - pos)) >= 0) {
                pos += len;
            }
            return buf;
        } catch (IOException ex) {
            Log.e(TAG, "loadFile", ex);
            return null;
        } finally {
            if (in != null) { try { in.close(); } catch (IOException ex) {
                Log.e(TAG, "loadFile.in.close", ex);
            }}
        }
    }

    static boolean filecopy(File src, File dst) {
        OutputStream out = null;
        InputStream in = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(dst));
            in = new BufferedInputStream(new FileInputStream(src));
            byte[] buf = new byte[2048];
            int len;
            while ((len = in.read(buf, 0, buf.length)) >= 0) {
                out.write(buf, 0, len);
            }
            out.flush();
            return true;
        } catch (IOException ex) {
            Log.e(TAG, "filecopy", ex);
            return false;
        } finally {
            if (out != null) { try { out.close(); } catch (IOException ex) {
                Log.e(TAG, "filecopy.out.close", ex);
            }}
            if (in != null) { try { in.close(); } catch (IOException ex) {
                Log.e(TAG, "filecopy.in.close", ex);
            }}
        }
    }

    private static java.text.SimpleDateFormat dateFormat = null;

    static String getDateTimeString(long unixTime) {
        if (unixTime == 0L) {
            return "-";
        } else {
            if (dateFormat == null) {
                dateFormat = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", java.util.Locale.US);
            }
            return dateFormat.format(new java.util.Date(unixTime));
        }
    }

    private static java.text.SimpleDateFormat exportDateFormat = null;

    static String getExportDateTimeString(long unixTime) {
        if (exportDateFormat == null) {
            exportDateFormat = new java.text.SimpleDateFormat(
                "yyyyMMddHHmmss", java.util.Locale.US);
        }
        return exportDateFormat.format(new java.util.Date(unixTime));
    }

    static String toString(idpwmemo.Value v) {
        return v.getTypeName() + ": " + v.value;
    }

    static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state)
            || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    static boolean isExternalStorageWriteable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
}