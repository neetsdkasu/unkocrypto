
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
        neetsdkasu.util.Base64.getDecoder();
    }
}