import Bin.Main;

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


    public static void main(String[] args) {
//        FileOutputStream fileWriter = new FileOutputStream(new File("D:\\Users\\Roma\\Desktop\\1488.ts"), true);
//
//        File file = new File("D:\\Users\\Roma\\Desktop\\Новая папка\\1.ts");
//        Stream<Path> list = Files.list(Paths.get("D:\\Users\\Roma\\Desktop\\Новая папка\\"));
//        Path[] objects = list.toArray(Path[]::new);
//        Arrays.sort(objects, new Comparator<Path>() {
//            @Override
//            public int compare(Path o1, Path o2) {
//                return o1.getFileName().toString().hashCode() - o2.getFileName().toString().hashCode();
//            }
//        });
////        System.out.println(Arrays.toString(objects));
//        for (Path object : objects) {
//            try(FileInputStream fileInputStream = new FileInputStream(object.toFile())){
//                int size = (int) object.toFile().length();
//                byte[] data = new byte[size];
//                fileInputStream.read(data);
//                fileWriter.write(data);
//            }
//        }
//        fileWriter.close();
//        System.setProperty("java.util.logging.config.file", Paths.get("src\\main\\resources\\properties\\logging.properties").toString());
//        LogManager.getLogManager().readConfiguration();
        Main.main(args);


    }
}
