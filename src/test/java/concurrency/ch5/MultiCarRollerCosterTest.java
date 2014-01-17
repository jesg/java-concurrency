package concurrency.ch5;

import static org.junit.Assert.*;

import java.util.concurrent.ArrayBlockingQueue;
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

public class MultiCarRollerCosterTest {
	private final int n = 15;
	private final int c = 5;
	private static final int m = 3;
	private final Semaphore mutex = new Semaphore(1);
	private final Semaphore mutex2 = new Semaphore(1);
	private int boarders = 0;
	private int unboarders = 0;
	private final Semaphore boardQueue = new Semaphore(0);
	private final Semaphore unboardQueue = new Semaphore(0);
	private final Semaphore allAboard = new Semaphore(0);
	private final Semaphore allAshore = new Semaphore(0);
	
	private final Semaphore[] loadingArea = new Semaphore[m];
	private final Semaphore[] unloadingArea = new Semaphore[m];
	
	private final CountDownLatch latch = new CountDownLatch(n + 1);
	private final BlockingQueue<Class<Void>> queue = new ArrayBlockingQueue<Class<Void>>(m);

	private static int next(int i){
		return (i + 1) % m;
	}
	@Test
	public void test() throws InterruptedException {
		for(int i = 0; i < m; i++){
			loadingArea[i] = new Semaphore(0);
			unloadingArea[i] = new Semaphore(0);
		}
		
		loadingArea[0].release();
		unloadingArea[0].release();
		
		ExecutorService carPool = Executors.newFixedThreadPool(m);
		for(int i = 0; i < m; i++){
			carPool.submit(new Car(i));
		}
		
		ExecutorService passengerPool = Executors.newFixedThreadPool(n);
		for(int i = 0; i < n; i++)
			passengerPool.submit(new Passenger(i));
		
		latch.countDown();
		latch.await(5, TimeUnit.SECONDS);
	}

	private class Car implements Runnable {
		private final int id;
		
		Car(int id){
			this.id = id;
		}

		public void run() {
			loadingArea[id].acquireUninterruptibly();
			load();
            boardQueue.release(c);
                                
            allAboard.acquireUninterruptibly();
            loadingArea[next(id)].release();
            _run();
            
            unloadingArea[id].acquireUninterruptibly();
            unboard();
            unboardQueue.release(c);
            allAshore.acquireUninterruptibly();
            unloadingArea[next(id)].release();
		}

		private void unboard() {
			System.out.println("Unboard Car " + id);
		}

		private void _run() {
			System.out.println("Run Car " + id);
		}

		private void load() {
			System.out.println("Load Car " + id);
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
			System.out.println("Unboard " + id);
		}

		private void board() {
			System.out.println("Board " + id);
		}
		
	}
}
