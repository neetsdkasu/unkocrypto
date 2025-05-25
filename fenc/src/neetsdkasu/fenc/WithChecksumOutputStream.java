package neetsdkasu.fenc;

import java.io.OutputStream;
import java.io.IOException;
import java.util.zip.Checksum;

class WithChecksumOutputStream extends OutputStream {

    final OutputStream out;
    final Checksum cs;

    WithChecksumOutputStream(OutputStream out, Checksum cs) {
        this.out = out;
        this.cs = cs;
    }

    @Override
    public void write(int b) throws IOException {
        this.cs.update(b);
        this.out.write(b);
    }
    
    @Override
    public void flush() throws IOException {
        this.out.flush();
    }

    @Override
    public void close() throws IOException {
        this.out.close();
    }
}
