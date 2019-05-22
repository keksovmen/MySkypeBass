package Bin.Networking.Utility;

import Bin.GUI.Forms.ThirdSkin;

import java.util.regex.Matcher;

public class ClientUser extends BaseUser {

    private ThirdSkin thirdSkin;

    public ClientUser(String name, int id) {
        super(name, id);
    }

    public ThirdSkin getThirdSkin() {
        return thirdSkin;
    }

    public void setThirdSkin(ThirdSkin thirdSkin) {
        this.thirdSkin = thirdSkin;
    }

    public static ClientUser parse(String data){
        Matcher matcher = parser.matcher(data);
        matcher.find();
        String name = matcher.group(1);
        String id = matcher.group(3);
        return new ClientUser(name, Integer.parseInt(id));
    }
}
