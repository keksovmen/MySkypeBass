package com.Abstraction.Util;

import java.io.InputStream;
import java.nio.file.NoSuchFileException;

public class Checker {

    private Checker() {
    }

    /**
     * Simple check if such resource exists
     *
     * @param path to resource
     * @return opened input stream for the resource
     * @throws NoSuchFileException if there is no such resource
     */

    public static InputStream getCheckedInput(String path) throws NoSuchFileException {
        InputStream resourceAsStream = Checker.class.getResourceAsStream(path);
        if (resourceAsStream == null) {
            throw new NoSuchFileException("There is no such resource - " + path);
        }
        return resourceAsStream;
    }


}
