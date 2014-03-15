package martinandersson.com.countdownlatchtest;

import java.util.concurrent.CountDownLatch;

/**
 * Kind of a copy from the first example provided in the JavaDoc of {@linkplain CountDownLatch}.
 * 
 * @author Martin Andersson (webmaster at martinandersson.com)
 */
public class Erroneous implements Marathon
{
    protected final int THREAD_COUNT;
    
    private CountDownLatch start, done;
    
    public Erroneous(int threadCount) {
        this.THREAD_COUNT = threadCount;
        reset();
    }
    
    
    
    /*
     * -------------
     * | OVERRIDES |
     * -------------
     */
    
    @Override
    public void reset() {
        start = new CountDownLatch(1);
        done = new CountDownLatch(THREAD_COUNT);
    }
    
    @Override
    public void runnerIsReady() throws InterruptedException {
        // Instead of a race started flag in the Judge class, one could check start.getCount() == 0 here.
        start.await();
    }

    @Override
    public void judgeFireStart() throws InterruptedException {
        start.countDown(); // <-- Does not throw InterruptedException. We keep the method signature as to be kind towards the subclass Solution.
    }
    
    @Override
    public void judgeAwaitCompletion() throws InterruptedException {
        done.await();
    }

    @Override
    public void runnerDone() {
        done.countDown();
    }
}
