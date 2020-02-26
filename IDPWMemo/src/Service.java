
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class Service
{
    Value[] values;
    byte[] secrets;

    Service(String serviceName)
    {
        this(new Value[]{ new Value(Value.SERVICE_NAME, serviceName) }, new byte[0]);
    }

    Service(Value[] values, byte[] secrets)
    {
        this.values = values;
        this.secrets = secrets;
    }

    String getServiceName()
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

    public String toString()
    {
        return getServiceName();
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
        out.writeInt(values.length);
        for (int i = 0; i < values.length; i++)
        {
            values[i].save(out);
        }
        out.writeInt(secrets.length);
        out.write(secrets);
    }
}