package neetsdkasu.fencapp;

import android.net.Uri;
import android.os.Handler;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

import neetsdkasu.fenc.Fenc;
import neetsdkasu.fenc.FencException;
import neetsdkasu.misc.ResultCallback;
import neetsdkasu.misc.Utils;

class FencRepository {

    private final Executor executor;
    private final Handler resultHandler;
    private final UserLocalDataSource localDataSource;

    static final String NO_FILE_NAME = "unknown";
    static final String FENC_EXT = ".fenc";

    static final String DEC_DIR = "dec";
    static final String DEC_SRC = "src" + FENC_EXT;
    static final String DEC_DST = "dst.dat";

    static final String ENC_DIR = "enc";
    static final String ENC_SRC = "src.dat";
    static final String ENC_DST = "dst" + FENC_EXT;


    FencRepository(UserLocalDataSource localDataSource, Executor executor, Handler resultHandler) {
        this.localDataSource = localDataSource;
        this.executor = executor;
        this.resultHandler = resultHandler;
    }

    void dec(final String password, final Uri uri, final ResultCallback<String, String> callback) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String filename = FencRepository.this.decSync(password, uri);
                    FencRepository.this.notifySuccess(filename, callback);
                } catch (FencException fex) {
                    String reason = fex.cause.toString();
                    FencRepository.this.notifyFailure(reason, callback);
                } catch (Exception ex) {
                    FencRepository.this.notifyError(ex, callback);
                }
            }
        });
    }

    void exportOrigFile(final Uri uri, final ResultCallback<Void, Void> callback) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FencRepository.this.exportOrigFileSync(uri);
                    FencRepository.this.notifySuccess(null, callback);
                } catch (IOException ex) {
                    FencRepository.this.notifyError(ex, callback);
                }
            }
        });
    }


    void enc(final String password, final Uri uri, final ResultCallback<String, String> callback) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String filename = FencRepository.this.encSync(password, uri);
                    FencRepository.this.notifySuccess(filename, callback);
                } catch (FencException fex) {
                    String reason = fex.cause.toString();
                    FencRepository.this.notifyFailure(reason, callback);
                } catch (Exception ex) {
                    FencRepository.this.notifyError(ex, callback);
                }
            }
        });
    }

    void exportFencFile(final Uri uri, final ResultCallback<Void, Void> callback) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FencRepository.this.exportFencFileSync(uri);
                    FencRepository.this.notifySuccess(null, callback);
                } catch (IOException ex) {
                    FencRepository.this.notifyError(ex, callback);
                }
            }
        });
    }

    <V,E> void notifySuccess(final V data, final ResultCallback<V, E> callback) {
        this.resultHandler.post(new Runnable() {
            public void run() {
                callback.onSuccess(data);
            }
        });
    }

    <V,E> void notifyFailure(final E data, final ResultCallback<V, E> callback) {
        this.resultHandler.post(new Runnable() {
            public void run() {
                callback.onFailure(data);
            }
        });
    }

    <V,E> void notifyError(final Exception ex, final ResultCallback<V, E> callback) {
        this.resultHandler.post(new Runnable() {
            public void run() {
                callback.onError(ex);
            }
        });
    }

    String decSync(final String password, final Uri uri) throws IOException {
        File src = this.localDataSource.getPrivateFile(DEC_DIR, DEC_SRC);
        this.localDataSource.importFile(uri, src);

        File dst = this.localDataSource.getPrivateFile(DEC_DIR, DEC_DST);
        this.localDataSource.deleteIfExists(dst);

        StringBuilder filename = new StringBuilder();

        try (InputStream in = this.localDataSource.openInputStream(src)) {
            try (OutputStream out = this.localDataSource.openOutputStream(dst)) {
                Fenc.dec(password, in, out, filename);
                out.flush();
            }
        }

        this.localDataSource.deleteIfExists(src);

        return filename.toString();
    }

    void exportOrigFileSync(final Uri uri) throws IOException {
        File origfile = this.localDataSource.getPrivateFile(DEC_DIR, DEC_DST);
        this.localDataSource.exportFile(origfile, uri);
        this.localDataSource.deleteIfExists(origfile);
    }

    String encSync(final String password, final Uri uri) throws IOException {
        String filename = Utils.ifNullToDefault(this.localDataSource.getFilename(uri), NO_FILE_NAME);

        File src = this.localDataSource.getPrivateFile(ENC_DIR, ENC_SRC);
        this.localDataSource.importFile(uri, src);

        File dst = this.localDataSource.getPrivateFile(ENC_DIR, ENC_DST);
        this.localDataSource.deleteIfExists(dst);

        long filesize = src.length();

        try (InputStream in = this.localDataSource.openInputStream(src)) {
            try (OutputStream out = this.localDataSource.openOutputStream(dst)) {
                Fenc.enc(password, filename, filesize, in, out);
                out.flush();
            }
        }

        this.localDataSource.deleteIfExists(src);

        return filename + FENC_EXT;
    }

    void exportFencFileSync(final Uri uri) throws IOException {
        File fencfile = this.localDataSource.getPrivateFile(ENC_DIR, ENC_DST);
        this.localDataSource.exportFile(fencfile, uri);
        this.localDataSource.deleteIfExists(fencfile);
    }
}