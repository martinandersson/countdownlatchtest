package martinandersson.com.countdownlatchtest;

/**
 * <p>Illustrates the simple workflow that coordinates a race of runners doing some
 * kind of a marathon thing. The players are the following:</p>
 * 
 * <ul>
 *   <li>A known amount of runners doing the marathon.</li>
 *   <li>A judge who is the owner of the starting pistol.</li>
 * </ul>
 * 
 * <p>Our goal is to prevent all types of false starts. No runner may start the race
 * before the starting pistol has went of, nor do we want a runner to <strong>arrive at
 * </strong> the starting line <strong>after</strong> the race has already begun.</p>
 * 
 * <p>Any <code>InterruptedException</code> thrown at any phase of the workflow will
 * invalidate the marathon/test.
 * 
 * @author Martin Andersson (webmaster at martinandersson.com)
 */
public interface Marathon
{
    /**
     * Marathon implementation must support multiple runs executed consecutively. The
     * marathon runner, or judge, is the manager of each marathon and has the responsibility
     * of calling this method before each marathon begins or after each marathon has completed.
     */
    void reset();
    
    /**
     * Called by each runner <i>when he has reached</i> the starting line and is ready for the start
     * signal. As in real life, anything can happen on the way. He might be hit by a car, there is
     * a certain distance for him to walk et cetera.
     */
    void runnerIsReady() throws InterruptedException;
    
    /**
     * Will be called by the judge after all threads has been created and <i>possibly</i> after a delay.
     */
    void judgeFireStart() throws InterruptedException;
    
        
    /**
     * Will be called by the judge when he is ready to wait for all the runners to finish the race.
     */
    void judgeAwaitCompletion() throws InterruptedException;
    
    
    /**
     * Called by each runner as they hit the finish line (that is instantly as soon as they begin the race).
     */
    void runnerDone();
}
