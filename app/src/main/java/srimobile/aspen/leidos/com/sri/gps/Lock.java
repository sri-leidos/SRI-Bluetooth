package srimobile.aspen.leidos.com.sri.gps;

/**
 * Created by walswortht on 4/9/2015.
 */
public class Lock {

    private boolean isLocked = false;

    public synchronized void lock()
            throws InterruptedException {
        while (isLocked) {
            wait();
        }
        isLocked = true;
    }

    public synchronized void unlock() {
        isLocked = false;
        notify();
    }
}