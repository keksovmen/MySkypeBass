import java.io.*;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

public class Tester {

    private int anInt;

    public synchronized void ImcreaseI(){
        anInt++;
    }

    public void showInt(){
        System.out.println(anInt);
    }

    public synchronized void showSync(){
        System.out.println("SYNC = " + anInt);
    }


    public static void main(String[] args) throws IOException {
        FileOutputStream fileWriter = new FileOutputStream(new File("D:\\Users\\Roma\\Desktop\\1488.ts"), true);

        File file = new File("D:\\Users\\Roma\\Desktop\\Новая папка\\1.ts");
        Stream<Path> list = Files.list(Paths.get("D:\\Users\\Roma\\Desktop\\Новая папка\\"));
        Path[] objects = list.toArray(Path[]::new);
        Arrays.sort(objects, new Comparator<Path>() {
            @Override
            public int compare(Path o1, Path o2) {
                return o1.getFileName().toString().hashCode() - o2.getFileName().toString().hashCode();
            }
        });
//        System.out.println(Arrays.toString(objects));
        for (Path object : objects) {
            try(FileInputStream fileInputStream = new FileInputStream(object.toFile())){
                int size = (int) object.toFile().length();
                byte[] data = new byte[size];
                fileInputStream.read(data);
                fileWriter.write(data);
            }
        }
        fileWriter.close();
//        FileInputStream fileReader1 = new FileInputStream(file);
//        int size = (int) file.length();
//        byte[] data = new byte[size];
//        fileReader1.read(data);
//        fileWriter.write(data);
//        fileReader1.close();

//        file = new File("D:\\Users\\Roma\\Desktop\\Новая папка\\2.ts");
//        fileReader1 = new FileInputStream(file);
//        size = (int) file.length();
//        data = new byte[size];
//        fileReader1.read(data);
//        fileWriter.write(data);
//        fileReader1.close();
//        fileWriter.close();
//        FileReader fileReader2 = new FileReader("D:\\Users\\Roma\\Desktop\\Новая папка\\1.ts");

    }
}
