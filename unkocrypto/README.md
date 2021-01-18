unkocrypto
==========

う　ん　こ  






## Static Methods
```java
// Encrypt
try
{
	int writtenBytes = neetsdkasu.crypto.Crypto.encrypt((int)blockSize, (java.util.zip.Checksum)cs, (java.util.Random)rand, (java.io.InputStream)src, (java.io.OutputStream)dst);
}
catch (java.io.IOException ex) {}

// Decrypt
try
{
	int writtenBytes = neetsdkasu.crypto.Crypto.decrypt((int)blockSize, (java.util.zip.Checksum)cs, (java.util.Random)rand, (java.io.InputStream)src, (java.io.OutputStream)dst);
}
catch (java.io.IOException ex) {}
```




## Demo
```java
    static void demo() throws Exception
    {
        byte[] data = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        long seed = 123456789L;
        Random rand = new Random();
        Checksum cs = new CRC32();
        int blockSize = Crypto.MIN_BLOCKSIZE;

        System.out.println("seed: " + seed);
        System.out.println("blockSize: " + blockSize);
        System.out.println("data:");
        showBytes(data);
        /* (stdout)
            seed: 123456789
            blockSize: 32
            data:
             1,  2,  3,  4,  5,  6,  7,  8,  9,  a,
        */

        InputStream src = new ByteArrayInputStream(data);
        ByteArrayOutputStream dst = new ByteArrayOutputStream();
        rand.setSeed(seed);

        // Encrypt
        Crypto.encrypt(blockSize, cs, rand, src, dst);

        byte[] secret = dst.toByteArray();
        System.out.println("secret:");
        showBytes(secret);
        /* (stdout)
            secret:
            a2, fc, 45, 90, 64, 80, 77, 46, 3f, 7e,
            1d, 7c, 64, fe, 5c, 98, 7a,  0, 79, a8,
            64, f2, 7d, c1, e3, 66, 31, 31, 1e, 62,
            b6,  4,
        */

        src = new ByteArrayInputStream(secret);
        dst.reset();
        rand.setSeed(seed);

        // Decrypt
        Crypto.decrypt(blockSize, cs, rand, src, dst);

        byte[] recover = dst.toByteArray();
        System.out.println("recover:");
        showBytes(recover);
        /* (stdout)
            recover:
             1,  2,  3,  4,  5,  6,  7,  8,  9,  a,
        */

        if (Arrays.equals(data, recover))
        {
            System.out.println("match!");
        }
        /* (stdout)
            match!
        */
    }

    static void showBytes(byte[] buf)
    {
        for (int i = 0; i < buf.length; i++)
        {
            System.out.printf("%2x, ", 0xFF & (int)buf[i]);
            if (i % 10 == 9) System.out.println();
        }
        System.out.println();
    }
```





------------
## OAP(オープンアプリ)用に関して  

##### 注意点1  
`test_oap`ディレクトリは   
WTK(Sun Java Wireless Toolkit 2.5.2 for CLDC)のプロジェクトとして作ったディレクトリのジャンクション(シンボリックリンク)  
`make_and_deploy_lib_for_oap.bat`や`c_oap.bat`を実行する場合は注意が必要  

##### 注意点2  
`make_and_deploy_lib_for_oap.bat`や`c_oap.bat`を実行する場合はサブモジュールのクローン(`git submodule update --init`)が必要  
