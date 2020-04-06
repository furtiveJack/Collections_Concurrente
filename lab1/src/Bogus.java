public class Bogus {
  private boolean stop;
  private final Object lock = new Object();

  public void runCounter() {
    var localCounter = 0;
    for(;;) {
      synchronized(lock) {
        if (stop) {
          break;
        }
      }
      localCounter++;
    }
    System.out.println(localCounter);
  }

  public void stop() {
    synchronized(lock) {
      stop = true;
    }
  }

  public static void main(String[] args) throws InterruptedException {
    var bogus = new Bogus();
    var thread = new Thread(bogus::runCounter);
    thread.start();
    Thread.sleep(100);
    bogus.stop();
    thread.join();
  }
}

/**
 1.
      Ce programme incrémente la variable localCounter de runCounter() jusqu'à ce que bogus.stop() soit appelée.

 2.
      Une boucle infinie apparait. En effet, la valeur du champs Bogus.stop est stockée en Ram, mais la VM, dans un
      soucis d'optimisation (au sein de la méthode runCounter()), va faire une copie dans un registre de la valeur de
      Bogus.stop. Donc quand la valeur de stop est changée (en ram), la méthode runCounter() n'en a pas connaissance
      et continue donc de tourner indéfiniment.

 3.
      On corrige le problème en utilisant des blocs synchronized qui forcent les lectures/écritures des champs de
      l'objet dans la ram plutot que dans des registres.

Res : 62980200

 4.
      Une implémentation n'utilisant ni synchronized, ni lock sont appelées lock-free

 */