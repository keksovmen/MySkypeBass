package Com.Util.History;

public interface History<T> {

    T getNext();
//    T getPrevious();
    void push(T data);
}
