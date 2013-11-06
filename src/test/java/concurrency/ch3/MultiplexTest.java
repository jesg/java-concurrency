package concurrency.ch3;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.junit.Test;


public class MultiplexTest {
	private final int n = 10;
	private final Semaphore multiplex = new Semaphore(n);
	private final Semaphore done = new Semaphore(0);
	private int count = 0;
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	@Test
	public void test() throws InterruptedException {
		for(int i = 0; i<100; i++){
			executor.submit(new Counter());
		}
		
		done.acquire(100); // wait for all 100 counters to complete
		System.out.println(count);
		assertEquals(100, count);
	}

	private class Counter implements Runnable{
		
		public void run() {
			try {
				multiplex.acquire();
				count = count + 1;
			} catch (InterruptedException e) {
			}finally{
				multiplex.release();
				done.release();
			}
			
		}
		
	}

}
