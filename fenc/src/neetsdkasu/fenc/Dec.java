package neetsdkasu.fenc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.zip.CRC32;
import neetsdkasu.crypto.Crypto;

final class Dec {
    final FencRandom rng;
    final DataInputStream in;
    final ArrayList<Long> sizes;
    final CRC32 cs;
    int version = 0;

    Dec(final String password, final InputStream in) {
        this.rng = new FencRandom(password);
        this.in = new DataInputStream(in);
        this.sizes = new ArrayList<>();
        this.cs = new CRC32();
    }

    class DecContent {
        void decContent(final OutputStream out) throws IOException {
            Dec.this.readData(out)
                .readSizes()
                .readChecksum();
        }
    }

    static DecContent dec(final String password, final InputStream in, final Appendable fileName) throws IOException {
         return new Dec(password, in)
            .readSignature()
            .readFormatVersion()
            .readFileName(fileName)
            .getDecContent();
    }

    DecContent getDecContent() {
        return this.new DecContent();
    }

    Dec readSignature() throws IOException {
        final byte[] buf = new byte[Fenc.SIGNATURE_SIZE];
        this.in.readFully(buf);
        if (!Arrays.equals(buf, Fenc.SIGNATURE)) {
            throw new FencException(FencException.Cause.NO_SIGNATURE);
        }
        return this;
    }

    Dec readFormatVersion() throws IOException {
        this.version = (int)this.in.readShort();
        if (this.version < 1 || Fenc.ENC_FORMAT_VERSION < this.version) {
            final String msg = "the file version is " + this.version;
            throw new FencException(FencException.Cause.UNSUPPORTED_VERSION, msg);
        }
        return this;
    }

    void readDecBytes(final int blockSize, final OutputStream data) throws IOException {
        final long encSize = this.in.readLong();
        final CRC32 cs = new CRC32();
        final InputStream in = new Take(this.in, encSize);
        final OutputStream out = new WithChecksumOutputStream(data, this.cs);
        final long dataSize = (long)Crypto.decrypt(blockSize, cs, this.rng, in, out);
        this.sizes.add(dataSize);
    }

    Dec readFileName(final Appendable fileName) throws IOException {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        this.readDecBytes(Fenc.FILE_NAME_BLOCK_SIZE, buf);
        String tmp;
        try {
            tmp = new DataInputStream(new ByteArrayInputStream(buf.toByteArray())).readUTF();
        } catch (java.io.UTFDataFormatException ex) {
            throw new FencException(FencException.Cause.WRONG_FORMAT_FILENAME, ex);
        }
        fileName.append(tmp);
        return this;
    }

    Dec readData(final OutputStream out) throws IOException {
        this.readDecBytes(Fenc.DATA_BLOCK_SIZE, out);
        return this;
    }

    Dec readSizes() throws IOException {
        final Long[] sizes = this.sizes.toArray(new Long[0]);
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        this.readDecBytes(Fenc.SIZEZ_BLOCK_SIZE, buf);
        final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buf.toByteArray()));
        final int length = dis.readUnsignedByte();
        if (sizes.length != length) {
            final String msg = "sizesSize: " + sizes.length + " != " + length;
            throw new FencException(FencException.Cause.UNMATCHED_SIZES, msg);
        }
        if (this.version == Fenc.ENC_FORMAT_VERSION_1) {
            return this;
        }
        for (int i = 0; i < sizes.length; i++) {
            final long expect = sizes[i];
            final long actual = dis.readLong();
            if (expect != actual) {
                final String msg = "sizes[" + i + "]: " + expect + " != " + actual;
                throw new FencException(FencException.Cause.UNMATCHED_SIZES, msg);
            }
        }
        return this;
    }

    Dec readChecksum() throws IOException {
        final long expect = this.cs.getValue();
        final long actual = this.in.readLong();
        if (expect != actual) {
            throw new FencException(FencException.Cause.UNMATCHED_CHECKSUM);
        }
        return this;
    }
}
