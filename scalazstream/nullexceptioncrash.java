import java.lang.Integer;

public class nullexceptioncrash {
    class A { Integer b; }

    public static void main(String[] argv) {
        A a = null;
        Integer b = a.b;
    }
}