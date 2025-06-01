package neetsdkasu.fencapp;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.File;
import java.io.IOException;

final class UserLocalDataSource {
    final Context context;

    UserLocalDataSource(Context context) {
        this.context = context;
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
}