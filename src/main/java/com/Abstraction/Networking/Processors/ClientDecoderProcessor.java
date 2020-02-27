package com.Abstraction.Networking.Processors;

import com.Abstraction.Client.Logic;
import com.Abstraction.Model.ChangeableModel;
import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Util.Cryptographics.Crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public class ClientDecoderProcessor extends ClientProcessor {

    private final Cipher decoder;

    public ClientDecoderProcessor(ChangeableModel model, Logic logic) {
        super(model, logic);
        decoder = Crypto.getCipherWithoutExceptions(Crypto.STANDARD_CIPHER_FORMAT);
    }

    @Override
    public boolean process(AbstractDataPackage dataPackage) {
        //decrypt data in package if such exists
        return super.process(decodeDataPackage(dataPackage));
    }

    /**
     * Will decode only if a package contains data, and have key to decode
     *
     * @param dataPackage to decode
     * @return same package if there is no data and modified one if is
     */

    protected AbstractDataPackage decodeDataPackage(AbstractDataPackage dataPackage) {
        if (dataPackage.getHeader().getLength() == 0)
            return dataPackage;
        if (!initCipher(dataPackage.getHeader().getFrom(), dataPackage.getHeader().getCode()))
            return dataPackage;
        dataPackage.setData(decodeData(dataPackage.getData()));
        return dataPackage;
    }

    /**
     * Trying to initialise cipher with DECODE_MODE
     *
     * @param idOfDude    to fetch key and algorithm params
     * @param instruction of dataPackage
     * @return true if cipher ready to decode, false if there is no such dude or exceptions
     */

    protected boolean initCipher(int idOfDude, CODE instruction) {
        User user = getCorrespondUser(idOfDude, instruction);
        if (user == null) {
            //handle not existing in underlying map
            return false;
        }
        try {
            decoder.init(Cipher.DECRYPT_MODE, user.getSharedKey(), user.getAlgorithmParameters());
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Fetch particular dude with key and algorithm, parameters
     *
     * @param idOfDude    to fetch
     * @param instruction to more specific task depend on server
     * @return dude that contain key and alg parameters for this package
     */

    protected User getCorrespondUser(int idOfDude, CODE instruction) {
        switch (instruction) {
            case SEND_CALL:
            case SEND_ACCEPT_CALL:
                return model.getMyself();
        }
        if (idOfDude == WHO.SERVER.getCode()) {
            return model.getMyself();
        } else {
            return model.getUserMap().get(idOfDude);
        }
    }

    /**
     * @param input coded data
     * @return will never return null if algorithm is not changed
     */

    protected byte[] decodeData(byte[] input) {
        try {
            return decoder.doFinal(input);
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
