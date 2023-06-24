package idpwmemo;

public class IDPWMemoException extends RuntimeException {
    public static final int CAUSE_WRAPPED_EXCEPTION    = -1; // almost DATA is not IDPWMemo format
    public static final int CAUSE_UNKNOWN              = 0;  // almost IDPWMemo's bug
    public static final int CAUSE_UNKNOWN_MEMO_VERSION = 1;  // almost DATA is not IDPWMemo format
    public static final int CAUSE_BROKEN_SECRETS       = 2;  // almost IDPWMemo's bug
    public static final int CAUSE_NOT_SET_PASSWORD     = 3;  // almost user program's bug
    public static final int CAUSE_NOT_SET_MEMO         = 4;  // almost user program's bug
    public static final int CAUSE_NOT_SELECT_SERVICE   = 5;  // almost user program's bug

    private static String causeToText(int cause) {
        switch (cause) {
            case CAUSE_WRAPPED_EXCEPTION:
                return "WRAPPED EXCEPTION";
            case CAUSE_UNKNOWN:
                return "UNKNOWN";
            case CAUSE_UNKNOWN_MEMO_VERSION:
                return "UNKNOWN MEMO VERSION";
            case CAUSE_BROKEN_SECRETS:
                return "BROKEN SECRETS";
            case CAUSE_NOT_SET_PASSWORD:
                return "NOT SET PASSWORD";
            case CAUSE_NOT_SET_MEMO:
                return "NOT SET MEMO";
            case CAUSE_NOT_SELECT_SERVICE:
                return "NOT SELECT SERVICE";
            default:
                return "UNKNOWN (" + cause + ")";
        }
    }

    private final int cause;
    private final String causeText;
    private final Exception ex;

    IDPWMemoException(int cause) {
        super(causeToText(cause));
        this.cause = cause;
        this.causeText = causeToText(cause);
        this.ex = null;
    }

    IDPWMemoException(Exception ex) {
        super(ex.toString());
        this.cause = CAUSE_WRAPPED_EXCEPTION;
        this.causeText = causeToText(this.cause);
        this.ex = ex;
    }

    public Exception getWrappedException() {
        return this.ex;
    }

    public int getIDPWMemoCause() {
        return this.cause;
    }

    public String getIDPWMemoCauseText() {
        return causeText;
    }
}