package com.thomaskuenneth.souffleur.server.ui;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class JsonFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return f.isFile() && f.getName().toLowerCase().endsWith(".json");
    }

    @Override
    public String getDescription() {
        return ".json";
    }
}
