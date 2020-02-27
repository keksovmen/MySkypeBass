package com.Abstraction.Networking.Utility.Users;

import com.Abstraction.Util.Algorithms;

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.Key;

public class CipherUser extends PlainUser {


    /**
     * Shared secret with server and this dude
     * <p>
     * "Linked together with {@link #algorithmParameters}"
     * if key is null then 100% params too
     */

    private final Key sharedKey;
    private final AlgorithmParameters algorithmParameters;


    public CipherUser(String name, int id, Key sharedKey, AlgorithmParameters algorithmParameters) {
        super(name, id);
        this.sharedKey = sharedKey;
        this.algorithmParameters = algorithmParameters;
    }

    @Override
    public Key getSharedKey() {
        return sharedKey;
    }

    @Override
    public AlgorithmParameters getAlgorithmParameters() {
        return algorithmParameters;
    }

    /**
     * Do not override server sends it and client parse in this format
     * Must look same as {@link #parser}
     *
     * @return this user as a string with full info to be parsed in {@link #parse(String)}
     */

    @Override
    public final String toNetworkFormat() {
        String body = super.toNetworkFormat();
        try {
            return body + " : " + Algorithms.byteArrayToString(sharedKey.getEncoded()) +
                    "- " + Algorithms.byteArrayToString(algorithmParameters.getEncoded());
        } catch (IOException e) {
            //will not happen after, so much testing
            //and don't know how to treat it
            e.printStackTrace();
            throw new IllegalStateException("Something wrong with algorithm parameters");
        }
    }

}
