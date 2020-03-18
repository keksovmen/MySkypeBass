package com.Implementation.Util.Logging;

import com.Abstraction.Util.Logging.LogManagerHelper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DesktopLogManager extends LogManagerHelper {

    public static final Path pathToLogs =
            Paths.get(System.getProperty("user.home") +
                    "/SkypeButBassBoosted/Client.log");

    @Override
    protected boolean prepareFiles() {
        Path parent = pathToLogs.getParent();
        if (!Files.isDirectory(parent)) {
            try {
                Files.createDirectory(parent);
            } catch (IOException e) {
                System.err.println("Could't create directory for logs");
                return false;
            }
        }
        return true;
    }

    @Override
    protected InputStream getPropertiesStream() throws IOException {
        return DesktopLogManager.class.getResourceAsStream("/properties/logging.properties");
    }
}
