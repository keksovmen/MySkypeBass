package com.Abstraction.Networking.Processors;

import com.Abstraction.Client.Logic;
import com.Abstraction.Model.ChangeableModel;
import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.Users.BaseUser;
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
        if (!dataPackage.getHeader().getCode().equals(CODE.SEND_SOUND))
            System.out.println(dataPackage.getHeader());
        return super.process(decodeDataPackage(dataPackage));
    }

    protected AbstractDataPackage decodeDataPackage(AbstractDataPackage dataPackage) {
        if (dataPackage.getHeader().getLength() == 0)
            return dataPackage;
        if (!initCipher(dataPackage.getHeader().getFrom(), dataPackage.getHeader().getCode()))
            return dataPackage;
        dataPackage.setData(decodeData(dataPackage.getData()));
        return dataPackage;
    }

    protected boolean initCipher(int idOfDude, CODE instruction) {
        BaseUser user = getCorrespondUser(idOfDude, instruction);
        if (user == null) {
            //handle not existing in udnerlying map
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

    protected BaseUser getCorrespondUser(int idOfDude, CODE instruction) {
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
