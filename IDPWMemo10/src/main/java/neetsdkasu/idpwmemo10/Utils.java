package neetsdkasu.idpwmemo10;

import android.content.Context;
import android.widget.Toast;
import java.io.File;
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

    static void alertShort(Context c, int msgResId) {
        Toast.makeText(c, msgResId, Toast.LENGTH_SHORT).show();
    }

    static void alertShort(Context c, String msg) {
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show();
    }
}