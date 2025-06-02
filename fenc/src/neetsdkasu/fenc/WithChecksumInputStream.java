package neetsdkasu.fenc;

import java.io.InputStream;
import java.io.IOException;
import java.util.zip.Checksum;

class WithChecksumInputStream extends InputStream {

    final InputStream in;
    final Checksum cs;

    WithChecksumInputStream(InputStream in, Checksum cs) {
        this.in = in;
        this.cs = cs;
    }
    
    @Override
    public int read() throws IOException {
        int b = this.in.read();
        if (b >= 0) {
            this.cs.update(b);
        }
        return b;
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }
}
