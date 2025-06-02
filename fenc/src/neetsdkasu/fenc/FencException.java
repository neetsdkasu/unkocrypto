package neetsdkasu.fenc;

public final class FencException extends java.lang.RuntimeException {
    public static enum Cause {
        NO_SIGNATURE,
        UNSUPPORTED_VERSION,
        WRONG_FORMAT_FILENAME,
        UNMATCHED_SIZES,
        UNMATCHED_CHECKSUM,
        WRONG_PASSWORD
    }
    
    public final Cause cause;

    FencException(final Cause cause) {
        super(cause.toString());
        this.cause = cause;
    }

    FencException(final Cause cause, final Exception detail) {
        super(cause.toString(), detail);
        this.cause = cause;
    }

    FencException(final Cause cause, final String additional) {
        super(cause.toString() + " : " + String.valueOf(additional));
        this.cause = cause;
    }

}