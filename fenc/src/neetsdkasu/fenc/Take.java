package neetsdkasu.fenc;

import java.io.InputStream;
import java.io.IOException;

class Take extends InputStream {

    final InputStream in;
    final long limit;

    long count = 0L;

    Take(InputStream in, long limit) {
        this.in = in;
        this.limit = limit;
    }
    
    @Override
    public int read() throws IOException {
        if (this.count < this.limit) {
            this.count++;
            return this.in.read();
        } else {
            return -1;
        }
    }

    @Override
    public void close() throws IOException {
        this.in.close();
    }
}
