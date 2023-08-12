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
    byte[] encodedPasswordV2 = null;
    int version = 0;
    Memo memo = null;
    int serviceIndex = -1;
    Service service = null;
    Value[] secrets = null;

    public IDPWMemo() {}

    public void clear()
    {
        encodedPasswordV1 = null;
        encodedPasswordV2 = null;
        version = 0;
        memo = null;
        serviceIndex = -1;
        service = null;
        secrets = null;
    }

    public void setPassword(String password)
    {
        try
        {
            setPassword(getBytes(password));
        }
        catch (IOException ex)
        {
            throw new IDPWMemoException(ex);
        }
    }

    public void setPassword(byte[] password)
    {
        try
        {
            byte[] tmp1 = cryptor.encryptV1(Service.EMPTY_BYTES, password);
            byte[] tmp2 = cryptor.encryptV2(password, password);
            encodedPasswordV1 = tmp1;
            encodedPasswordV2 = tmp2;
            version = 0;
            memo = null;
            serviceIndex = -1;
            service = null;
            secrets = null;
        }
        catch (IOException ex)
        {
            throw new IDPWMemoException(ex);
        }
    }

    byte[] getPasswordV1() throws IOException
    {
        if (encodedPasswordV1 == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SET_PASSWORD);
        }
        return cryptor.decryptV1(Service.EMPTY_BYTES, encodedPasswordV1);
    }

    public void newMemo()
    {
        newMemo(2);
    }

    public void newMemo(int version)
    {
        if (version < 1 || version > 2)
        {
            throw new IllegalArgumentException("version " + version + " is unsupported (supported version 1 or 2)");
        }
        if (encodedPasswordV1 == null || encodedPasswordV2 == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SET_PASSWORD);
        }
        this.version = version;
        memo = new Memo();
        serviceIndex = -1;
        service = null;
        secrets = null;
    }

    public boolean loadMemo(byte[] src)
    {
        try
        {
            byte[] buf = null;
            int st = Cryptor.checkSrcType(src);
            if ((st&2) != 0)
            {
                buf = cryptor.decryptV2(encodedPasswordV2, src);
            }
            if (buf != null)
            {
                version = 2;
            }
            else if ((st&1) != 0)
            {
                byte[] password = getPasswordV1();
                buf = cryptor.decryptRepeatV1(2, password, src);
                password = null;
                if (buf == null)
                {
                    return false;
                }
                version = 1;
            }
            else
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
        catch (IOException ex)
        {
            throw new IDPWMemoException(ex);
        }
    }

    public boolean isIdle()
    {
        return encodedPasswordV1 == null || encodedPasswordV2 == null;
    }

    public boolean hasMemo()
    {
        return memo != null;
    }

    public int getSelectedServiceIndex()
    {
        return serviceIndex;
    }

    public boolean hasSelectedService()
    {
        return serviceIndex >= 0;
    }

    public int getServiceCount()
    {
        if (memo == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SET_MEMO);
        }
        return memo.getServiceCount();
    }

    public Service[] getServices()
    {
        if (memo == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SET_MEMO);
        }
        return memo.getServices();
     }

    public Service getService(int index)
    {
        if (memo == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SET_MEMO);
        }
        return memo.getService(index);
    }

    public Service getService()
    {
        if (serviceIndex < 0)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SELECT_SERVICE);
        }
        return memo.getService(serviceIndex);
    }

    public Service getSelectedService()
    {
        if (service == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SELECT_SERVICE);
        }
        try
        {
            saveSecrets();
            return service;
        }
        catch (IOException ex)
        {
            throw new IDPWMemoException(ex);
        }
    }

    public String getSelectedServiceName()
    {
        if (service == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SELECT_SERVICE);
        }
        return service.getServiceName();
    }

    public String[] getServiceNames()
    {
        if (memo == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SET_MEMO);
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
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SET_MEMO);
        }
        memo.setServices(services);
        serviceIndex = -1;
        service = null;
        secrets = null;
    }

    public void addService(IDPWMemo serviceFrom)
    {
        if (serviceFrom == null)
        {
            throw new IllegalArgumentException("serviceFrom is null");
        }
        if (memo == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SET_MEMO);
        }
        try
        {
            service = serviceFrom.getSelectedService().getCopy();
            Value[] srcSecrets = serviceFrom.getSecrets();
            Value[] tmpSecrets = new Value[srcSecrets.length];
            for (int i = 0; i < srcSecrets.length; i++)
            {
                tmpSecrets[i] = srcSecrets[i].getCopy();
            }
            setSecrets(tmpSecrets);
            saveSecrets();
            serviceIndex = memo.addService(service.getCopy());
            secrets = null;
        }
        catch (IOException ex)
        {
            throw new IDPWMemoException(ex);
        }
    }

    public void setService(int index, IDPWMemo serviceFrom)
    {
        if (serviceFrom == null)
        {
            throw new IllegalArgumentException("serviceFrom is null");
        }
        if (memo == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SET_MEMO);
        }
        if (index < 0 || index >= memo.getServiceCount())
        {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        try
        {
            service = serviceFrom.getSelectedService().getCopy();
            Value[] srcSecrets = serviceFrom.getSecrets();
            Value[] tmpSecrets = new Value[srcSecrets.length];
            for (int i = 0; i < srcSecrets.length; i++)
            {
                tmpSecrets[i] = srcSecrets[i].getCopy();
            }
            setSecrets(tmpSecrets);
            saveSecrets();
            serviceIndex = index;
            memo.setService(index, service.getCopy());
            secrets = null;
        }
        catch (IOException ex)
        {
            throw new IDPWMemoException(ex);
        }
    }

    public void addNewService(String serviceName)
    {
        if (memo == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SET_MEMO);
        }
        service = new Service(serviceName);
        serviceIndex = memo.addService(service.getCopy());
        secrets = null;
    }

    public void selectService(int index)
    {
        if (memo == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SET_MEMO);
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
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SELECT_SERVICE);
        }
        return service.getValues();
    }

    public void setValues(Value[] values)
    {
        if (service == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SELECT_SERVICE);
        }
        service.setValues(values);
    }

    public void setSecrets(Value[] values)
    {
        if (service == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SELECT_SERVICE);
        }
        secrets = values == null
                ? Service.EMPTY_VALUES
                : values;
    }

    public Value[] getSecrets()
    {
        if (secrets != null)
        {
            return secrets;
        }
        if (service == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SELECT_SERVICE);
        }
        try
        {
            byte[] src = service.getSecrets();
            if (src == null || src.length == 0)
            {
                secrets = Service.EMPTY_VALUES;
                return secrets;
            }
            byte[] buf = null;
            if (version == 1)
            {
                byte[] password = getPasswordV1();
                buf = cryptor.decryptRepeatV1(2, password, src);
                password = null;
            }
            else if (version == 2)
            {
                buf = cryptor.decryptV2(encodedPasswordV2, src);
            }
            src = null;
            if (buf == null)
            {
                throw new IDPWMemoException(IDPWMemoException.CAUSE_BROKEN_SECRETS);
            }
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buf));
            buf = null;
            Value[] tmp = Service.readSecrets(dis);
            dis.close();
            secrets = tmp;
            tmp = null;
            return secrets;
        }
        catch (IOException ex)
        {
            throw new IDPWMemoException(ex);
        }
    }

    public void removeService(int index)
    {
        if (memo == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SET_MEMO);
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

    public void updateSelectedService()
    {
        if (service == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SELECT_SERVICE);
        }
        try
        {
            saveSecrets();
            service.setTime(System.currentTimeMillis());
            memo.setService(serviceIndex, service.getCopy());
        }
        catch (IOException ex)
        {
            throw new IDPWMemoException(ex);
        }
    }

    public void removeSelectedService()
    {
        if (service == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SELECT_SERVICE);
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
        if (version == 1)
        {
            byte[] password = getPasswordV1();
            service.setSecrets(cryptor.encryptRepeatV1(2, password, buf));
            password = null;
        }
        else if (version == 2)
        {
            service.setSecrets(cryptor.encryptV2(encodedPasswordV2, buf));
        }
        buf = null;
    }

    public byte[] save()
    {
        if (memo == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SET_MEMO);
        }
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            memo.save(dos);
            dos.flush();
            byte[] buf = baos.toByteArray();
            dos.close();
            if (version == 1)
            {
                return cryptor.encryptRepeatV1(2, getPasswordV1(), buf);
            }
            else if (version == 2)
            {
                return cryptor.encryptV2(encodedPasswordV2, buf);
            }
            else
            {
                throw new IDPWMemoException(IDPWMemoException.CAUSE_UNKNOWN);
            }
        }
        catch (IOException ex)
        {
            throw new IDPWMemoException(ex);
        }
    }

    public void changePassword(String newPassword)
    {
        try
        {
            changePassword(Cryptor.getBytes(newPassword));
        }
        catch (IOException ex)
        {
            throw new IDPWMemoException(ex);
        }
    }

    public void changePassword(byte[] newPassword)
    {
        if (memo == null)
        {
            throw new IDPWMemoException(IDPWMemoException.CAUSE_NOT_SET_MEMO);
        }
        try
        {
            if (version == 1)
            {
                changePasswordV1(newPassword);
            }
            else if (version == 2)
            {
                changePasswordV2(newPassword);
            }
            else
            {
                throw new IDPWMemoException(IDPWMemoException.CAUSE_UNKNOWN);
            }
        }
        catch (IOException ex)
        {
            throw new IDPWMemoException(ex);
        }
    }

    private void changePasswordV1(byte[] newPassword) throws IOException
    {
        byte[] oldPassword = getPasswordV1();
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
        byte[] tmp1 = cryptor.encryptV1(Service.EMPTY_BYTES, newPassword);
        byte[] tmp2 = cryptor.encryptV2(newPassword, newPassword);
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
        encodedPasswordV1 = tmp1;
        encodedPasswordV2 = tmp2;
        for (int i = 0; i < memo.getServiceCount(); i++)
        {
            memo.getService(i).setSecrets(encs[i]);
        }
        oldPassword = null;
        tmp1 = null;
        tmp2 = null;
        encs = null;
    }

    private void changePasswordV2(byte[] newPassword) throws IOException
    {
        byte[] tmpPasswordV2 = cryptor.encryptV2(newPassword, newPassword);
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
            byte[] dec = cryptor.decryptV2(encodedPasswordV2, sec);
            sec = null;
            encs[i] = cryptor.encryptV2(tmpPasswordV2, dec);
            dec = null;
        }
        byte[] tmpPasswordV1 = cryptor.encryptV1(Service.EMPTY_BYTES, newPassword);
        if (service != null && service.hasSecrets())
        {
            byte[] sec = service.getSecrets();
            byte[] dec = cryptor.decryptV2(encodedPasswordV2, sec);
            sec = null;
            byte[] enc = cryptor.encryptV2(tmpPasswordV2, dec);
            dec = null;
            service.setSecrets(enc);
            enc = null;
        }
        encodedPasswordV1 = tmpPasswordV1;
        encodedPasswordV2 = tmpPasswordV2;
        for (int i = 0; i < memo.getServiceCount(); i++)
        {
            memo.getService(i).setSecrets(encs[i]);
        }
        tmpPasswordV1 = null;
        tmpPasswordV2 = null;
        encs = null;
    }

    public void convertV1ToV2()
    {
        if (version != 1)
        {
            return;
        }
        try
        {
            byte[] oldPassword = getPasswordV1();
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
                encs[i] = cryptor.encryptV2(encodedPasswordV2, dec);
                dec = null;
            }
            if (service != null && service.hasSecrets())
            {
                byte[] sec = service.getSecrets();
                byte[] dec = cryptor.decryptRepeatV1(2, oldPassword, sec);
                sec = null;
                byte[] enc = cryptor.encryptV2(encodedPasswordV2, dec);
                dec = null;
                service.setSecrets(enc);
                enc = null;
            }
            for (int i = 0; i < memo.getServiceCount(); i++)
            {
                memo.getService(i).setSecrets(encs[i]);
            }
            version = 2;
            oldPassword = null;
            encs = null;
        }
        catch (IOException ex)
        {
            throw new IDPWMemoException(ex);
        }
    }
}