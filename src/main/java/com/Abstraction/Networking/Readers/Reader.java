package com.Abstraction.Networking.Readers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;

import java.io.IOException;

public interface Reader {

    AbstractDataPackage read() throws IOException;
}
