package concurrency.ch3;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;


public class CyclicBarrierTest {
	private final int n = 10;
	private final CountDownLatch latch = new CountDownLatch(2 * n + 1);
	private final CyclicBarrier barrier = new CyclicBarrier(n);
	
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
				barrier.await();
				
				criticalSection();
			} catch (InterruptedException e) {
				return;
			} catch (BrokenBarrierException e) {
				return;
			}
			
		}
		
		private void criticalSection(){
			System.out.println("Iteration: " + i+ " Thread: "+Thread.currentThread()+ " Time: "+System.currentTimeMillis());
		}
	}
}
