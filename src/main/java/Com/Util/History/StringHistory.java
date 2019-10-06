package Com.Util.History;

public class StringHistory implements History<String> {

    private String[] history;
    private int index;
    private int pushIndex;

    private int initialIndex;

    public StringHistory(int size) {
        if (size <= 0)
            throw new IllegalArgumentException("Size can't be negative or zero! " + size);
        history = new String[size];
        index = 0;
        pushIndex = 0;
        initialIndex = 0;
    }

    @Override
    public String getNext() {
        initialIndex = index;
        return getNextThroughLoop();
//        index = checkIndex(index);
//        String result = history[index];
//        index--;
//        if (result == null)
//            return getNext();
//        return result;
    }

    private String getNextThroughLoop(){
        index = checkIndex(index);
        String result = history[index];
        index--;
        if (initialIndex == index)
            return "";
        if (result == null)
            return getNextThroughLoop();
        return result;
    }

//    @Override
//    public String getPrevious() {
//        checkIndex();
//        String result = history[index];
//        index--;
//        if (result == null)
//            return "";
//        return result;
//    }

    @Override
    public void push(String data) {
        index = pushIndex;
        history[pushIndex] = data;
        pushIndex++;
        if (pushIndex >= history.length)
            pushIndex = 0;
    }

    private int checkIndex(int index) {
        if (index >= history.length) {
            return 0;
        }else if (index < 0){
            return history.length - 1;
        }
        return index;
    }
}
