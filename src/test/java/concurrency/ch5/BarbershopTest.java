package concurrency.ch5;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BarbershopTest {
	private final int total = 30;
	private final int n = 5;
	private int customers = 0;
	private final Semaphore mutex = new Semaphore(1);
	private final Semaphore customer = new Semaphore(0);
	private final Semaphore barber = new Semaphore(0);

	private final ExecutorService customerPool = Executors.newFixedThreadPool(2*n);
	private final CountDownLatch latch = new CountDownLatch(total);
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		latch.await(5, TimeUnit.SECONDS);
	}

	@Test
	public void test() {
		Executors.newSingleThreadExecutor()
			.submit(new Barber());
		
		for(int i = 0; i < total; i++)
			customerPool.submit(new Customer(i));
		
	}
	
	private class Customer implements Runnable {
		private final int id;
		
		Customer(int id){
			this.id = id;
		}
		
		public void run() {
			mutex.acquireUninterruptibly();
			if(customers == n ) {
				mutex.release();
				latch.countDown();
				System.out.println("balk "+ id);
				return;
			}
			++customers;
			mutex.release();
			
			customer.release();
			barber.acquireUninterruptibly();
			
			getHairCut();
			
			mutex.acquireUninterruptibly();
			--customers;
			mutex.release();
			latch.countDown();
		}

		private void getHairCut() {
			System.out.println("customer: "+id+" waiting customers: "+customers);
		}	
	}

	private class Barber implements Runnable {

		public void run() {
			while(true)
				cutHair();
		}

		private void cutHair() {
			customer.acquireUninterruptibly();
			barber.release();
		}
		
	}
}
