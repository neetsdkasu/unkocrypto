package neetsdkasu.idpwmemoics;

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
                Log.e(TAG, "loadFile", ex);
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
                Log.e(TAG, "filecopy", ex);
            }}
            if (in != null) { try { in.close(); } catch (IOException ex) {
                Log.e(TAG, "filecopy", ex);
            }}
        }
    }
}