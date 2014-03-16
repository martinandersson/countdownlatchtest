package martinandersson.com.countdownlatchtest;

import java.util.concurrent.CountDownLatch;

/**
 * <p>Building on the same idea as in {@linkplain SolutionOne}, it is possible to use only two CountDownLatche's
 * and use the same amount of code as the original {@linkplain Erroneous} class do. Except offering a real
 * solution, this implementation doesn't report false positives either.</p>
 * 
 * <h1>Here's how it work.</h1>
 * 
 * <p>Set the count of the start count down latch to the amount of runners + 1 (the judge). Then make
 * the runners call <code>start.countDown()</code> before <code>start.await()</code>. This way, all threads,
 * both runners and the judge will cooperatively wait at the start line for each other. Not until all threads
 * has reached the start line will the race begin.</p>
 * 
 * <p>Please compare this class with the original example supplied by the {@linkplain CountDownLatch} JavaDoc
 * (realized in {@linkplain Erroneous}). The only difference is two lines of code.
 * 
 * @author Martin Andersson (webmaster at martinandersson.com)
 */
public class SolutionTwo implements Marathon
{
    private final int THREAD_COUNT;
    
    private CountDownLatch start, done;
    
    public SolutionTwo(int threadCount)
    {
        THREAD_COUNT = threadCount;
        reset();
    }
    
    
    
    /*
     * -------------
     * | OVERRIDES |
     * -------------
     */

    @Override
    public void reset() {
        start = new CountDownLatch(THREAD_COUNT + 1); // <-- All runners + the judge.
        done = new CountDownLatch(THREAD_COUNT);      // <-- Same as before: only runners.
    }

    @Override
    public void runnerIsReady() throws InterruptedException, MissedStartException
    {
        if (start.getCount() == 0L)
            throw new MissedStartException();
        
        start.countDown(); // <-- Using the start as a signal for reaching the starting line..
        start.await();     // <-- ..and then wait for the judge (or everybody else for that matter).
    }

    @Override
    public void judgeFireStart() throws InterruptedException {
        start.countDown(); // <-- Counter will not reach zero until ALL THREADS (including the runners) has invoked countDown().
        
        // If you need the judge to be synchronized too, just add a call here to start.await().
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
