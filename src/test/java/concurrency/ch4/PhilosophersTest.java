package concurrency.ch4;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class PhilosophersTest {
	private final int n = 5;
	private final Semaphore[] forks = new Semaphore[n];
	private final CountDownLatch latch = new CountDownLatch(20);

	@Before
	public void setUp() throws Exception {
		for(int i = 0; i < n; i++){
			forks[i] = new Semaphore(1);
		}
	}

	@Test
	public void test() throws InterruptedException {
		for(int i = 0; i < n - 1; i++){
			Executors.newSingleThreadExecutor()
				.execute(new RightHandedPhilosopher(i));
		}
		
		Executors.newSingleThreadExecutor()
			.execute(new LeftHandedPhilosopher(n-1));
		
		latch.await(5, TimeUnit.SECONDS);
	}

	
	private class RightHandedPhilosopher implements Runnable {
		private final int id;
		
		RightHandedPhilosopher(final int id) {
			this.id = id;
		}
		
		public void run() {
			while(true){
				think();
				getForks();
				eat();
				putForks();
				latch.countDown();
			} 
		}

		private void putForks() {
			forks[right()].release();
			forks[left()].release();
		}

		private void eat() {
			System.out.println(toString() + " Eat");
		}

		private void getForks() {
			forks[right()].acquireUninterruptibly();
			forks[left()].acquireUninterruptibly();
		}

		private void think() {
			System.out.println(toString() +" Think");
		}
		
		protected int left(){ return (id + 1) % 5; }
		protected int right(){ return id; }
		
		@Override
		public String toString() {
			return "Philosopher "+id;
		}
		
	}
	
	private class LeftHandedPhilosopher extends RightHandedPhilosopher{

		LeftHandedPhilosopher(int id) {
			super(id);
		}
		
		@Override
		protected int right() {
			return super.left();
		}
		@Override
		protected int left() {
			return super.right();
		}
	}
}
