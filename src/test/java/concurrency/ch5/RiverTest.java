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

public class RiverTest {
	private final CyclicBarrier barrier = new CyclicBarrier(4);
	private final Semaphore mutex = new Semaphore(1);
	private int hackers = 0;
	private int serfs = 0;
	private final Semaphore hackerQueue = new Semaphore(0);
	private final Semaphore serfQueue = new Semaphore(0);
	private int categoryTotal = 10;
	private CountDownLatch latch = new CountDownLatch(categoryTotal*4 + 1);
	
	@Test
	public void test() throws InterruptedException {
		ExecutorService pool = Executors.newFixedThreadPool(4);
		for(int i = 0; i < categoryTotal*2; i++){
			pool.submit(new Serf());
		}
		ExecutorService pool2 = Executors.newFixedThreadPool(4);
		for(int i = 0; i < categoryTotal*2; i++){
			pool2.submit(new Hacker());
		}
		latch.countDown();
		latch.await(5, TimeUnit.SECONDS);
	}

	private class Hacker implements Runnable {

		public void run() {
			boolean isCaptain = false;
			try {
				mutex.acquireUninterruptibly();
				++hackers;
				if( hackers == 4 ) {
					System.out.println("==========");
					hackerQueue.release(4);
					hackers = 0;
					isCaptain = true;
				}else if( hackers == 2 && serfs >= 2){
					System.out.println("==========");
					hackerQueue.release(2);
					serfQueue.release(2);
					hackers = 0;
					serfs -= 2;
					isCaptain = true;
				}else {
					mutex.release();
				}
				
				hackerQueue.acquireUninterruptibly();
				
				System.out.println("Hacker Board ");
			
				barrier.await();
				
				if( isCaptain ) {
					System.out.println("Row boat");
					mutex.release();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private class Serf implements Runnable {

		public void run() {
			boolean isCaptain = false;
			try {
				mutex.acquireUninterruptibly();
				++serfs;
				if( serfs == 4 ) {
					System.out.println("==========");
					serfQueue.release(4);
					serfs = 0;
					isCaptain = true;
				}else if( serfs == 2 && hackers >= 2){
					System.out.println("==========");
					hackerQueue.release(2);
					serfQueue.release(2);
					hackers -= 2;
					serfs = 0;
					isCaptain = true;
				}else {
					mutex.release();
				}
				
				serfQueue.acquireUninterruptibly();
				
				System.out.println("Serf Board ");
			
				barrier.await();
				
				if( isCaptain ) {
					System.out.println("Row boat");
					mutex.release();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
	
		}
		
	}
}
