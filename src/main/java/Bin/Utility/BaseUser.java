package Bin.Utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseUser {

    private String name;
    private int id;
    public static final Pattern parser = Pattern.compile("((\\w|[а-яА-Я])+) - (\\d+)");

    public BaseUser(String name, int id) {
        this.name = name;
        this.id = id;
//        System.out.println(toString());
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name + " - " + id;
    }

    public static BaseUser parse(String data){
        Matcher matcher = parser.matcher(data);
        matcher.find();
        String name = matcher.group(1);
        String id = matcher.group(3);
        return new BaseUser(name, Integer.parseInt(id));
    }

}
