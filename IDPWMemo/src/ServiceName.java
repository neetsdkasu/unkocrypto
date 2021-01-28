import java.text.SimpleDateFormat;
import java.util.Date;

import idpwmemo.Service;


class ServiceName
{
    static final SimpleDateFormat fmt =
        new SimpleDateFormat("yyMMdd:HHmm");

    final String serviceName;
    final String value;

    ServiceName(Service service)
    {
        serviceName = service.getServiceName();
        long time = service.getTime();
        if (time == 0L)
        {
            value = "[000000:0000] "
                + serviceName;
        }
        else
        {
            value = "[" +
                fmt.format(new Date(service.getTime()))
                + "] " + serviceName;
        }
    }

    @Override
    public String toString()
    {
        return value;
    }
}