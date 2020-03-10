package com.Abstraction.Util.Logging.Handlers;

import java.io.IOException;
import java.util.logging.FileHandler;

public class ClientFileHandler extends FileHandler {

    public ClientFileHandler() throws IOException, SecurityException {
    }

    public ClientFileHandler(String pattern) throws IOException, SecurityException {
        super(pattern);
    }

    public ClientFileHandler(String pattern, boolean append) throws IOException, SecurityException {
        super(pattern, append);
    }

    public ClientFileHandler(String pattern, int limit, int count) throws IOException, SecurityException {
        super(pattern, limit, count);
    }

    public ClientFileHandler(String pattern, int limit, int count, boolean append) throws IOException, SecurityException {
        super(pattern, limit, count, append);
    }
}
