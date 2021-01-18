package idpwmemo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class Service
{
    static final Value[] EMPTY_VALUES = new Value[0];
    static final byte[] EMPTY_BYTES = new byte[0];

    private Value[] values;
    private byte[] secrets;

    public Service(String serviceName)
    {
        this(new Value[]{ new Value(Value.SERVICE_NAME, serviceName) }, EMPTY_BYTES);
    }

    Service(Value[] values, byte[] secrets)
    {
        this.values = values;
        this.secrets = secrets;
    }

    public String getServiceName()
    {
        for (int i = 0; i < values.length; i++)
        {
            if (values[i].type == Value.SERVICE_NAME)
            {
                return values[i].value;
            }
        }
        return "";
    }

    public boolean isValidState()
    {
        return getServiceName().trim().length() > 0;
    }

    public String toString()
    {
        return getServiceName();
    }

    public Service getCopy()
    {
        Service copy = new Service(
            new Value[values.length],
            secrets.length == 0
                ? EMPTY_BYTES
                : new byte[secrets.length]
        );
        for (int i = 0; i < values.length; i++)
        {
            copy.values[i] = values[i].getCopy();
        }
        if (secrets.length > 0)
        {
            System.arraycopy(secrets, 0, copy.secrets, 0, secrets.length);
        }
        return copy;
    }

    public Value[] getValues()
    {
        return values;
    }

    public void setValues(Value[] values)
    {
        this.values = values == null
                    ? EMPTY_VALUES
                    : values;
    }

    byte[] getSecrets()
    {
        return secrets;
    }

    void setSecrets(byte[] secrets)
    {
        this.secrets = secrets == null
                     ? EMPTY_BYTES
                     : secrets;
    }

    boolean hasSecrets()
    {
        return secrets != null && secrets.length > 0;
    }

    boolean equalsValues(Value[] values)
    {
        if (this.values == values)
        {
            return true;
        }
        if (this.values == null || values == null)
        {
            return false;
        }
        if (this.values.length != values.length)
        {
            return false;
        }
        for (int i = 0; i < values.length; i++)
        {
            if (!this.values[i].equals(values[i]))
            {
                return false;
            }
        }
        return true;
    }

    boolean equalsSecrets(byte[] secrets)
    {
        if (this.secrets == secrets)
        {
            return true;
        }
        if (this.secrets == null || secrets == null)
        {
            return false;
        }
        if (this.secrets.length != secrets.length)
        {
            return false;
        }
        for (int i = 0; i < secrets.length; i++)
        {
            if (this.secrets[i] != secrets[i])
            {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!Service.class.isInstance(o))
        {
            return false;
        }
        Service s = (Service)o;
        return equalsSecrets(s.secrets)
            && equalsValues(s.values);
    }

    static Value[] filterValues(Value[] values)
    {
         Value[] tmp = new Value[values.length];
         int count = 0;
         for (int i = 0; i < values.length; i++)
         {
             if (!values[i].isEmpty())
             {
                 tmp[count] = values[i];
                 count++;
             }
         }
         if (count == 0)
         {
             tmp = null;
             return new Value[0];
         }
         if (count == values.length)
         {
             tmp = null;
             return values;
         }
         Value[] ret = new Value[count];
         System.arraycopy(tmp, 0, ret, 0, count);
         tmp = null;
         return ret;
    }

    static Value[] readSecrets(DataInput in) throws IOException
    {
        Value[] values;
        int vlen = in.readInt();
        values = new Value[vlen];
        for (int i = 0; i < vlen; i++)
        {
            values[i] = Value.load(in);
        }
        return values;
    }

    static void writeSecrets(DataOutput out, Value[] values) throws IOException
    {
        out.writeInt(values.length);
        for (int i = 0; i < values.length; i++)
        {
            values[i].save(out);
        }
    }

    static Service load(DataInput in) throws IOException
    {
        Value[] values;
        byte[] secrets;
        int vlen = in.readInt();
        values = new Value[vlen];
        for (int i = 0; i < vlen; i++)
        {
            values[i] = Value.load(in);
        }
        int slen = in.readInt();
        secrets = new byte[slen];
        in.readFully(secrets);
        return new Service(values, secrets);
    }

    void save(DataOutput out) throws IOException
    {
        Value[] filtered = filterValues(values);
        out.writeInt(filtered.length);
        for (int i = 0; i < filtered.length; i++)
        {
            filtered[i].save(out);
        }
        filtered = null;
        out.writeInt(secrets.length);
        out.write(secrets);
    }
}