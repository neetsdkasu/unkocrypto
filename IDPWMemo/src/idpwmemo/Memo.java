package idpwmemo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

final class Memo
{
    static final int VERSION = 1;

    static final Service[] EMPTY_SERVICES = new Service[0];

    Service[] services;

    Memo()
    {
        services = EMPTY_SERVICES;
    }

    Memo(Service[] services)
    {
        this.services = services;
    }

    int getServiceCount()
    {
        return services.length;
    }

    void setServices(Service[] services)
    {
        this.services = services == null
                      ? Memo.EMPTY_SERVICES
                      : services;
    }

    int addService(Service newService)
    {
        Service[] tmp = new Service[services.length + 1];
        System.arraycopy(services, 0, tmp, 0, services.length);
        tmp[tmp.length - 1] = newService;
        services = tmp;
        return tmp.length - 1;
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

    Service[] getFilteredServices()
    {
        Service[] tmp = new Service[services.length];
        int count = 0;
        for (int i = 0; i < services.length; i++)
        {
            if (services[i].isValidState())
            {
                tmp[count] = services[i];
                count++;
            }
        }
        if (count == 0)
        {
            tmp = null;
            return EMPTY_SERVICES;
        }
        if (count == services.length)
        {
            tmp = null;
            return services;
        }
        Service[] ret = new Service[count];
        System.arraycopy(tmp, 0, ret, 0, count);
        tmp = null;
        return ret;
    }

    void save(DataOutput out) throws IOException
    {
        out.writeInt(VERSION);
        Service[] filtered = getFilteredServices();
        out.writeInt(filtered.length);
        for (int i = 0; i < filtered.length; i++)
        {
            filtered[i].save(out);
        }
        filtered = null;
    }
}