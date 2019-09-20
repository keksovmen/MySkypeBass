package Com.Model;

public interface Registration<T> {

    boolean registerListener(T listener);

    boolean removeListener(T listener);
}
