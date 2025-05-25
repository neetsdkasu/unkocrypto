
import java.io.*;
import java.util.*;
import neetsdkasu.fenc.*;

public class Test {
    public static void main(String[] args) throws java.lang.Exception {
        Test.testEnc();
        Test.testEncData();
        Test.testDec();
        Test.testDecData();
        Test.testDecFileName();
        Test.testDecFileNameString();
        System.err.println("all tests ok");
    }

    static byte[] encFileBytes = null;
    static byte[] loadEncFile() throws java.lang.Exception {
        if (Test.encFileBytes == null) {
            Test.encFileBytes = java.nio.file.Files.readAllBytes(new File("file-1.fenc").toPath());
        }
        return Test.encFileBytes.clone();
    }

    static final String PASSWORD = "FooBar123";
    static final String FILE_NAME = "web-demo.txt";
    static final String SRC_STR = "Hello, world!";
    static byte[] getSrc() throws java.lang.Exception {
        return Test.SRC_STR.getBytes("UTF-8");
    }

    static void testEnc() throws java.lang.Exception {
        System.err.println("testEnc...");
        final String password = Test.PASSWORD;
        final String fileName = Test.FILE_NAME;
        final byte[] src = Test.getSrc();
        final ByteArrayInputStream in = new ByteArrayInputStream(src);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Fenc.enc(password, fileName, (long)src.length, in, out);
        final byte[] encData = out.toByteArray();
        final byte[] expect = Test.loadEncFile();
        if (!Arrays.equals(encData, expect)) {
            throw new RuntimeException("invalid: testEnc");
        }
        System.err.println("testEnc... ok");
    }

    static void testEncData() throws java.lang.Exception {
        System.err.println("testEncData...");
        final String password = Test.PASSWORD;
        final String fileName = Test.FILE_NAME;
        final byte[] src = Test.getSrc();
        final byte[] encData = Fenc.encData(password, fileName, src);
        final byte[] expect = Test.loadEncFile();
        if (!Arrays.equals(encData, expect)) {
            throw new RuntimeException("invalid: testEncData");
        }
        System.err.println("testEncData... ok");
    }

    static void testDec() throws java.lang.Exception {
        System.err.println("testDec...");
        final String password = Test.PASSWORD;
        final byte[] src = Test.loadEncFile();
        final ByteArrayInputStream in = new ByteArrayInputStream(src);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final StringBuilder fileName = new StringBuilder();
        Fenc.dec(password, in, out, fileName);
        if (!Test.FILE_NAME.equals(fileName.toString())) {
            throw new RuntimeException("invalid: testDec ... fileName " + fileName);
        }
        final byte[] decData = out.toByteArray();
        final byte[] expect = Test.getSrc();
        if (!Arrays.equals(decData, expect)) {
            throw new RuntimeException("invalid: testDec ... content");
        }
        System.err.println("testDec... ok");
    }

    static void testDecData() throws java.lang.Exception {
        System.err.println("testDecData...");
        final String password = Test.PASSWORD;
        final byte[] src = Test.loadEncFile();
        final ByteArrayInputStream in = new ByteArrayInputStream(src);
        final Fenc.DecDataResult res = Fenc.decData(password, in);
        final String fileName = res.fileName;
        if (!Test.FILE_NAME.equals(fileName)) {
            throw new RuntimeException("invalid: testDecData ... fileName " + fileName);
        }
        final byte[] decData = res.content;
        final byte[] expect = Test.getSrc();
        if (!Arrays.equals(decData, expect)) {
            throw new RuntimeException("invalid: testDecData ... content");
        }
        System.err.println("testDecData... ok");
    }

    static void testDecFileName() throws java.lang.Exception {
        System.err.println("testDecFileName...");
        final String password = Test.PASSWORD;
        final byte[] src = Test.loadEncFile();
        final ByteArrayInputStream in = new ByteArrayInputStream(src);
        final StringBuilder fileName = new StringBuilder();
        final Fenc.FencDecContent fdc = Fenc.decFileName(password, in, fileName);
        if (!Test.FILE_NAME.equals(fileName.toString())) {
            throw new RuntimeException("invalid: testDecFileName ... fileName " + fileName);
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        fdc.decContent(out);
        final byte[] decData = out.toByteArray();
        final byte[] expect = Test.getSrc();
        if (!Arrays.equals(decData, expect)) {
            throw new RuntimeException("invalid: testDecFileName ... content");
        }
        System.err.println("testDecFileName... ok");
    }

    static void testDecFileNameString() throws java.lang.Exception {
        System.err.println("testDecFileNameString...");
        final String password = Test.PASSWORD;
        final byte[] src = Test.loadEncFile();
        final ByteArrayInputStream in = new ByteArrayInputStream(src);
        final String fileName = Fenc.decFileNameString(password, in);
        if (!Test.FILE_NAME.equals(fileName)) {
            throw new RuntimeException("invalid: testDecFileNameString ... fileName " + fileName);
        }
        System.err.println("testDecFileNameString... ok");
    }

}
