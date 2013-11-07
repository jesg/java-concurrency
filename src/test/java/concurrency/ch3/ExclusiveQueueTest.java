package concurrency.ch3;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This is the solution to a non-exclusive queue.
 * 
 * @author Jason Gowan
 *
 */
public class ExclusiveQueueTest {
	private final int n = 100;
	private int leaders = 0;
	private int followers = 0;
	private final Semaphore mutex = new Semaphore(1);
	private final Semaphore leaderQueue = new Semaphore(0);
	private final Semaphore followerQueue = new Semaphore(0);
	private final Semaphore rendezvous = new Semaphore(0);
	private ExecutorService executor = Executors.newCachedThreadPool();

	@After
	public void cleanUp() throws InterruptedException{
		executor.awaitTermination(5, TimeUnit.SECONDS);
	}

	@Test
	public void test() {
		for(int i = 0; i < n; i++){
			final int index = i;
			//leader
			executor.submit(new Runnable(){

				public void run() {
					mutex.acquireUninterruptibly();
					if(followers > 0){
						--followers;
						followerQueue.release();
					}else{
						++leaders;
						mutex.release();
						leaderQueue.acquireUninterruptibly();
					}
					
					dance("leader "+index);
					rendezvous.acquireUninterruptibly();
					mutex.release();
				}
				
			});
			
			//follower
			executor.submit(new Runnable(){

				public void run() {
					mutex.acquireUninterruptibly();
					if(leaders > 0){
						--leaders;
						leaderQueue.release();
					}else{
						++followers;
						mutex.release();
						followerQueue.acquireUninterruptibly();
					}
					
					dance("follower " + index);
					rendezvous.release();
				}
				
			});
		}
		
	}

	private void dance(String type) {
		System.out.println("Dance " + type);
	}
}
