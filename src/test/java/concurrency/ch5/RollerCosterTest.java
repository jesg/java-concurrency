package concurrency.ch5;

import static org.junit.Assert.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RollerCosterTest {
	private final int n = 5;
	private final int c = 5;
	private final Semaphore mutex = new Semaphore(1);
	private final Semaphore mutex2 = new Semaphore(1);
	private int boarders = 0;
	private int unboarders = 0;
	private final Semaphore boardQueue = new Semaphore(0);
	private final Semaphore unboardQueue = new Semaphore(0);
	private final Semaphore allAboard = new Semaphore(0);
	private final Semaphore allAshore = new Semaphore(0);
	private final CountDownLatch latch = new CountDownLatch(n + 1);
	private final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

	@Test
	public void test() throws InterruptedException {
		Executors.newSingleThreadExecutor().submit(new Car());
		
		ExecutorService passengerPool = Executors.newFixedThreadPool(n);
		for(int i = 0; i < n; i++)
			passengerPool.submit(new Passenger(i));
		
		latch.countDown();
		latch.await(5, TimeUnit.SECONDS);
		for(String result : queue){
			System.out.println(result);
		}
	}

	private class Car implements Runnable {

		public void run() {
			for(;;){
				load();
				boardQueue.release(c);
				
				allAboard.acquireUninterruptibly();
				_run();
				
				unboard();
				unboardQueue.release(c);
				allAshore.acquireUninterruptibly();
			}
		}

		private void unboard() {
//			System.out.println("Unboard");
			queue.add("Unboard");
		}

		private void _run() {
//			System.out.println("Run");
			queue.add("Run");
		}

		private void load() {
//			System.out.println("Load");
			queue.add("Load");
		}
		
	}
	
	private class Passenger implements Runnable{
		private final int id;
		
		Passenger(int id){
			this.id = id;
		}

		public void run() {
			
			mutex.acquireUninterruptibly();
			++boarders;
			if( boarders == c ) {
				allAboard.release();
				boarders = 0;
			}
			mutex.release();
			
			boardQueue.acquireUninterruptibly();
			board();
			
			unboardQueue.acquireUninterruptibly();
			unboard();
			
			mutex2.acquireUninterruptibly();
			++unboarders;
			if( unboarders == c ){
				allAshore.release();
				unboarders = 0;
			}
			mutex2.release();
			latch.countDown();
		}

		private void unboard() {
//			System.out.println("Unboard " + id);
			queue.add("Unboard " + id);
		}

		private void board() {
//			System.out.println("Board " + id);
			queue.add("Board " + id);
		}
		
	}
}
