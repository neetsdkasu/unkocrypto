package idpwmemo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

final class Memo
{
    static final int VERSION = 2;

    static final Service[] EMPTY_SERVICES = new Service[0];

    int loadedVersion;
    Service[] services;

    Memo()
    {
        loadedVersion = VERSION;
        services = EMPTY_SERVICES;
    }

    Memo(int loadedVersion, Service[] services)
    {
        this.loadedVersion = loadedVersion;
        this.services = services;
    }

    int getLoadedVersion()
    {
        return loadedVersion;
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

    Service[] getServices()
    {
        return services;
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
        if (version < 0 || version > VERSION)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_UNKNOWN_MEMO_VERSION);
        }
        int count = in.readInt();
        if (count < 0)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_INVALID_DATA);
        }
        Service[] services = new Service[count];
        if (version == 1)
        {
            for (int i = 0; i < count; i++)
            {
                services[i] = Service.loadV1(in);
            }
        }
        else
        {
            for (int i = 0; i < count; i++)
            {
                services[i] = Service.load(in);
            }
        }
        return new Memo(version, services);
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