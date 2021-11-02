package neetsdkasu.idpwmemoics;

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
}
