package Com.Networking.Utility;

import java.util.concurrent.atomic.AtomicBoolean;

public class ClientUser extends BaseUser {
    private final AtomicBoolean isCalling;

    public ClientUser(String name, int id) {
        super(name, id);
        isCalling = new AtomicBoolean(false);
    }

    public void call(){
        isCalling.set(true);
    }

    public void drop(){
        isCalling.set(false);
    }

    public boolean isCalling(){
        return isCalling.get();
    }
}
