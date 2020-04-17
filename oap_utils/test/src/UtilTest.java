
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Test {}

class UtilTest
{
    public static void main(String[] args) throws Exception
    {
        String target = null;
        if (args.length > 0)
        {
            target = args[0];
        }

        for (Method m : UtilTest.class.getDeclaredMethods())
        {
            if (m.isAnnotationPresent(Test.class))
            {
                if (target != null && m.getName().indexOf(target) < 0)
                {
                    continue;
                }
                Calendar cal = Calendar.getInstance();
                System.out.print("[" + cal.getTime() + "]");
                System.out.print(" " + m.getName() + ": ");
                try
                {
                    m.invoke(null);
                    System.out.println("passed");
                }
                catch (Exception ex)
                {
                    System.out.println("failed");
                    ex.printStackTrace();
                    break;
                }
            }
        }
        System.out.println("done.");
    }

    @Test
    static void testBase64Encoder()
    {
        Random rand = new Random();

        java.util.Base64.Encoder enc1 = java.util.Base64.getEncoder();
        neetsdkasu.util.Base64.Encoder enc2 = neetsdkasu.util.Base64.getEncoder();

        for (int i = 0; i < 100000; i++)
        {
            int size = rand.nextInt(2048) + 1;
            byte[] src = new byte[size];
            rand.nextBytes(src);

            byte[] dst1 = enc1.encode(src);
            byte[] dst2 = enc2.encode(src);

            if (!Arrays.equals(dst1, dst2))
            {
                throw new RuntimeException("unmatch");
            }
        }
    }

    @Test
    static void testBase64EncoderWithoutPadding()
    {
        Random rand = new Random();

        java.util.Base64.Encoder enc1 = java.util.Base64.getEncoder().withoutPadding();
        neetsdkasu.util.Base64.Encoder enc2 = neetsdkasu.util.Base64.getEncoder().withoutPadding();

        for (int i = 0; i < 100000; i++)
        {
            int size = rand.nextInt(2048) + 1;
            byte[] src = new byte[size];
            rand.nextBytes(src);

            byte[] dst1 = enc1.encode(src);
            byte[] dst2 = enc2.encode(src);

            if (!Arrays.equals(dst1, dst2))
            {
                throw new RuntimeException("unmatch");
            }
        }
    }

    @Test
    static void testBase64UrlEncoder()
    {
        Random rand = new Random();

        java.util.Base64.Encoder enc1 = java.util.Base64.getUrlEncoder();
        neetsdkasu.util.Base64.Encoder enc2 = neetsdkasu.util.Base64.getUrlEncoder();

        for (int i = 0; i < 100000; i++)
        {
            int size = rand.nextInt(2048) + 1;
            byte[] src = new byte[size];
            rand.nextBytes(src);

            byte[] dst1 = enc1.encode(src);
            byte[] dst2 = enc2.encode(src);

            if (!Arrays.equals(dst1, dst2))
            {
                throw new RuntimeException("unmatch");
            }
        }
    }

    @Test
    static void testBase64MimeEncoder()
    {
        Random rand = new Random();

        java.util.Base64.Encoder enc1 = java.util.Base64.getMimeEncoder();
        neetsdkasu.util.Base64.Encoder enc2 = neetsdkasu.util.Base64.getMimeEncoder();

        for (int i = 0; i < 100000; i++)
        {
            int size = rand.nextInt(2048) + 1;
            byte[] src = new byte[size];
            rand.nextBytes(src);

            byte[] dst1 = enc1.encode(src);
            byte[] dst2 = enc2.encode(src);

            if (!Arrays.equals(dst1, dst2))
            {
                throw new RuntimeException("unmatch");
            }
        }
    }

    @Test
    static void testBase64MimeEncoderWithLineParam()
    {
        Random rand = new Random();
        byte[] sepChars = ",._;:@[](){}!#$%&'~^-*?\"\t\r\n".getBytes();

        for (int i = 0; i < 100000; i++)
        {
            int size = rand.nextInt(2048) + 1;
            byte[] src = new byte[size];
            rand.nextBytes(src);

            int lineLength = rand.nextInt(300);
            int sepLength = rand.nextInt(5) + rand.nextInt(3);
            byte[] lineSeperator = new byte[sepLength];
            for (int j = 0; j < sepLength; j++)
            {
                lineSeperator[j] = sepChars[rand.nextInt(sepChars.length)];
            }

            java.util.Base64.Encoder enc1 = java.util.Base64.getMimeEncoder(lineLength, lineSeperator);
            neetsdkasu.util.Base64.Encoder enc2 = neetsdkasu.util.Base64.getMimeEncoder(lineLength, lineSeperator);

            byte[] dst1 = enc1.encode(src);
            byte[] dst2 = enc2.encode(src);

            if (!Arrays.equals(dst1, dst2))
            {
                throw new RuntimeException("unmatch");
            }
        }
    }

    @Test
    static void testBase64Decoder()
    {
        Random rand = new Random();

        java.util.Base64.Encoder enc = java.util.Base64.getEncoder();
        java.util.Base64.Decoder dec1 = java.util.Base64.getDecoder();
        neetsdkasu.util.Base64.Decoder dec2 = neetsdkasu.util.Base64.getDecoder();

        for (int i = 0; i < 100000; i++)
        {
            int size = rand.nextInt(2048) + 1;
            byte[] src = new byte[size];
            rand.nextBytes(src);

            byte[] buf = enc.encode(src);

            byte[] rec1 = dec1.decode(buf);
            byte[] rec2 = dec2.decode(buf);

            if (!Arrays.equals(rec1, rec2))
            {
                throw new RuntimeException("unmatch");
            }
        }
    }

    @Test
    static void testBase64UrlDecoder()
    {
        Random rand = new Random();

        java.util.Base64.Encoder enc = java.util.Base64.getUrlEncoder();
        java.util.Base64.Decoder dec1 = java.util.Base64.getUrlDecoder();
        neetsdkasu.util.Base64.Decoder dec2 = neetsdkasu.util.Base64.getUrlDecoder();

        for (int i = 0; i < 100000; i++)
        {
            int size = rand.nextInt(2048) + 1;
            byte[] src = new byte[size];
            rand.nextBytes(src);

            byte[] buf = enc.encode(src);

            byte[] rec1 = dec1.decode(buf);
            byte[] rec2 = dec2.decode(buf);

            if (!Arrays.equals(rec1, rec2))
            {
                throw new RuntimeException("unmatch");
            }
        }
    }

    @Test
    static void testBase64MimeDecoder()
    {
        Random rand = new Random();

        java.util.Base64.Encoder enc = java.util.Base64.getMimeEncoder();
        java.util.Base64.Decoder dec1 = java.util.Base64.getMimeDecoder();
        neetsdkasu.util.Base64.Decoder dec2 = neetsdkasu.util.Base64.getMimeDecoder();

        for (int i = 0; i < 100000; i++)
        {
            int size = rand.nextInt(2048) + 1;
            byte[] src = new byte[size];
            rand.nextBytes(src);

            byte[] buf = enc.encode(src);

            byte[] rec1 = dec1.decode(buf);
            byte[] rec2 = dec2.decode(buf);

            if (!Arrays.equals(rec1, rec2))
            {
                throw new RuntimeException("unmatch");
            }
        }
    }

    @Test
    static void testBase64MimeDecoderWithLineParam()
    {
        Random rand = new Random();
        byte[] sepChars = ",._;:@[](){}!#$%&'~^-*?\"\t\r\n".getBytes();

        java.util.Base64.Decoder dec1 = java.util.Base64.getMimeDecoder();
        neetsdkasu.util.Base64.Decoder dec2 = neetsdkasu.util.Base64.getMimeDecoder();

        for (int i = 0; i < 100000; i++)
        {
            int size = rand.nextInt(2048) + 1;
            byte[] src = new byte[size];
            rand.nextBytes(src);

            int lineLength = rand.nextInt(300);
            int sepLength = rand.nextInt(5) + rand.nextInt(3);
            byte[] lineSeperator = new byte[sepLength];
            for (int j = 0; j < sepLength; j++)
            {
                lineSeperator[j] = sepChars[rand.nextInt(sepChars.length)];
            }

            java.util.Base64.Encoder enc = java.util.Base64.getMimeEncoder(lineLength, lineSeperator);

            byte[] buf = enc.encode(src);

            byte[] rec1 = dec1.decode(buf);
            byte[] rec2 = dec2.decode(buf);

            if (!Arrays.equals(rec1, rec2))
            {
                throw new RuntimeException("unmatch");
            }
        }
    }

    @Test
    static void testBase64DecoderWithoutPadding()
    {
        Random rand = new Random();

        java.util.Base64.Encoder enc = java.util.Base64.getEncoder().withoutPadding();
        java.util.Base64.Decoder dec1 = java.util.Base64.getDecoder();
        neetsdkasu.util.Base64.Decoder dec2 = neetsdkasu.util.Base64.getDecoder();

        for (int i = 0; i < 100000; i++)
        {
            int size = rand.nextInt(2048) + 1;
            byte[] src = new byte[size];
            rand.nextBytes(src);

            byte[] buf = enc.encode(src);

            byte[] rec1 = dec1.decode(buf);
            byte[] rec2 = dec2.decode(buf);

            if (!Arrays.equals(rec1, rec2))
            {
                throw new RuntimeException("unmatch");
            }
        }
    }

    @Test
    static void testBase64MimeDecoderWithLineParamWithoutPadding()
    {
        Random rand = new Random();
        byte[] sepChars = ",._;:@[](){}!#$%&'~^-*?\"\t\r\n".getBytes();

        java.util.Base64.Decoder dec1 = java.util.Base64.getMimeDecoder();
        neetsdkasu.util.Base64.Decoder dec2 = neetsdkasu.util.Base64.getMimeDecoder();

        for (int i = 0; i < 100000; i++)
        {
            int size = rand.nextInt(2048) + 1;
            byte[] src = new byte[size];
            rand.nextBytes(src);

            int lineLength = rand.nextInt(300);
            int sepLength = rand.nextInt(5) + rand.nextInt(3);
            byte[] lineSeperator = new byte[sepLength];
            for (int j = 0; j < sepLength; j++)
            {
                lineSeperator[j] = sepChars[rand.nextInt(sepChars.length)];
            }

            java.util.Base64.Encoder enc = java.util.Base64.getMimeEncoder(lineLength, lineSeperator).withoutPadding();

            byte[] buf = enc.encode(src);

            byte[] rec1 = dec1.decode(buf);
            byte[] rec2 = dec2.decode(buf);

            if (!Arrays.equals(rec1, rec2))
            {
                throw new RuntimeException("unmatch");
            }
        }
    }


    @Test
    static void testBase64DecoderInvalidInput()
    {
        Random rand = new Random();

        java.util.Base64.Encoder enc = java.util.Base64.getEncoder();
        java.util.Base64.Decoder dec1 = java.util.Base64.getDecoder();
        neetsdkasu.util.Base64.Decoder dec2 = neetsdkasu.util.Base64.getDecoder();

        for (int i = 0; i < 100000; i++)
        {
            int size = rand.nextInt(2048) + 1;
            byte[] src = new byte[size];
            rand.nextBytes(src);

            byte[] buf = enc.encode(src);

            if ((i & 1) == 0)
            {
                for (int j = rand.nextInt(5); j >= 0; j--)
                {
                    buf[rand.nextInt(buf.length)] = (byte)rand.nextInt(256);
                }
            }
            else
            {
                buf = Arrays.copyOf(buf, rand.nextInt(buf.length + buf.length / 10 + 8));
            }

            byte[] rec1, rec2;
            Exception ex1 = null, ex2 = null;

            try
            {
                rec1 = dec1.decode(buf);
            }
            catch (Exception ex)
            {
                ex1 = ex;
                rec1 = null;
            }

            try
            {
                rec2 = dec2.decode(buf);
            }
            catch (Exception ex)
            {
                ex2 = ex;
                rec2 = null;
            }

            if (!Arrays.equals(rec1, rec2))
            {
                System.out.println(Arrays.toString(buf));
                System.out.println(Arrays.toString(rec1));
                System.out.println(Arrays.toString(rec2));
                System.out.println(i);
                System.out.println(ex1);
                System.out.println(ex2);
                throw new RuntimeException("unmatch");
            }
        }
    }

    @Test
    static void testEncodeWithEmptyInput()
    {
        java.util.Base64.Encoder[] encoders1 = {
            java.util.Base64.getEncoder(),
            java.util.Base64.getUrlEncoder(),
            java.util.Base64.getMimeEncoder()
        };
        neetsdkasu.util.Base64.Encoder[] encoders2 = {
            neetsdkasu.util.Base64.getEncoder(),
            neetsdkasu.util.Base64.getUrlEncoder(),
            neetsdkasu.util.Base64.getMimeEncoder()
        };

        for (int i = 0; i < encoders1.length; i++)
        {
            java.util.Base64.Encoder enc1 = encoders1[i];
            neetsdkasu.util.Base64.Encoder enc2 = encoders2[i];

            byte[] src = new byte[0];
            byte[] buf1, buf2;
            Exception ex1 = null, ex2 = null;

            try
            {
                buf1 = enc1.encode(src);
            }
            catch (Exception ex)
            {
                ex1 = ex;
                buf1 = null;
            }

            try
            {
                buf2 = enc2.encode(src);
            }
            catch (Exception ex)
            {
                ex2 = ex;
                buf2 = null;
            }

            if (!Arrays.equals(buf1, buf2))
            {
                System.out.println(Arrays.toString(buf1));
                System.out.println(Arrays.toString(buf2));
                System.out.println(i);
                System.out.println(ex1);
                System.out.println(ex2);
                throw new RuntimeException("unmatch");
            }
        }
    }

    @Test
    static void testDecodeWithEmptyInput()
    {
        java.util.Base64.Decoder[] decoders1 = {
            java.util.Base64.getDecoder(),
            java.util.Base64.getUrlDecoder(),
            java.util.Base64.getMimeDecoder()
        };
        neetsdkasu.util.Base64.Decoder[] decoders2 = {
            neetsdkasu.util.Base64.getDecoder(),
            neetsdkasu.util.Base64.getUrlDecoder(),
            neetsdkasu.util.Base64.getMimeDecoder()
        };

        for (int i = 0; i < decoders1.length; i++)
        {
            java.util.Base64.Decoder dec1 = decoders1[i];
            neetsdkasu.util.Base64.Decoder dec2 = decoders2[i];

            byte[] src = new byte[0];
            byte[] buf1, buf2;
            Exception ex1 = null, ex2 = null;

            try
            {
                buf1 = dec1.decode(src);
            }
            catch (Exception ex)
            {
                ex1 = ex;
                buf1 = null;
            }

            try
            {
                buf2 = dec2.decode(src);
            }
            catch (Exception ex)
            {
                ex2 = ex;
                buf2 = null;
            }

            if (!Arrays.equals(buf1, buf2))
            {
                System.out.println(Arrays.toString(buf1));
                System.out.println(Arrays.toString(buf2));
                System.out.println(i);
                System.out.println(ex1);
                System.out.println(ex2);
                throw new RuntimeException("unmatch");
            }
        }
    }
}