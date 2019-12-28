package com.Abstraction.Networking.Processors;

import com.Abstraction.Client.Logic;
import com.Abstraction.Model.ChangeableModel;
import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Networking.Utility.WHO;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ClientDecoderProcessor extends ClientProcessor {

    private final Cipher decoder;

    public ClientDecoderProcessor(ChangeableModel model, Logic logic) throws NoSuchPaddingException, NoSuchAlgorithmException {
        super(model, logic);
        decoder = Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    @Override
    public boolean process(AbstractDataPackage dataPackage) {
        //decrypt data in package if such exists

        return super.process(decodeDataPackage(dataPackage));
    }

    protected AbstractDataPackage decodeDataPackage(AbstractDataPackage dataPackage) {
        if (dataPackage.getData().length == 0)
            return dataPackage;
        if (!initCipher(dataPackage.getHeader().getFrom()))
            return dataPackage;
        dataPackage.setData(decodeData(dataPackage.getData()));
        return dataPackage;
    }

    protected boolean initCipher(int idOfDude) {
        BaseUser user = getCorrespondUser(idOfDude);
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

    protected BaseUser getCorrespondUser(int idOfDude){
        if (idOfDude == WHO.SERVER.getCode()) {
            return model.getMyself();
        } else {
            return model.getUserMap().get(idOfDude);
        }
    }

    protected byte[] decodeData(byte[] input){
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
