package neetsdkasu.fenc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class Fenc {

    public static byte[] encData(final String password, final String fileName, final byte[] data) throws IOException {
        if  (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("require: String password");
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("require: String fileName");
        }
        if (fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("don't use any slashes or back-slashes: String fileName");
        }
        try {
            new java.net.URI("file:///" + fileName);
        } catch (java.net.URISyntaxException ex) {
            throw new IllegalArgumentException("syntax error: String fileName", ex);
        }
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("require: byte[] data");
        }
        final ByteArrayInputStream in = new ByteArrayInputStream(data);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Enc.enc(password, fileName, (long)data.length, in, out);
        return out.toByteArray();
    }

    public static void enc(final String password, final String fileName, final long inputSize, final InputStream in, final OutputStream out) throws IOException {
        if  (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("require: String password");
        }
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("require: String fileName");
        }
        if (fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("don't use any slashes or back-slashes: String fileName");
        }
        try {
            new java.net.URI("file:///" + fileName);
        } catch (java.net.URISyntaxException ex) {
            throw new IllegalArgumentException("syntax error: String fileName", ex);
        }
        if (inputSize <= 0L) {
            throw new IllegalArgumentException("must be positive: long inputSize");
        }
        if (in == null) {
            throw new IllegalArgumentException("require: InputStream in");
        }
        if (out == null) {
            throw new IllegalArgumentException("require: OutputStream out");
        }
        Enc.enc(password, fileName, inputSize, in, out);
    }

    public static void dec(String password, InputStream in, OutputStream out, Appendable fileName) throws IOException {
        if  (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("require: String password");
        }
        if (in == null) {
            throw new IllegalArgumentException("require: InputStream in");
        }
        if (out == null) {
            throw new IllegalArgumentException("require: OutputStream out");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("require: Appendable fileName");
        }
        Dec.dec(password, in, fileName).decContent(out);
    }

    public static final class DecDataResult {
        public final String fileName;
        public final byte[] content;
        DecDataResult(final String fileName, byte[] content) {
            this.fileName = fileName;
            this.content = content;
        }
    }

    public static Fenc.DecDataResult decData(String password, InputStream in) throws IOException {
        if  (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("require: String password");
        }
        if (in == null) {
            throw new IllegalArgumentException("require: InputStream in");
        }
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final StringBuilder fileName = new StringBuilder();
        Dec.dec(password, in, fileName).decContent(out);
        return new Fenc.DecDataResult(fileName.toString(), out.toByteArray());
    }

    public static final class FencDecContent {
        Dec.DecContent dc;
        FencDecContent(final Dec.DecContent dc) {
            this.dc = dc;
        }
        Dec.DecContent take() {
            Dec.DecContent tmp = this.dc;
            this.dc = null;
            return tmp;
        }
        public void decContent(final OutputStream out) throws IOException {
            if (out == null) {
                throw new IllegalArgumentException("require: OutputStream out");
            }
            if (this.dc == null) {
                throw new IllegalStateException("already read content");
            }
            this.take().decContent(out);
        }
    }

    public static Fenc.FencDecContent decFileName(String password, InputStream in, Appendable fileName) throws IOException {
        if  (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("require: String password");
        }
        if (in == null) {
            throw new IllegalArgumentException("require: InputStream in");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("require: Appendable fileName");
        }
        return new Fenc.FencDecContent(Dec.dec(password, in, fileName));
    }

    public static String decFileNameString(String password, InputStream in) throws IOException {
        if  (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("require: String password");
        }
        if (in == null) {
            throw new IllegalArgumentException("require: InputStream in");
        }
        final StringBuilder fileName = new StringBuilder();
        Dec.dec(password, in, fileName);
        return fileName.toString();
    }

    static final int SIGNATURE_SIZE = 20;
    static final byte[] SIGNATURE = new byte[SIGNATURE_SIZE];
    static {
        SIGNATURE[0] = 83;
        SIGNATURE[1] = 11;
        SIGNATURE[2] = 21;
        try {
            System.arraycopy("[FENC@NEETSDKASU]".getBytes("UTF-8"), 0, SIGNATURE, 3, SIGNATURE_SIZE-3);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static final int FILE_NAME_BLOCK_SIZE = neetsdkasu.crypto.Crypto.MIN_BLOCKSIZE;
    static final int DATA_BLOCK_SIZE = 1024;
    static final int SIZEZ_BLOCK_SIZE = neetsdkasu.crypto.Crypto.MIN_BLOCKSIZE;

    static final int ENC_FORMAT_VERSION = 2;
    static final int ENC_FORMAT_VERSION_1 = 1;
}

