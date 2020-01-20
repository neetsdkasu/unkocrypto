
import java.util.Random;
import java.util.zip.Adler32;
import java.util.zip.Checksum;

import neetsdkasu.crypto.Crypto;
import neetsdkasu.crypto.CryptoException;

public class CryptoTest
{
    public static void main(String[] args)
    {
        long seed = 1234;
        if (args.length > 0)
        {
            seed = Long.parseLong(args[0]);
        }

        byte[] src = {1, 2, 3, 4, 5, 6, 7, 111, 99};

        Checksum checksum = new Adler32();
        Random rand = new Random(seed);

        int size = 32;


        byte[] dst = Crypto.encrypt(size, checksum, rand, src);

        for (int i = 0; i < dst.length; i++)
        {
            System.out.printf("%3d: %02X%n", i, 0xFF & (int)dst[i]);
        }

        Random rand2 = new Random(seed);

        byte[] ret = Crypto.decrypt(size, checksum, rand2, dst, 0);

        for (int i = 0; i < ret.length; i++)
        {
            System.out.printf("%3d: %02X%n", i, 0xFF & (int)ret[i]);
        }
        
        Result res = test();
        System.out.println(res);

    }
    
    private static class Result
    {
        public int count = 0;
        public boolean ok = true;
        public String testName = "";
        public Exception exp = null;
        void next(String testName)
        {
            this.testName = testName;
            count++;
            System.err.printf("[%02d] %s%n", count, testName);
        }
        Result failed(String msg)
        {
            this.exp = new Exception(msg);
            this.ok = false;
            return this;
        }
        Result failed(Exception exp)
        {
            this.exp = exp;
            this.ok = false;
            return this;
        }
        @Override
        public String toString()
        {
            if (ok)
            {
                return String.format("all ok [%2d]", count);
            }
            return String.format(
                "failed [%02d] testName: %s, Exception: %s",
                count,
                String.valueOf(testName),
                String.valueOf(exp)
            );
        }
    }
    
    private static Result test()
    {
        Result res = new Result();
        
        Checksum dummyChecksum = new Adler32();
        Random dummyRandom = new Random();
        byte[] dummySrc = new byte[5];
        
        {
            int min_size = 32;
            res.next("CHECK MIN_SIZE(" + min_size + ")");
            if (Crypto.MIN_SIZE != min_size)
            {
                return res.failed("WRONG SIZE: " + Crypto.MIN_SIZE);
            }
        }
        
        {
            int max_size = 1024;
            res.next("CHECK MAX_SIZE(" + max_size + ")");
            if (Crypto.MAX_SIZE != max_size)
            {
                return res.failed("WRONG SIZE: " + Crypto.MAX_SIZE);
            }
        }
        
        {
            int meta_size = 12;
            res.next("CHECK META_SIZE(" + meta_size + ")");
            if (Crypto.META_SIZE != meta_size)
            {
                return res.failed("WRONG SIZE: " + Crypto.META_SIZE);
            }
        }
        
        {
            int size = Crypto.MIN_SIZE - 1;
            try
            {
                res.next("CHECK encrypt(4) smaller size(" + size + ") than MIN_SIZE(" + Crypto.MIN_SIZE + ")");
                Crypto.encrypt(size, dummyChecksum, dummyRandom, dummySrc);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = String.format(
                    "OUT OF RANGE size(%d): %d <= size <= %d",
                    size, Crypto.MIN_SIZE, Crypto.MAX_SIZE);
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }

        {
            int size = Crypto.MIN_SIZE - 1;
            try
            {
                res.next("CHECK encrypt(6) smaller size(" + size + ") than MIN_SIZE(" + Crypto.MIN_SIZE + ")");
                Crypto.encrypt(size, dummyChecksum, dummyRandom, dummySrc, 0, dummySrc.length);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = String.format(
                    "OUT OF RANGE size(%d): %d <= size <= %d",
                    size, Crypto.MIN_SIZE, Crypto.MAX_SIZE);
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }
        
        {
            int size = Crypto.MAX_SIZE + 1;
            try
            {
                res.next("CHECK encrypt(4) larger size(" + size + ") than MAX_SIZE(" + Crypto.MAX_SIZE + ")");
                Crypto.encrypt(size, dummyChecksum, dummyRandom, dummySrc);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = String.format(
                    "OUT OF RANGE size(%d): %d <= size <= %d",
                    size, Crypto.MIN_SIZE, Crypto.MAX_SIZE);
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }

        {
            int size = Crypto.MAX_SIZE + 1;
            try
            {
                res.next("CHECK encrypt(6) larger size(" + size + ") than MAX_SIZE(" + Crypto.MAX_SIZE + ")");
                Crypto.encrypt(size, dummyChecksum, dummyRandom, dummySrc, 0, dummySrc.length);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = String.format(
                    "OUT OF RANGE size(%d): %d <= size <= %d",
                    size, Crypto.MIN_SIZE, Crypto.MAX_SIZE);
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }


        {
            int size = Crypto.MIN_SIZE;
            try
            {
                res.next("CHECK encrypt(4) checksum is null");
                Crypto.encrypt(size, null, dummyRandom, dummySrc);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = "checksum is null";
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }

        {
            int size = Crypto.MIN_SIZE;
            try
            {
                res.next("CHECK encrypt(6) checksum is null");
                Crypto.encrypt(size, null, dummyRandom, dummySrc, 0, dummySrc.length);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = "checksum is null";
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }
        
        {
            int size = Crypto.MIN_SIZE;
            try
            {
                res.next("CHECK encrypt(4) rand is null");
                Crypto.encrypt(size, dummyChecksum, null, dummySrc);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = "rand is null";
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }

        {
            int size = Crypto.MIN_SIZE;
            try
            {
                res.next("CHECK encrypt(6) rand is null");
                Crypto.encrypt(size, dummyChecksum, null, dummySrc, 0, dummySrc.length);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = "rand is null";
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }

        {
            int size = Crypto.MIN_SIZE;
            try
            {
                res.next("CHECK encrypt(4) src is null");
                Crypto.encrypt(size, dummyChecksum, dummyRandom, null);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = "src is null";
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }

        {
            int size = Crypto.MIN_SIZE;
            try
            {
                res.next("CHECK encrypt(6) src is null");
                Crypto.encrypt(size, dummyChecksum, dummyRandom, null, 0, dummySrc.length);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = "src is null";
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }
        

        {
            int off = -1;
            int size = Crypto.MIN_SIZE;
            try
            {
                res.next("CHECK encrypt(6) invalid off(" + off + ")");
                Crypto.encrypt(size, dummyChecksum, dummyRandom, dummySrc, off, dummySrc.length);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = String.format(
                    "OUT OF RANGE off(%d): 0 <= off < src.length(%d) - len",
                    off, dummySrc.length);
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }
        
        {
            int off = dummySrc.length;
            int size = Crypto.MIN_SIZE;
            try
            {
                res.next("CHECK encrypt(6) invalid off(" + off + "): src.length(" + dummySrc.length + ")");
                Crypto.encrypt(size, dummyChecksum, dummyRandom, dummySrc, off, dummySrc.length);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = String.format(
                    "OUT OF RANGE off(%d): 0 <= off < src.length(%d) - len",
                    off, dummySrc.length);
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }

        {
            int size = Crypto.MIN_SIZE;
            int off = 0;
            int len = -1;
            try
            {
                res.next("CHECK encrypt(6) invalid len(" + len + ")");
                Crypto.encrypt(size, dummyChecksum, dummyRandom, dummySrc, off, len);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = String.format(
                    "OUT OF RANGE len(%d): 0 < len <= min(src.length(%d) - off(%d), size(%d) - %d)",
                    len, dummySrc.length, off, size, Crypto.META_SIZE);
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }
        
        {
            int size = Crypto.MIN_SIZE;
            int off = 0;
            int len = dummySrc.length + 1;
            try
            {
                res.next("CHECK encrypt(6) invalid len(" + len + "): off(" + off + "), src.length(" + dummySrc.length + ")");
                Crypto.encrypt(size, dummyChecksum, dummyRandom, dummySrc, off, len);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = String.format(
                    "OUT OF RANGE len(%d): 0 < len <= min(src.length(%d) - off(%d), size(%d) - %d)",
                    len, dummySrc.length, off, size, Crypto.META_SIZE);
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }

        {
            int size = Crypto.MIN_SIZE;
            int off = dummySrc.length / 2;
            int len = dummySrc.length - off + 1;
            try
            {
                res.next("CHECK encrypt(6) invalid len(" + len + "): off(" + off + "), src.length(" + dummySrc.length + ")");
                Crypto.encrypt(size, dummyChecksum, dummyRandom, dummySrc, off, len);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = String.format(
                    "OUT OF RANGE len(%d): 0 < len <= min(src.length(%d) - off(%d), size(%d) - %d)",
                    len, dummySrc.length, off, size, Crypto.META_SIZE);
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }

        {
            int size = Crypto.MIN_SIZE;
            byte[] src = new byte[size + size];
            int off = 0;
            int len = size - Crypto.META_SIZE + 1;
            try
            {
                res.next("CHECK encrypt(6) invalid len(" + len + "): size - META_SIZE = " + (size - Crypto.META_SIZE));
                Crypto.encrypt(size, dummyChecksum, dummyRandom, src, off, len);
                return res.failed("WRONG");
            }
            catch (IllegalArgumentException ex)
            {
                String msg = String.format(
                    "OUT OF RANGE len(%d): 0 < len <= min(src.length(%d) - off(%d), size(%d) - %d)",
                    len, src.length, off, size, Crypto.META_SIZE);
                if (!msg.equals(ex.getMessage()))
                {
                    return res.failed(String.format(
                        "WRONG MESSAGE (valid:'%s'): '%s'",
                        msg, ex.getMessage()));
                }
            }
        }
        
        
        {
            int step = (Crypto.MAX_SIZE - Crypto.MIN_SIZE) / 7;
            for (int size = Crypto.MIN_SIZE; size < Crypto.MAX_SIZE + step; size += step)
            {
                size = Math.min(size, Crypto.MAX_SIZE);
                try
                {
                    res.next("CHECK encrypt(4) validate size(" + size + ")");
                    byte[] ret = Crypto.encrypt(size, dummyChecksum, dummyRandom, dummySrc);
                    if (ret == null)
                    {
                        return res.failed("ret is null");
                    }
                    if (ret.length != size)
                    {
                        return res.failed(String.format(
                            "ret size(%d)", ret.length));
                    }
                }
                catch (Exception ex)
                {
                    return res.failed(ex);
                }
            }
        }
        
        {
            int step = (Crypto.MAX_SIZE - Crypto.MIN_SIZE) / 7;
            for (int size = Crypto.MIN_SIZE; size < Crypto.MAX_SIZE + step; size += step)
            {
                size = Math.min(size, Crypto.MAX_SIZE);
                try
                {
                    res.next("CHECK encrypt(6) validate size(" + size + ")");
                    byte[] ret = Crypto.encrypt(size, dummyChecksum, dummyRandom, dummySrc, 0, dummySrc.length);
                    if (ret == null)
                    {
                        return res.failed("ret is null");
                    }
                    if (ret.length != size)
                    {
                        return res.failed(String.format(
                            "ret size(%d)", ret.length));
                    }
                }
                catch (Exception ex)
                {
                    return res.failed(ex);
                }
            }
        }

        return res;
    }
}