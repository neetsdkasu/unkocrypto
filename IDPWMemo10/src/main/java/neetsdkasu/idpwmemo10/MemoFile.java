package neetsdkasu.idpwmemo10;

import java.io.File;

public final class MemoFile {
    public final File file;
    public final String name;

    public MemoFile(File f) {
        file = f;
        name = file.getName();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MemoFile) {
            return file.equals(((MemoFile)obj).file);
        } else {
            return false;
        }
    }
}
