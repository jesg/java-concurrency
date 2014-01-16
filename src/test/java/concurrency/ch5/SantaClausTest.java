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

public class SantaClausTest {
	private final int elveCount = 50;
	private final int raindeerCount = 9;
	private int elves = 0;
	private int reindeer = 0;
	private final Semaphore santaSem = new Semaphore(0);
	private final Semaphore reindeerSem = new Semaphore(0);
	private final Semaphore elveSem = new Semaphore(0);
	private final Semaphore elfTex = new Semaphore(1);
	private final Semaphore mutex = new Semaphore(1);
	private final CountDownLatch latch = new CountDownLatch(11);

	@Test
	public void test() throws InterruptedException {
		Executors.newSingleThreadExecutor().submit(new Santa());
		
		ExecutorService elveService = Executors.newFixedThreadPool(4);
		for(int i = 0; i < elveCount; i++)
			elveService.submit(new Elve(i));
		
		ExecutorService raindeerService = Executors.newFixedThreadPool(9);
		for(int i = 0; i < raindeerCount; i++)
			raindeerService.submit(new Reindeer(i));
		
		latch.countDown();
		latch.await(10, TimeUnit.SECONDS);
	}
	
	private class Santa implements Runnable {

		public void run() {
			for(;;){
				santaSem.acquireUninterruptibly();
				mutex.acquireUninterruptibly();
				
				if( reindeer == 9 ) {
					prepareSleigh();
					reindeerSem.release(9);
					break;
				}else if( elves == 3 ){
					helpElves();
					elveSem.release(3);
				}
				
				mutex.release();
			}
		}
		
		private void prepareSleigh(){
			System.out.println("Prepare sleigh");
			latch.countDown();
		}
		
		private void helpElves(){
			System.out.println("Help Elves " + elves);
		}
	}
	
	private class Reindeer implements Runnable {
		private final int id;
		
		public Reindeer(int id) {
			this.id = id;
		}

		public void run() {
			mutex.acquireUninterruptibly();
			++reindeer;
			if( reindeer == 9 ) santaSem.release();
			mutex.release();
			
			reindeerSem.acquireUninterruptibly();
			getHitched();
		}
		
		private void getHitched(){
			System.out.println("Reindeer " + id + " hitched");
			latch.countDown();
		}
	}
	
	private class Elve implements Runnable {
		private final int id;
		
		public Elve(int id) {
			this.id = id;
		}


		public void run() {
			elfTex.acquireUninterruptibly();
			
			mutex.acquireUninterruptibly();
			
			++elves;
			if( elves == 3 ) 
				santaSem.release();
			else
				elfTex.release();
			
			mutex.release();
			
			elveSem.acquireUninterruptibly();
			getHelp();
			
			mutex.acquireUninterruptibly();
			--elves;
			if( elves == 0 ) elfTex.release();
			mutex.release();
		}
		
		private void getHelp(){
			System.out.println("Elve help " + id);
		}
	}
}
