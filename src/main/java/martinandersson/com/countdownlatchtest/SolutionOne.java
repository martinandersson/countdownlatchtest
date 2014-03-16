package martinandersson.com.countdownlatchtest;

import java.util.concurrent.CountDownLatch;

/**
 * <p>Represents a working solution to the race-problem where runner threads might miss
 * the start. The fix is to introduce a third <code>CountDownLatch</code> counting down
 * all threads that is <i>prepared</i> for the start signal. As such there is little code
 * we need to add in order to patch the super class <code>Erroneous</code>.</p>
 * 
 * <p>However, there's still one little problem using this solution. Although our
 * <code>prepare</code> field will guarantee that the judge wait for all runners to become
 * prepared, you might sometimes see in the console output that the judge still report that
 * a runner missed the start. Reason is that as soon as the last runner count down the prepare
 * latch to zero, our judge wake up and will fire the start signal. The runner might not have
 * the time to actually reach the start line and will discover that the start signal had already
 * been fired. This race condition could therefore produce a false positive. We know it is
 * and shouldn't care, but a more clean and bullet proof solution is offered as
 * {@linkplain SolutionTwo}.</p>
 * 
 * @author Martin Andersson (webmaster at martinandersson.com)
 */
public class SolutionOne extends Erroneous
{
    private CountDownLatch prepare; // <-- This field rite here is the only difference.
    
    public SolutionOne(int threadCount) {
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
    public void runnerIsReady() throws InterruptedException, MissedStartException {
        prepare.countDown();
        super.runnerIsReady();
    }

    @Override
    public void judgeFireStart() throws InterruptedException {
        prepare.await(); // <-- The judge has to wait for all the runners to become ready. Quite simple huh?
        super.judgeFireStart();
    }
    
    // ..workerDone() and driverAwaitCompletion() works just the same as they do for the Erroneous class.
}
