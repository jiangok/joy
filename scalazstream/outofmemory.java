import java.lang.Integer;

// to generate hprof, go to the outofmemory.class folder and run:
// java -Xmx1m -XX:+HeapDumpOnOutOfMemoryError outofmemory
public class outofmemory {

  public static void main(String[] args) {
    Integer[] a = new Integer[2000000];
  }
}