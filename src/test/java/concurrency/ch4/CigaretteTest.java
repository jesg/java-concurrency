package concurrency.ch4;

import static org.junit.Assert.*;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CigaretteTest {
	private final Semaphore mutex = new Semaphore(1);
	private final Semaphore agent = new Semaphore(1);
	private boolean isTobacco = false;
	private boolean isPaper = false;
	private boolean isMatch = false;
	private final Semaphore tobacco = new Semaphore(0);
	private final Semaphore paper = new Semaphore(0);
	private final Semaphore match = new Semaphore(0);
	
	private final CountDownLatch latch = new CountDownLatch(20);
	private final ExecutorService agentService = Executors.newFixedThreadPool(3);
	private final ExecutorService smokerService = Executors.newFixedThreadPool(3);

	@After
	public void cleanUp() throws InterruptedException{
		latch.await(5, TimeUnit.SECONDS);
	}

	@Test
	public void test() {
		Executors.newSingleThreadExecutor()
			.submit(new Agent());
		
		smokerService.submit(new Smoker("tobacco", tobacco));
		smokerService.submit(new Smoker("paper", paper));
		smokerService.submit(new Smoker("match", match));
		
		agentService.submit(new A());
		agentService.submit(new B());
		agentService.submit(new C());
	}

	private class Smoker implements Runnable{
		private final String name;
		private final Semaphore sem;
		
		Smoker(String name, Semaphore sem) {
			this.name = name;
			this.sem = sem;
		}

		public void run() {
			while(true){
				sem.acquireUninterruptibly();
				makeCigarette();
				agent.release();
				smoke();
				latch.countDown();
			}
		}

		private void smoke() {
			System.out.println(name + " smoke");
		}

		private void makeCigarette() {
			System.out.println(name + " make cigarette");
		}
		
	}
	
	private class Agent implements Runnable{
		private final ThreadLocalRandom random = ThreadLocalRandom.current(); // new in java 7
		private final Semaphore[] smokers = new Semaphore[]{tobacco, paper, match}; 

		public void run() {
			while(true){
				agent.acquireUninterruptibly();
				smokers[random.nextInt(0, 3)].release();
			}
		}
		
	}
	private class A implements Runnable{

		public void run() {
			while(true){
				tobacco.acquireUninterruptibly();
				
				mutex.acquireUninterruptibly();
				if(isPaper){
					isPaper = false;
					match.release();
				}else if(isMatch){
					isMatch = false;
					paper.release();
				}else{
					isTobacco = true;
				}
				mutex.release();
			}
		}
		
	}
	private class B implements Runnable{

		public void run() {
			while(true){
				paper.acquireUninterruptibly();
				
				mutex.acquireUninterruptibly();
				if(isTobacco){
					isTobacco= false;
					match.release();
				}else if(isMatch){
					isMatch = false;
					tobacco.release();
				}else{
					isPaper = true;
				}
				mutex.release();
			}
		}	
	}
	private class C implements Runnable{

		public void run() {
			while(true){
				match.acquireUninterruptibly();
				
				mutex.acquireUninterruptibly();
				if(isTobacco){
					isTobacco= false;
					paper.release();
				}else if(isPaper){
					isPaper = false;
					tobacco.release();
				}else{
					isMatch = true;
				}
				mutex.release();
			}
		}	
	}
}
