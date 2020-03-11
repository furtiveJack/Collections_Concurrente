import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
    private final AtomicInteger counter = new AtomicInteger();

    public int nextInt() {
        while (true) {
            var value = counter.get();
            if (counter.compareAndSet(value, value + 1)) {
                return value;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException{
        var counter = new Counter();
        var threads = new ArrayList<Thread>();
        for (var i = 0 ; i < 4 ; ++i) {
            var thread = new Thread( () -> {
                for (var j = 0 ; j < 100_000 ; ++j) {
                    counter.nextInt();
                }
            });
            thread.start();
            threads.add(thread);
        }

        for (var thread : threads) {
            thread.join();
        }

        System.out.println(counter.nextInt());
    }
}


/**

 1.     Le code n'est pas thread safe car deux threads peuvent accéder à la valeur de counter et la modifier sans
        que les autres s'en rendent compte,

 2.     Ca ne change rien, car l'opération ++ n'est pas atomique.

 3.     La valeur de retour de compareAndSet vaut vrai si la valeur a été modifiée, et faux si l'ancienne valeur
        envoyée en paramètre n'est pas celle trouvée en mémoire.

 4.     Cette méthode permet une incrémentation atomique en fonction de l'architecture

 5.     Le terme lock-free signifie que que le code ne contient ni bloc synchronized, ni lock.
        Counter et Counter2 sont donc lock-free
 */