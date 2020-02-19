
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

final class Value
{
    // Value Types
    static final int SERVICE_NAME      = 0;
    static final int SERVICE_URL       = 1;
    static final int ID                = 2;
    static final int PASSWORD          = 3;
    static final int EMAIL             = 4;
    static final int REMINDER_QUESTION = 5;
    static final int REMINDER_ANSWER   = 6;
    static final int DESCTIPTION       = 7;

    static String typeName(int type)
    {
        switch (type)
        {
        case SERVICE_NAME:
            return "service name";
        case SERVICE_URL:
            return "service url";
        case ID:
            return "id";
        case PASSWORD:
            return "password";
        case EMAIL:
            return "email";
        case REMINDER_QUESTION:
            return "reminder question";
        case REMINDER_ANSWER:
            return "reminder answer";
        case DESCTIPTION:
            return "description";
        default:
            return "unknown";
        }
    }

    String getTypeName()
    {
        return typeName((int)type);
    }

    void setDefaultVisible()
    {
        switch ((int)type)
        {
        case PASSWORD:
        case EMAIL:
        case REMINDER_QUESTION:
        case REMINDER_ANSWER:
            visible = false;
            break;
        // case SERVICE_NAME:
        // case SERVICE_URL:
        // case ID:
        // case DESCTIPTION:
        default:
            visible = true;
        }
    }

    byte    type;
    boolean visible;
    String  value;

    Value(int type, String value)
    {
        set((byte)type, value);
    }

    Value(byte type, String value)
    {
        set(type, value);
    }

    void set(int type, String value)
    {
        set((byte)type, value);
    }

    void set(byte type, String value)
    {
        this.type = type;
        this.value = value;
    }

    static Value load(DataInput in) throws IOException
    {
        byte type = in.readByte();
        String value = in.readUTF();
        return new Value(type, value);
    }

    void save(DataOutput out) throws IOException
    {
        out.writeByte(type);
        out.writeUTF(value);
    }
}