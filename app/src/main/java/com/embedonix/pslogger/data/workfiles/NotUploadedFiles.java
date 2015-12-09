package com.embedonix.pslogger.data.workfiles;



import java.io.File;

/**
 * Created by Saeid on 12-5-2014.
 */
public class NotUploadedFiles {

    private File file;

    public NotUploadedFiles(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
