package Bin.Networking.Utility;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseUser {

    private String name;
    private int id;
    public static final Pattern parser = Pattern.compile("((\\w|[а-яА-Я])+) - (\\d+)");

    public BaseUser(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    @Override
    public final String toString() {
        return name + " - " + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseUser baseUser = (BaseUser) o;
        return id == baseUser.id &&
                Objects.equals(name, baseUser.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id);
    }

    public static BaseUser parse(String data){
        Matcher matcher = parser.matcher(data);
        matcher.find();
        String name = matcher.group(1);
        String id = matcher.group(3);
        return new BaseUser(name, Integer.parseInt(id));
    }

}
