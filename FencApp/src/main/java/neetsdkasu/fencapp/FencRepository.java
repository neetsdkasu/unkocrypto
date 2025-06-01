package neetsdkasu.fencapp;

import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.concurrent.Executor;

import neetsdkasu.fenc.Fenc;
import neetsdkasu.fenc.FencException;
import neetsdkasu.misc.ResultCallback;

class FencRepository {

    private final Executor executor;
    private final Handler resultHandler;
    private final UserLocalDataSource localDataSource;

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

    void export(final Uri uri, final ResultCallback<Void, Void> callback) {
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FencRepository.this.exportSync(uri);
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
        File src = this.localDataSource.getPrivateFile("dec", "src.fenc");
        this.copyEncFile(uri, src);
        File dst = this.localDataSource.getPrivateFile("dec", "dst.data");
        Files.deleteIfExists(dst.toPath());
        StringBuilder filename = new StringBuilder();
        try (InputStream in = new BufferedInputStream(new FileInputStream(src))) {
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(dst))) {
                Fenc.dec(password, in, out, filename);
                out.flush();
            }
        }
        return filename.toString();
    }

    void copyEncFile(final Uri uri, final File dst) throws IOException {
        Files.deleteIfExists(dst.toPath());
        try (ParcelFileDescriptor pfd = this.localDataSource.openLocalUri(uri, "r")) {
            try (InputStream in = new BufferedInputStream(new FileInputStream(pfd.getFileDescriptor()))) {
                Files.copy(in, dst.toPath());
            }
        }
    }

    void exportSync(final Uri uri) throws IOException {
        File origfile = this.localDataSource.getPrivateFile("dec", "dst.data");
        try (ParcelFileDescriptor pfd = this.localDataSource.openLocalUri(uri, "w")) {
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(pfd.getFileDescriptor()))) {
                Files.copy(origfile.toPath(), out);
                out.flush();
            }
        }
    }
}