package Com.Networking.Utility;

/**
 * Represent client side user
 */

public class ClientUser extends BaseUser {

    public static final int NO_ONE = -1;
//    private final AtomicBoolean isCalling;
    private int whoCalling = NO_ONE;

    public ClientUser(String name, int id) {
        super(name, id);
//        isCalling = new AtomicBoolean(false);
    }

    /**
     * When you calling some one
     */

    public synchronized void call(int id){
        whoCalling = id;
    }

    /**
     * When you stop calling
     */

    public synchronized void drop(){
        whoCalling = NO_ONE;
    }

    public int isCalling(){
        return whoCalling;
    }
}
