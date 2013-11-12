package concurrency.ch5;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SavagesTest {
	private final int m = 5;
	private int servings = 0;
	private final Semaphore mutex = new Semaphore(1);
	private final Semaphore emptyPot = new Semaphore(0);
	private final Semaphore fullPot = new Semaphore(0);
	
	private final ExecutorService savagePool = Executors.newFixedThreadPool(4);
	private final ExecutorService cookPool = Executors.newSingleThreadExecutor();
	private final CountDownLatch latch = new CountDownLatch(20);

	@After
	public void tearDown() throws Exception {
		latch.await(5, TimeUnit.SECONDS);
		savagePool.shutdown();
		cookPool.shutdown();
	}

	@Test
	public void test() {
		savagePool.submit(new Savage());
		savagePool.submit(new Savage());
		savagePool.submit(new Savage());
		
		cookPool.submit(new Cook());
	}

	private class Savage implements Runnable {

		public void run() {
			while(true){
				mutex.acquireUninterruptibly();
				if(servings == 0) {
					emptyPot.release();
					fullPot.acquireUninterruptibly();
					
				}
				--servings;
				getServingFromPot();
				mutex.release();
				
				eat();
				latch.countDown();
			}
		}

		private void eat() {
			System.out.println("eat ");
		}

		private void getServingFromPot() {
			System.out.println("Servings "+servings);
		}
		
	}
	
	private class Cook implements Runnable {

		public void run() {
			while(true){
				emptyPot.acquireUninterruptibly();
				putServingsInPot();
				fullPot.release();
			}
		}

		private void putServingsInPot() {
			servings += m;
			System.out.println("put servings");
		}
		
	}
}
