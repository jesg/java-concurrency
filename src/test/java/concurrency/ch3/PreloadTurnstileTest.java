package concurrency.ch3;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.junit.Test;


public class PreloadTurnstileTest {
	private final int n = 10;
	private final CountDownLatch latch = new CountDownLatch(2 * n + 1);
	private final Semaphore turnstile2 = new Semaphore(0);
	private final Semaphore turnstile = new Semaphore(0);
	private final Semaphore mutex = new Semaphore(1);
	private int count = 0;
	
	@Test
	public void test() throws InterruptedException {
		ExecutorService[] executors = new ExecutorService[n];
		
		for(int i = 0; i < n; i++){
			executors[i] = Executors.newSingleThreadExecutor();
			executors[i].submit(new Critical(1));
		}
		
		for(int i = 0; i < n; i++){
			executors[i].submit(new Critical(2));
		}
		
		// wait for all 20 critical sections to complete execution
		latch.countDown();
		latch.await();
	}

	private class Critical implements Runnable {
		private final int i;
		
		Critical(int i){
			this.i = i;
		}

		public void run() {
			latch.countDown();
			try {
				mutex.acquire();
				count++;
				if( count == n ) {
					turnstile.release(n);
				}
				mutex.release();
			} catch (InterruptedException e) {
				throw new RuntimeException("Unable to aquire mutex", e);
			}
			
			try {
				turnstile.acquire();
				
				criticalSection();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			try {
				mutex.acquire();
				count--;
				if( count == 0 ) {
					turnstile2.release(n);
				}
				mutex.release();
			} catch (InterruptedException e) {
				throw new RuntimeException("Unable to aquire mutex", e);
			}

			try {
				turnstile2.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		private void criticalSection(){
			System.out.println("Iteration: " + i+ " Thread: "+Thread.currentThread()+ " Time: "+System.currentTimeMillis());
		}
	}
}
