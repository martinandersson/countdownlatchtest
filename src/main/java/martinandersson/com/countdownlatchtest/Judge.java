package martinandersson.com.countdownlatchtest;

import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import martinandersson.com.countdownlatchtest.Marathon.MissedStartException;

/**
 * <h1>About this class</h1>
 * 
 * <p>The judge is the one responsible for starting a <code>Marathon</code>. He create a
 * new thread for each runner. Not until he has spawned all the runners, is he allowed to
 * start the race.</p>
 * 
 * <p>What makes the judge particular interesting is that he accepts parameters that will
 * govern whether or not the judge, and/or the runners, spend time doing some other errand
 * before they are ready to really participate in the start of the race.</p>
 * 
 * <p>This class and all related types is a demonstration that the first example provided
 * in the JavaDoc of {@link CountDownLatch} is not sufficient to achieve a similar task.
 * The Oracle provided example can only guarantee that no threads start <i>prior</i> to the
 * start signal, but the possibility of threads <i>missing</i> the start signal all together
 * and not spending time waiting for the signal is still there. Obviously not what we would
 * want in a real life application doing a marathon!</p>
 * 
 * <p>See {@linkplain Marathon} for an in-depth walkthrough of the workflow.</p>
 * 
 * <h1>Results using author's machine with hard coded defaults</h1>
 * 
 * <p><u>The Oracle example</u> will only make it, that is, no runners/threads will miss their
 * start if we impose an admin thread/judge delay.</p>
 * 
 * <p>If we use <u>no delays</u>, we can see that threads begin missing the start signal if only
 * just a low ratio of them. That is not okay. I reason this should be the normal behavior of
 * application code using a "larger" amount of threads. The application will see that the main
 * driver finishes creating new threads quite fast and then simply fires the start signal. Yet
 * the operating system hasn't had time enough to schedule all the workers.</p>
 * 
 * <p>If we on the other hand impose a worker thread/runner <u>delay</u>, <strong>all</strong> runners
 * will miss the start signal!<p>
 * 
 * <p>Using any of the two provided <u>solutions</u> made <strong>no</strong> threads miss their start,
 * independently of the previously described conditions.</p>
 * 
 * @author Martin Andersson (webmaster at martinandersson.com)
 */
public class Judge
{
    public static void main(String... ignored) throws InterruptedException
    {
    	final int runners = 50, /* is ms: */ delay = 10, noDelay = 0;
    	
    	
    	
        // Oracle assumptions; driver thread do something else before firing the signal:
        Judge driverDelay = new Judge(runners, delay, noDelay);
        
        // Probably more close to real world applications, everything happens as fast as possible:
        Judge noDelays = new Judge(runners, noDelay, noDelay);
        
        // What if worker threads are the ones that take time before reaching the starting line?
        Judge workerDelay = new Judge(runners, noDelay, delay);
        
        
        
        // Now execute all using the Oracle JavaDoc technique versus the solutions:
        
        Judge[] judges = { driverDelay, noDelays, workerDelay };
        
        Marathon oracleJavadoc = new Erroneous(runners);
        for (Judge j : judges) {
        	j.start(oracleJavadoc);
        }
        
        System.out.println(); // <-- print newline
        
        Marathon solutionOne = new SolutionOne(runners);
        for (Judge j : judges) {
        	j.start(solutionOne);
        }
        
        System.out.println();
        
        Marathon solutionTwo = new SolutionTwo(runners);
        for (Judge j : judges) {
        	j.start(solutionTwo);
        }
    }
    
    
    
    private final int runnerCount, judgeDelay, runnerDelay;
    
    private volatile boolean raceInterrupted;
    
    /**
     * Will keep count of all the worker/runner threads that missed the race start signal meaning
     * the runner arrived at the starting line of the marathon after the judge fired his starting
     * pistol.
     */
    private AtomicInteger missed;
    
    
    
    /**
     * All delays in number of milliseconds.
     */
    public Judge(int runnerCount, int judgeDelay, int runnerDelay)
    {
        this.runnerCount = runnerCount;
        this.judgeDelay = judgeDelay;
        this.runnerDelay = runnerDelay;
    }
    
    
    
    public void start(final Marathon test)
    {
        if (raceInterrupted)
            throw new AssertionError("Restarting a previously interrupted marathon could produce undesirable results.");
        
        log(test);
        execute(test);
        
        if (raceInterrupted)
            System.out.println("Marathon failure. Some runner thread was interrupted before completing a sleep.");
        else
            System.out.println("\tAmount of runners (threads) that missed their start: " + missed.get());
    }
    
    private void log(Marathon test)
    {
        System.out.println(
                MessageFormat.format("Running {0} using amount of workers: {1}, admin delay: {2} (ms), worker delay: {3} (ms).",
                        test.getClass().getSimpleName(), // {0} 
                        runnerCount,                     // {1}
                        judgeDelay,                      // {2}
                        runnerDelay));                   // {3}
    }
    
    private void execute(final Marathon test)
    {
        this.reset();
        test.reset();
        
        
        // Create and start all worker threads:
        
        int leftToDo = this.runnerCount;
        
        while (leftToDo-- > 0) {
        	Thread t = newRunner(test);
        	t.start();
        }
        
        
        // Perhaps our judge need to take a piss before starting the race?
        
        try {
            sleep(judgeDelay); }
        
        catch (InterruptedException e) {
            doInterrupt();
            return; }
        
        
        // Start the race:
        
        try {
	        test.judgeFireStart(); }
        
        catch (InterruptedException e) {
	        doInterrupt();
	        return; }
        
        try {
            test.judgeAwaitCompletion(); }
        
        catch (InterruptedException e) {
            doInterrupt(); }
    }
    
    
    void reset()
    {
        raceInterrupted = false;
        missed = new AtomicInteger();
    }
    
    private Thread newRunner(final Marathon test)
    {
        Thread t = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
            	// Before start, the worker might want to go and take a piss too:
            	
                try {
                    sleep(runnerDelay); }
                
                catch (InterruptedException e) {
                    doInterrupt();
                    return; }
                
                // Okay all done:
                
                try {
                    test.runnerIsReady(); }
                
                catch (MissedStartException e) {
                	missed.incrementAndGet();
                }
                
                catch (InterruptedException e) {
                    doInterrupt();
                    return; }
                
                // ..here's what a marathon race is all about: Nothing.
                
                test.runnerDone();
            }
        });
        
        t.setDaemon(true);
        return t;
    }
    
    private void doInterrupt()
    {
    	this.raceInterrupted = true;
        Thread.currentThread().interrupt();
    }
    
    
    
    /**
     * Will guard against spurious wakeups. Invoking thread will not return from this
     * method call until at least the provided amount of milliseconds has passed or
     * <code>InterruptedException</code> happened.
     */
    private static void sleep(final long milliseconds) throws InterruptedException
    {
        if (milliseconds <= 0L)
            return;
        
        final long mayReturn = System.currentTimeMillis() + milliseconds;
        
        while (System.currentTimeMillis() < mayReturn)
            TimeUnit.MILLISECONDS.sleep(mayReturn - System.currentTimeMillis()); // <-- Noop if value <= 0
    }
}
