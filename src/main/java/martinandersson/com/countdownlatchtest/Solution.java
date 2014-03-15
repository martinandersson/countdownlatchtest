package martinandersson.com.countdownlatchtest;

import java.util.concurrent.CountDownLatch;

/**
 * <p>Represents a working solution to the race-problem where runner threads might miss
 * the start. The fix is to introduce a third <code>CountDownLatch</code> counting down
 * all threads that is <i>prepared</i> for the start signal.</p>
 * 
 * @author Martin Andersson (webmaster at martinandersson.com)
 */
public class Solution extends Erroneous
{
    private CountDownLatch prepare;
    
    public Solution(int threadCount) {
        super(threadCount);
    }
    
    
    
    /*
     * -------------
     * | OVERRIDES |
     * -------------
     */
    
    @Override
    public void reset() {
        prepare = new CountDownLatch(super.THREAD_COUNT);
        super.reset();
    }
    
    @Override
    public void runnerIsReady() throws InterruptedException {
        prepare.countDown();
        super.runnerIsReady();
    }

    @Override
    public void judgeFireStart() throws InterruptedException {
        prepare.await();
        super.judgeFireStart();
    }
    
    // ..workerDone() and driverAwaitCompletion() works just the same as they do for the Erroneous class.
}
