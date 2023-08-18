package idpwmemo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class Value
{
    // Value Types
    public static final int SERVICE_NAME      = 0;
    public static final int SERVICE_URL       = 1;
    public static final int ID                = 2;
    public static final int PASSWORD          = 3;
    public static final int EMAIL             = 4;
    public static final int REMINDER_QUESTION = 5;
    public static final int REMINDER_ANSWER   = 6;
    public static final int DESCRIPTION       = 7;

    public static boolean isValidType(int type) {
        return 0 <= type && type <= 7;
    }

    public static String typeName(int type)
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
        case DESCRIPTION:
            return "description";
        default:
            return "unknown";
        }
    }

    public String getTypeName()
    {
        return typeName((int)type);
    }

    public byte    type;
    public String  value;

    public Value(int type, String value)
    {
        set((byte)type, value);
    }

    public Value(byte type, String value)
    {
        set(type, value);
    }

    public void set(int type, String value)
    {
        set((byte)type, value);
    }

    public void set(byte type, String value)
    {
        this.type = type;
        this.value = value;
    }

    public Value getCopy()
    {
        return new Value(type, value);
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!Value.class.isInstance(o))
        {
            return false;
        }
        Value v = (Value)o;
        return type == v.type && value.equals(v.value);
    }

    public boolean isEmpty()
    {
        return value == null || value.trim().length() == 0;
    }

    static Value load(DataInput in) throws IOException
    {
        byte type = in.readByte();
        if (!Value.isValidType((int)type))
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_INVALID_DATA);
        }
        String value = in.readUTF();
        return new Value(type, value);
    }

    void save(DataOutput out) throws IOException
    {
        out.writeByte(type);
        out.writeUTF(value);
    }
}