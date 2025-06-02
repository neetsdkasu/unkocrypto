package neetsdkasu.fenc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.CRC32;
import neetsdkasu.crypto.Crypto;

final class Enc {
    final FencRandom rng;
    final DataOutputStream out;
    final ArrayList<Long> sizes;
    final CRC32 cs;

    Enc(final String password, final OutputStream out) {
        this.rng = new FencRandom(password);
        this.out = new DataOutputStream(out);
        this.sizes = new ArrayList<>();
        this.cs = new CRC32();
    }

    static void enc(final String password, final String fileName, final long inputSize, final InputStream in, final OutputStream out) throws IOException {
        new Enc(password, out)
            .writeSignature()
            .writeFormatVersion()
            .writeFileName(fileName)
            .writeData(inputSize, in)
            .writeSizes()
            .writeChecksum();
    }

    static long calcEncSize(final int blockSize, final long inputSize) {
        final int dataSize = blockSize - Crypto.META_SIZE;
        final long dataCount = (inputSize + (long)dataSize - 1L) / (long)dataSize;
        return dataCount * (long)blockSize;
    }

    Enc writeSignature() throws IOException {
        this.out.write(Fenc.SIGNATURE);
        return this;
    }

    Enc writeFormatVersion() throws IOException {
        this.out.writeShort(Fenc.ENC_FORMAT_VERSION);
        return this;
    }

    Enc writeEncBytes(final int blockSize, final long dataSize, final InputStream data) throws IOException {
        this.sizes.add(dataSize);
        final long encSize = Enc.calcEncSize(blockSize, dataSize);
        out.writeLong(encSize);
        final CRC32 cs = new CRC32();
        final InputStream in = new WithChecksumInputStream(new Take(data, dataSize), this.cs);
        Crypto.encrypt(blockSize, cs, this.rng, in, this.out);
        return this;
    }

    Enc writeFileName(final String fileName) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(fileName);
        dos.flush();
        final byte[] data = baos.toByteArray();
        final InputStream in = new ByteArrayInputStream(data);
        return this.writeEncBytes(Fenc.FILE_NAME_BLOCK_SIZE, (long)data.length, in);
    }

    Enc writeData(final long dataSize, final InputStream data) throws IOException {
        return this.writeEncBytes(Fenc.DATA_BLOCK_SIZE, dataSize, data);
    }

    Enc writeSizes() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(baos);
        dos.writeByte(this.sizes.size());
        for (long v : this.sizes) {
            dos.writeLong(v);
        }
        dos.flush();
        final byte[] data = baos.toByteArray();
        final InputStream in = new ByteArrayInputStream(data);
        return this.writeEncBytes(Fenc.SIZEZ_BLOCK_SIZE, (long)data.length, in);
    }
    
    Enc writeChecksum() throws IOException {
        this.out.writeLong(this.cs.getValue());
        return this;
    }
}
