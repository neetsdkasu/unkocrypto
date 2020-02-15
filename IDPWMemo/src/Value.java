
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

final class Value
{
    // Value Types
    static final byte SERVICE_NAME      = 0;
    static final byte SERVICE_URL       = 1;
    static final byte ID                = 1 << 3; // lower 3 bits (8 Ids) for difference of some purpose IDs (login ID, user ID, user number , user name, etc...)
    static final byte PASSWORD          = 2 << 3; // lower 3 bits (8 passwords) for multiple passwords (2nd password, secret number, etc...)
    static final byte MAIL              = 3 << 3; // one or more mail addresses registered to the service
    static final byte REMINDER_QUESTION = 4 << 3;
    static final byte REMINDER_ANSWER   = 5 << 3;
    static final byte DESCTIPTION       = 6 << 3;

    String typeName()
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
        case MAIL:
            return "mail";
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
    
    void setDefaultVisible()
    {
        switch (type)
        {
        case PASSWORD:
        case MAIL:
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