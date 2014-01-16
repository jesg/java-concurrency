package concurrency.ch5;

import static org.junit.Assert.*;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class H20Test {
	private final int n = 5;
	private final Semaphore mutex = new Semaphore(1);
	private int oxygen = 0;
	private int hydrogen = 0;
	private final CyclicBarrier barrier = new CyclicBarrier(3);
	private final Semaphore oxyQueue = new Semaphore(0);
	private final Semaphore hydroQueue = new Semaphore(0);
	
	private final CountDownLatch latch = new CountDownLatch(3*n + 1);

	@Test
	public void test() throws InterruptedException {
		ExecutorService oxygenPool = Executors.newFixedThreadPool(n);
		for(int i = 0; i < n; i++){
			oxygenPool.submit(new O());
		}
		
		ExecutorService hydrogenPool = Executors.newFixedThreadPool(2*n);
		for(int i = 0; i < 2*n; i++){
			hydrogenPool.submit(new H());
		}
		
		latch.countDown();
		latch.await(5, TimeUnit.SECONDS);
	}
	private class O implements Runnable {

		public void run() {
			latch.countDown();
			try {
				mutex.acquireUninterruptibly();
				++oxygen;
				if( hydrogen >= 2 ){
					System.out.println("=====");
					hydroQueue.acquireUninterruptibly(2);
					hydrogen -= 2;
					oxyQueue.release();
					oxygen -= 1;
				}else{
					mutex.release();
				}
					
				oxyQueue.acquireUninterruptibly();
				System.out.println("Bond O");
				barrier.await();
				
				mutex.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
		}
		
	}
	private class H implements Runnable {

		public void run() {
			latch.countDown();
			
			try {
				mutex.acquireUninterruptibly();
				++hydrogen;
				if(oxygen >= 1 && hydrogen >= 2){
					System.out.println("=====");
					hydroQueue.release(2);
					hydrogen -= 2;
					oxyQueue.release();
					oxygen -= 1;
				}else{
					mutex.release();
				}
				
				hydroQueue.acquireUninterruptibly();
				System.out.println("Bond H");
				barrier.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
		}
		
	}
}
