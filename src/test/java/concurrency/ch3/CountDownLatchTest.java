package concurrency.ch3;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.junit.Test;


public class CountDownLatchTest {
	private final int n = 10;
	private final CountDownLatch latch = new CountDownLatch(n);
	
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
				latch.countDown(); // increments count by 1
				latch.await(); // waits for count to reach n
				
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
