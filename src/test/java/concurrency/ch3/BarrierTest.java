package concurrency.ch3;

import static org.junit.Assert.*;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;

public class BarrierTest {
	private final int n = 10;
	private final Semaphore barrier = new Semaphore(0);
	private final Semaphore mutex = new Semaphore(1);
	private int count = 0;
	
	@Test
	public void test() {
		for(int i = 0; i < 10; i++){
			Executors.newSingleThreadExecutor()
				.submit(new Critical());
		}
	}

	private class Critical implements Runnable {

		public void run() {
			try {
				mutex.acquire();
			} catch (InterruptedException e) {
				throw new RuntimeException("Unable to aquire mutex", e);
			}
			count++;
			mutex.release();
			
			if( count == n ) barrier.release();
			
			try {
				/*
				 * The following is a turnstile.  A turnstile allows one thread to pass at a time.
				 */
				barrier.acquire();
				barrier.release();
				
				criticalSection();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		private void criticalSection(){
			System.out.println("Thread: "+Thread.currentThread()+ " Time: "+System.currentTimeMillis());
		}
	}
}
