package fr.umlv.conc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.file.Path;

public class Utils {

    //Question 3
//    private static volatile Path HOME;
//    private final static Object LOCK = new Object();
//
//    public static Path getHome() {
//        if (HOME == null) {
//            synchronized (LOCK) {
//                if (HOME == null) {
//                    return HOME = Path.of(System.getenv("HOME"));
//                }
//            }
//        }
//        return HOME;
//    }

    // Question 4
//    private static Path HOME;
//    private static final VarHandle HANDLE;
//
//    static {
//        var lookup = MethodHandles.lookup();
//        try {
//            HANDLE = lookup.findStaticVarHandle(Utils.class, "HOME", Path.class);
//        } catch (NoSuchFieldException | IllegalAccessException e) {
//            throw new AssertionError(e);
//        }
//    }
//
//    public static Path getHome() {
//        var home = (Path) HANDLE.getAcquire();
//        if (home == null) {
//            synchronized(Utils.class) {
//                home = (Path) HANDLE.getAcquire();
//                if (home == null) {
//                    HANDLE.setRelease(Path.of(System.getenv("HOME")));
//                    return home;
//                }
//            }
//        }
//        return home;
//    }

    // Question 5

    private static class LazyHolder  {
        static final Path HOME = Path.of(System.getenv("HOME"));
    }

    public static Path getHome() {
        return LazyHolder.HOME;
    }
}


/*
   Exercice 3

   2) Le code n'est pas thread-safe car  le singleton HOME peut etre initialisé plusieurs fois (or un singleton ne
   doit etre exécuté qu'une fois par nature)

   3) On a un problème de publication car un thread peut lancer l'initialisation du Path HOME (HOME = Path.of....),
    et un autre thread qui rentre dans la méthode verrait HOME non null, mais pour autant l'initialisation de HOME n'est pas finie.
   Ce 2eme thread renverrait alors un objet Path pas totalement initialisés.

   On peut résoudre ce problème en rendant le champs HOME volatile. Cela permet de s'assurer que toutes les écritures
   du constructeur de Path sont bien réalisées avant d'affecter l'objet à HOME. De la même manière, on s'assure
   que la lecture se fera sur l'objet totalement initialisé.

   4) getAcquire et setRelease coûtent un peu moins cher que des lectures/écritures volatiles

   5) L'initialisation de la classe est instantanée (car pas de champs/blocs static).
    L'initialisation de la classe interne LazyHolder ne sera faite par la JVM qu'au moment où celle-ci doit
    être appelée (donc au premier appel de getHome() seulement)
 */
