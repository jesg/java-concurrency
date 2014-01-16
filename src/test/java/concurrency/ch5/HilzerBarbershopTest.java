package concurrency.ch5;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import concurrency.ch3.FifoQueue;

public class HilzerBarbershopTest {
	private final int total = 100;
	private final int n = 4;
	private int customers = 0;
	private final Semaphore mutex = new Semaphore(1);
	private final BlockingQueue<Class<Void>> standingRoom = new ArrayBlockingQueue<Class<Void>>(16, true);
	private final BlockingQueue<Class<Void>> sofa = new ArrayBlockingQueue<Class<Void>>(4, true);
	private final Semaphore chair = new Semaphore(3);
	private final Semaphore barber = new Semaphore(0);
	private final Semaphore customer = new Semaphore(0);
	private final Semaphore cash = new Semaphore(0);
	private final Semaphore receipt = new Semaphore(0);
	
	private final ExecutorService customerPool = Executors.newFixedThreadPool(n);
	private final ExecutorService barberPool = Executors.newFixedThreadPool(3);
	private final CountDownLatch latch = new CountDownLatch(total + 1);

	@Test
	public void test() throws InterruptedException {
		for(int i = 0; i < 3; i++)
			barberPool.submit(new Barber(i));
		
		for(int i = 0; i < total; i++)
			customerPool.submit(new Customer(i));
		
		latch.countDown();
		latch.await(10, TimeUnit.SECONDS);
	}
	
	private class Customer implements Runnable {
		private final int id;
		
		Customer(int id){
			this.id = id;
		}
		
		public void run() {

			try{
				
				mutex.acquireUninterruptibly();
				if(customers == 20 ) {
					mutex.release();
					exitShop();
					System.out.println("Full");
					return;
				}
				++customers;
				mutex.release();
				
				standingRoom.put(Void.TYPE);
				enterShop();
				
				sofa.put(Void.TYPE);
				sitOnSofa();
				standingRoom.take();
				
				chair.acquireUninterruptibly();
				sitInBarberChair();
				sofa.take();
				
				customer.release();
				barber.acquireUninterruptibly();
				getHairCut();
				chair.release(); // the books solution forgets to release the chair
				
				pay();
				cash.release();
				receipt.acquireUninterruptibly();
				
				mutex.acquireUninterruptibly();
				--customers;
				mutex.release();
				
				exitShop();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally{
				latch.countDown();
			}
		}

		private void pay() {
			System.out.println("Pay " + id);
		}

		private void sitInBarberChair() {
			System.out.println("Sit in barber chair " + id);
		}

		private void sitOnSofa() {
			System.out.println("Sit on Sofa " + id);
		}

		private void enterShop() {
			System.out.println("Enter shop " + id);
		}

		private void exitShop() {
			System.out.println("Exit shop " + id);
		}

		private void getHairCut() {
			System.out.println("customer: "+id+" waiting customers: "+customers);
		}	
	}

	private class Barber implements Runnable {
		private final int id;
		
		public Barber(int id) {
			this.id = id;
		}

		public void run() {
			for(;;){
				customer.acquireUninterruptibly();
				barber.release();
				cutHair();
				
				cash.acquireUninterruptibly();
				acceptPayment();
				receipt.release();
			}
		}

		private void acceptPayment() {
			System.out.println("Barber " + id + " accept payment");
		}

		private void cutHair() {
			System.out.println("Barber " + id + " cut hair.");
		}
		
	}
}
