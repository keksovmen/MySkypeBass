import java.util.Arrays;

public class CppComparator {

    public static void main(String[] args) {
        /*Test how arrays work compare to cpp and then ArrayList*/
        Dummy dummy = new Dummy(1);
//        ArrayList<Dummy> dummies = new ArrayList<>();
        Dummy[] dummies = new Dummy[2];
//        dummies.add(dummy);
        dummies[0] = dummy;
        dummy.id = 5;
        System.out.println(dummy.id);
//        System.out.println(dummies);
        System.out.println(Arrays.toString(dummies));
    }
}

class Dummy{
    public Dummy() {
    }

    public Dummy(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Dummy{" +
                "id=" + id +
                '}';
    }

    int id;
}