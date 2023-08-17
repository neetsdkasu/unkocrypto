package neetsdkasu.idpwmemo10;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.IntPredicate;

final class Utils {

    private Utils() {}

    static final String MEMO_DIR = "memo";
    static final String EXTENSION = ".memo";

    static final int MEMO_NAME_LENGTH_MIN = 1;
    static final int MEMO_NAME_LENGTH_MAX = 50;

    static File getMemoDir(Context c) {
        return c.getDir(Utils.MEMO_DIR, Context.MODE_PRIVATE);
    }

    static File getMemoFile(Context c, String memoName) {
        return  new File(Utils.getMemoDir(c), memoName);
    }

    static boolean inRange(int value, int min, int max) {
        return min <= value && value <= max;
    }

    static boolean inSize(int value, int size) {
        return Utils.inRange(value, 0, size - 1);
    }

    static boolean isValidMemoNameCharacter(int ch) {
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

    static final IntPredicate memoNameCharacterChecker = new IntPredicate() {
        public boolean test(int value) {
            return Utils.isValidMemoNameCharacter(value);
        }
    };

    static boolean isValidMemoName(CharSequence name) {
        if (name == null) {
            return false;
        }
        if (!Utils.inRange(name.length(), Utils.MEMO_NAME_LENGTH_MIN, Utils.MEMO_NAME_LENGTH_MAX)) {
            return false;
        }
        return name.chars().allMatch(Utils.memoNameCharacterChecker);
    }

    static boolean isValidServiceName(String name) {
        if (name == null) {
            return false;
        }
        return name.trim().length() > 0;
    }

    static void alertShort(Context c, int msgResId) {
        Toast.makeText(c, msgResId, Toast.LENGTH_SHORT).show();
    }

    static void alertShort(Context c, String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }

    private static final String[] VALUE_TYPES = new String[8];
    static {
        for (int i = 0; i < Utils.VALUE_TYPES.length; i++) {
            Utils.VALUE_TYPES[i] = idpwmemo.Value.typeName(i);
        }
    }
    static final List<String> VALUE_TYPE_LIST = Collections.unmodifiableList(Arrays.asList(Utils.VALUE_TYPES));

    static boolean isValidValueType(int valueType) {
        return Utils.inSize(valueType, VALUE_TYPES.length);
    }

    @android.annotation.SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyMMdd:HHmm");

    static String formatDate(long time) {
        if (time == 0L) {
            return "000000:0000";
        }
        return DATE_FMT.format(new java.util.Date(time));
    }

    static void hideInputMethod(Context context, TextView... views) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            for (TextView view : views) {
                if (imm.isActive(view) && view.isInTouchMode()) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
    }

}