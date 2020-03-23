import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class COWSet<E> {
    private final E[][] hashArray;

    private static final Object[] EMPTY = new Object[0];

    private static final VarHandle ELEM_HANDLE = MethodHandles.arrayElementVarHandle(Object[][].class);

    @SuppressWarnings("unchecked")
    public COWSet(int capacity) {
        var array = new Object[capacity][];
        Arrays.setAll(array, __ -> EMPTY);
        this.hashArray = (E[][])array;
    }

    @SuppressWarnings("unchecked")
    public boolean add(E element) {
        Objects.requireNonNull(element);
        while (true) {
            var index = element.hashCode() % hashArray.length;
            var array = (E[]) ELEM_HANDLE.getVolatile(hashArray, index);
            for (var e : array) {
                if (element.equals(e)) {
                    return false;
                }
            }

            var newArray = Arrays.copyOf(array, array.length + 1);
            newArray[array.length] = element;

            if (ELEM_HANDLE.compareAndSet(hashArray, index, array, newArray)) {
                return true;
            }
        }
    }
    @SuppressWarnings("unchecked")
    public void forEach(Consumer<? super E> consumer) {
        for(var index = 0; index < hashArray.length; index++) {
            var oldArray = (E[]) ELEM_HANDLE.getVolatile(hashArray, index);
            for(var element: oldArray) {
                consumer.accept(element);
            }
        }
    }

    public static void main(String[] args) {
        var nbThreads = 2;
        var set = new COWSet<Integer>(8);
        for (var i = 0 ; i < nbThreads ; ++i) {
            new Thread( () -> {
               for (var j = 0 ; j < 200_000 ; ++j) {
                   set.add(j);
               }
            }).start();
        }
        System.out.println(set);
    }
}