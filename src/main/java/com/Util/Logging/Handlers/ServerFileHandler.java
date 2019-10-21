package com.Util.Logging.Handlers;

import java.io.IOException;
import java.util.logging.FileHandler;

public class ServerFileHandler extends FileHandler {

    public ServerFileHandler() throws IOException, SecurityException {
    }

    public ServerFileHandler(String pattern) throws IOException, SecurityException {
        super(pattern);
    }

    public ServerFileHandler(String pattern, boolean append) throws IOException, SecurityException {
        super(pattern, append);
    }

    public ServerFileHandler(String pattern, int limit, int count) throws IOException, SecurityException {
        super(pattern, limit, count);
    }

    public ServerFileHandler(String pattern, int limit, int count, boolean append) throws IOException, SecurityException {
        super(pattern, limit, count, append);
    }
}
