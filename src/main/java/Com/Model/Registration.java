package Com.Model;

public interface Registration {

    boolean registerListener(Updater listener);

    boolean removeListener(Updater listener);
}
