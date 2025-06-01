package neetsdkasu.fencapp;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import neetsdkasu.misc.Utils;

final class UserLocalDataSource {
    final Context context;

    UserLocalDataSource(Context context) {
        this.context = context;
    }

    String getFilename(Uri uri) {
        return Utils.getFilenameFromUri(this.context, uri);
    }

    ParcelFileDescriptor openLocalUri(Uri uri, String mode) throws IOException {
        return this.context.getContentResolver().openFileDescriptor(uri, mode);
    }

    File getPrivateDir(String dir) {
        return this.context.getDir(dir, Context.MODE_PRIVATE);
    }

    File getPrivateFile(String dir, String filename) {
        return new File(this.getPrivateDir(dir), filename);
    }

    void deleteIfExists(final File file) throws IOException {
        Files.deleteIfExists(file.toPath());
    }

    InputStream openInputStream(final File file) throws IOException {
        return new BufferedInputStream(new FileInputStream(file));
    }

    OutputStream openOutputStream(final File file) throws IOException {
        return new BufferedOutputStream(new FileOutputStream(file));
    }

    void importFile(final Uri uri, final File dst) throws IOException {
        Files.deleteIfExists(dst.toPath());
        try (ParcelFileDescriptor pfd = this.openLocalUri(uri, "r")) {
            try (InputStream in = new BufferedInputStream(new FileInputStream(pfd.getFileDescriptor()))) {
                Files.copy(in, dst.toPath());
            }
        }
    }

    void exportFile(final File src, final Uri uri) throws IOException {
        try (ParcelFileDescriptor pfd = this.openLocalUri(uri, "w")) {
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream(pfd.getFileDescriptor()))) {
                Files.copy(src.toPath(), out);
                out.flush();
            }
        }
    }
}