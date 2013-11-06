package concurrency.ch3;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.junit.Test;

/**
 * Count to two with two threads.
 * 
 * @author Jason Gowan
 *
 */
public class MutexTest {
	private final Semaphore mutex = new Semaphore(1);
	private final Semaphore done = new Semaphore(0);
	private int count = 0;

	@Test
	public void test() throws InterruptedException {
		callCounter();
		callCounter();
		
		done.acquire(2); // wait for both counters to complete
		System.out.println(count);
		assertEquals(2, count);
	}

	private void callCounter() {
		Executors.newSingleThreadExecutor()
			.submit(new Counter());
	}

	private class Counter implements Runnable{
		
		public void run() {
			try {
				mutex.acquire();
				count = count + 1;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally{
				mutex.release();
				done.release();
			}
			
		}
	}
}
