package cn.flyyz.gsupl.classinteractionsystem;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    public static File createTempFile(Context context, String prefix) throws IOException {
        File cacheDir = context.getCacheDir();
        return File.createTempFile(prefix, ".tmp", cacheDir);
    }

    public static void copyInputStreamToFile(InputStream input, File file) throws IOException {
        try (FileOutputStream output = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }
    }
}