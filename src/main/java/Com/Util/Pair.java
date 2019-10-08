package Com.Util;

public class Pair<T,Y> {

    private final T first;
    private final Y second;

    public Pair(T first, Y second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst(){return first;}
    public Y getSecond(){return second;}

}
