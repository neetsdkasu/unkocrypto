
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class Memo
{
    static final int VERSION = 1;

    Service[] services;

    Memo()
    {
        services = new Service[0];
    }

    Memo(Service[] services)
    {
        this.services = services;
    }

    int getServiceCount()
    {
        return services.length;
    }

    void addService(Service newService)
    {
        Service[] tmp = new Service[services.length + 1];
        System.arraycopy(services, 0, tmp, 0, services.length);
        tmp[tmp.length - 1] = newService;
        services = tmp;
    }

    Service getService(int index)
    {
        return services[index];
    }

    void setService(int index, Service service)
    {
        services[index] = service;
    }

    void removeService(int index)
    {
        for (int i = index + 1; i < services.length; i++)
        {
            services[i - 1] = services[i];
        }
        Service[] tmp = new Service[services.length - 1];
        System.arraycopy(services, 0, tmp, 0, tmp.length);
        services = tmp;
    }

    static Memo load(DataInput in) throws IOException
    {
        int version = in.readInt();
        if (version > VERSION)
        {
            throw new IOException("invalid version");
        }
        int count = in.readInt();
        Service[] services = new Service[count];
        for (int i = 0; i < count; i++)
        {
            services[i] = Service.load(in);
        }
        return new Memo(services);
    }

    void save(DataOutput out) throws IOException
    {
        out.writeInt(VERSION);
        out.writeInt(services.length);
        for (int i = 0; i < services.length; i++)
        {
            services[i].save(out);
        }
    }
}