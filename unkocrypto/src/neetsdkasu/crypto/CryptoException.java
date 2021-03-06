package neetsdkasu.crypto;

public final class CryptoException extends RuntimeException
{
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_INVALID_COUNT = 1;
    public static final int TYPE_INVALID_DATA = 2;
    public static final int TYPE_INVALID_CHECKSUM = 3;
    public static final int TYPE_INVALID_DATASIZE = 4;

    private static String makeTypeMessage(int type)
    {
        switch (type)
        {
        case TYPE_UNKNOWN:
            return "UNKNOWN";
        case TYPE_INVALID_COUNT:
            return "INVALID COUNT";
        case TYPE_INVALID_DATA:
            return "INVALID DATA";
        case TYPE_INVALID_CHECKSUM:
            return "INVALID CHECKSUM";
        case TYPE_INVALID_DATASIZE:
            return "INVALID DATASIZE";
        default:
            return "UNKNOWN (" + type + ")";
        }
    }

    private final int type;

    public CryptoException(int type)
    {
        super(makeTypeMessage(type));
        this.type = type;
    }

    public CryptoException(Exception ex)
    {
        // super(ex);
        super(ex.toString()); // CLDC/MIDP
        type = TYPE_UNKNOWN;
    }

    public int getType()
    {
        return type;
    }
}