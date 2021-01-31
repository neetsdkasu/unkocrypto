package idpwmemo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class IDPWMemo
{
    public static byte[] getBytes(String s)  throws IOException
    {
        return Cryptor.getBytes(s);
    }

    Cryptor cryptor = Cryptor.instance;
    byte[] encodedPasswordV1 = null;
    Memo memo = null;
    int serviceIndex = -1;
    Service service = null;
    Value[] secrets = null;

    public IDPWMemo() {}

    public void clear()
    {
        encodedPasswordV1 = null;
        memo = null;
        serviceIndex = -1;
        service = null;
        secrets = null;
    }

    public void setPassword(String password) throws IOException
    {
        setPassword(getBytes(password));
    }

    public void setPassword(byte[] password) throws IOException
    {
        byte[] tmp = cryptor.encryptV1(Service.EMPTY_BYTES, password);
        encodedPasswordV1 = tmp;
        memo = null;
        serviceIndex = -1;
        service = null;
        secrets = null;
    }

    byte[] getPassword() throws IOException
    {
        if (encodedPasswordV1 == null)
        {
            throw new RuntimeException("no password");
        }
        return cryptor.decryptV1(Service.EMPTY_BYTES, encodedPasswordV1);
    }

    public void newMemo()
    {
        if (encodedPasswordV1 == null)
        {
            throw new RuntimeException("no password");
        }
        memo = new Memo();
        serviceIndex = -1;
        service = null;
        secrets = null;
    }

    public boolean loadMemo(byte[] src) throws IOException
    {
        byte[] password = getPassword();
        byte[] buf = cryptor.decryptRepeatV1(2, password, src);
        password = null;
        if (buf == null)
        {
            return false;
        }
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buf));
        buf = null;
        Memo tmp = Memo.load(dis);
        dis.close();
        memo = tmp;
        serviceIndex = -1;
        service = null;
        secrets = null;
        return true;
    }

    public int getServiceCount()
    {
        if (memo == null)
        {
            throw new RuntimeException("no memo");
        }
        return memo.getServiceCount();
    }

    public Service[] getServices()
    {
        if (memo == null)
        {
            throw new RuntimeException("no memo");
        }
        return memo.getServices();
     }

    public Service getService(int index)
    {
        if (memo == null)
        {
            throw new RuntimeException("no memo");
        }
        return memo.getService(index);
    }

    public Service getService()
    {
        if (serviceIndex < 0)
        {
            throw new RuntimeException("not select service");
        }
        return memo.getService(serviceIndex);
    }

    public Service getSelectedService() throws IOException
    {
        if (service == null)
        {
            throw new RuntimeException("not select service");
        }
        saveSecrets();
        return service;
    }

    public String getSelectedServiceName()
    {
        if (service == null)
        {
            throw new RuntimeException("not select service");
        }
        return service.getServiceName();
    }

    public String[] getServiceNames()
    {
        if (memo == null)
        {
            throw new RuntimeException("no memo");
        }
        String[] names = new String[memo.getServiceCount()];
        for (int i = 0; i < names.length; i++)
        {
            names[i] = memo.getService(i).getServiceName();
        }
        return names;
    }

    public void setServices(Service[] services)
    {
        if (memo == null)
        {
            throw new RuntimeException("no memo");
        }
        memo.setServices(services);
        serviceIndex = -1;
        service = null;
        secrets = null;
    }

    public void addService(IDPWMemo serviceFrom) throws IOException
    {
        if (serviceFrom == null)
        {
            throw new IllegalArgumentException("serviceFrom is null");
        }
        if (memo == null)
        {
            throw new RuntimeException("no memo");
        }
        service = serviceFrom.getSelectedService().getCopy();
        setSecrets(serviceFrom.getSecrets());
        saveSecrets();
        serviceIndex = memo.addService(service.getCopy());
        secrets = null;
    }

    public void setService(int index, IDPWMemo serviceFrom) throws IOException
    {
        if (serviceFrom == null)
        {
            throw new IllegalArgumentException("serviceFrom is null");
        }
        if (memo == null)
        {
            throw new RuntimeException("no memo");
        }
        if (index < 0 || index >= memo.getServiceCount())
        {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        service = serviceFrom.getSelectedService().getCopy();
        setSecrets(serviceFrom.getSecrets());
        saveSecrets();
        serviceIndex = index;
        memo.setService(index, service.getCopy());
        secrets = null;
    }

    public void addNewService(String serviceName)
    {
        if (memo == null)
        {
            throw new RuntimeException("no memo");
        }
        service = new Service(serviceName);
        serviceIndex = memo.addService(service.getCopy());
        secrets = null;
    }

    public void selectService(int index)
    {
        if (memo == null)
        {
            throw new RuntimeException("no memo");
        }
        if (index < 0 || index >= memo.getServiceCount())
        {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        serviceIndex = index;
        service = memo.getService(index).getCopy();
        secrets = null;
    }

    public Value[] getValues()
    {
        if (service == null)
        {
            throw new RuntimeException("not select service");
        }
        return service.getValues();
    }

    public void setValues(Value[] values)
    {
        if (service == null)
        {
            throw new RuntimeException("not select service");
        }
        service.setValues(values);
    }

    public void setSecrets(Value[] values)
    {
        if (service == null)
        {
            throw new RuntimeException("not select service");
        }
        secrets = values == null
                ? Service.EMPTY_VALUES
                : values;
    }

    public Value[] getSecrets() throws IOException
    {
        if (secrets != null)
        {
            return secrets;
        }
        if (service == null)
        {
            throw new RuntimeException("not select service");
        }
        byte[] src = service.getSecrets();
        if (src == null || src.length == 0)
        {
            secrets = Service.EMPTY_VALUES;
            return secrets;
        }
        byte[] password = getPassword();
        byte[] buf = cryptor.decryptRepeatV1(2, password, src);
        password = null;
        src = null;
        if (buf == null)
        {
            throw new  RuntimeException("secrets data is broken");
        }
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buf));
        buf = null;
        Value[] tmp = Service.readSecrets(dis);
        dis.close();
        secrets = tmp;
        tmp = null;
        return secrets;
    }

    public void removeService(int index)
    {
        if (memo == null)
        {
            throw new RuntimeException("no memo");
        }
        if (index < 0 || index >= memo.getServiceCount())
        {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        if (serviceIndex == index)
        {
            removeSelectedService();
            return;
        }
        if (index < serviceIndex)
        {
            serviceIndex--;
        }
        memo.removeService(index);
    }

    public void updateSelectedService() throws IOException
    {
        if (service == null)
        {
            throw new RuntimeException("not select service");
        }
        saveSecrets();
        service.setTime(System.currentTimeMillis());
        memo.setService(serviceIndex, service.getCopy());
    }

    public void removeSelectedService()
    {
        if (service == null)
        {
            throw new RuntimeException("not select service");
        }
        memo.removeService(serviceIndex);
        serviceIndex = -1;
        service = null;
        secrets = null;
    }

    void saveSecrets() throws IOException
    {
        if (secrets == null)
        {
            return;
        }
        Value[] filtered = Service.filterValues(secrets);
        if (filtered.length == 0)
        {
            service.setSecrets(null);
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        Service.writeSecrets(dos, filtered);
        filtered = null;
        dos.flush();
        byte[] buf = baos.toByteArray();
        dos.close();
        byte[] password = getPassword();
        service.setSecrets(cryptor.encryptRepeatV1(2, password, buf));
        password = null;
        buf = null;
    }

    public byte[] save() throws IOException
    {
        if (memo == null)
        {
            throw new RuntimeException("no memo");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        memo.save(dos);
        dos.flush();
        byte[] buf = baos.toByteArray();
        dos.close();
        return cryptor.encryptRepeatV1(2, getPassword(), buf);
    }

    public void changePassword(String newPassword) throws IOException
    {
        changePassword(Cryptor.getBytes(newPassword));
    }

    public void changePassword(byte[] newPassword) throws IOException
    {
        if (memo == null)
        {
            throw new RuntimeException("no memo");
        }
        byte[] oldPassword = getPassword();
        byte[][] encs = new byte[memo.getServiceCount()][];
        for (int i = 0; i < memo.getServiceCount(); i++)
        {
            Service sv = memo.getService(i);
            if (!sv.hasSecrets())
            {
                encs[i] = Service.EMPTY_BYTES;
                continue;
            }
            byte[] sec = sv.getSecrets();
            byte[] dec = cryptor.decryptRepeatV1(2, oldPassword, sec);
            sec = null;
            encs[i] = cryptor.encryptRepeatV1(2, newPassword, dec);
            dec = null;
        }
        byte[] tmp = cryptor.encryptV1(Service.EMPTY_BYTES, newPassword);
        if (service != null && service.hasSecrets())
        {
            byte[] sec = service.getSecrets();
            byte[] dec = cryptor.decryptRepeatV1(2, oldPassword, sec);
            sec = null;
            byte[] enc = cryptor.encryptRepeatV1(2, newPassword, dec);
            dec = null;
            service.setSecrets(enc);
            enc = null;
        }
        encodedPasswordV1 = tmp;
        for (int i = 0; i < memo.getServiceCount(); i++)
        {
            memo.getService(i).setSecrets(encs[i]);
        }
    }
}