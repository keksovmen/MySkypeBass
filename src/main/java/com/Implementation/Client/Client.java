package com.Implementation.Client;

import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Model.ChangeableModel;
import com.Abstraction.Networking.Servers.AbstractServer;
import com.Abstraction.Networking.Servers.SimpleServer;
import com.Abstraction.Networking.Utility.ProtocolValueException;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.Cryptographics.Crypto;
import com.Abstraction.Util.FormatWorker;

import java.io.IOException;

public class Client extends AbstractClient {


    private AbstractServer server;

    public Client(ChangeableModel model) {
        super(model);
    }

    @Override
    protected void additionalCases(BUTTONS buttons, Object[] data) {
        switch (buttons) {
            case CREATE_SERVER:
                onServerCreate(data);
                return;
            case STOP_SERVER:
                onStopServer();
                return;
        }
    }

    @Override
    protected String createDefaultName() {
        return System.getProperty("user.name");
    }

    protected void onServerCreate(Object[] data) {
        String[] strings = validateServerCreateData(data);
        if (strings == null) {
            return;
        }
        try {
            server = SimpleServer.getFromStrings(strings[0], strings[1], strings[2], (Boolean) data[3], authenticator);
        } catch (IOException e) {
            stringNotify(ACTIONS.PORT_ALREADY_BUSY, strings[0]);
            return;
        } catch (ProtocolValueException e) {
            stringNotify(ACTIONS.INVALID_AUDIO_FORMAT, e.getMessage());
            return;
        }
        if (server.start("Simple Server")) {
            plainNotify(ACTIONS.SERVER_CREATED);
        } else {
            plainNotify(ACTIONS.SERVER_CREATED_ALREADY);
        }

    }

    protected void onStopServer(){
        if (server != null) {
            server.close();
            server = null;
            plainNotify(ACTIONS.SERVER_CLOSED);
        }
    }


    protected final String[] validateServerCreateData(Object[] data) {
        String port = (String) data[0];
        if (FormatWorker.checkZeroLength(port))
            port = "8188"; // or get from property map
        if (!checkPort(port))
            return null;

        String sampleRate = (String) data[1];
        if (FormatWorker.checkZeroLength(sampleRate)
                || !FormatWorker.verifyOnlyDigits(sampleRate)) {
            stringNotify(ACTIONS.WRONG_SAMPLE_RATE_FORMAT, sampleRate);
            return null;
        }

        String sampleSize = (String) data[2];
        if (FormatWorker.checkZeroLength(sampleSize)
                || !FormatWorker.verifyOnlyDigits(sampleSize)) {
            stringNotify(ACTIONS.WRONG_SAMPLE_SIZE_FORMAT, sampleSize);
            return null;
        }

        Boolean encryption = (Boolean) data[3];
        if (encryption){
            if (!Crypto.isCipherAcceptable(Crypto.STANDARD_CIPHER_FORMAT)) {
                stringNotify(ACTIONS.CIPHER_FORMAT_ON_SERVER_IS_NOT_ACCEPTED, "Server can't be created in encrypted mode, not supported, by your JVM. Format is - "
                        + Crypto.STANDARD_CIPHER_FORMAT);
                return null;
            }
        }

        return new String[]{port, sampleRate, sampleSize};
    }


}
