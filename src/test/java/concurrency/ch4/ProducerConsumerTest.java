package concurrency.ch4;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class ProducerConsumerTest {
	private final Semaphore mutex = new Semaphore(1);
	private final Semaphore items = new Semaphore(0);
	private Queue<String> buffer = new LinkedList<String>();
	private final CountDownLatch latch = new CountDownLatch(3);

	@Test
	public void test() throws InterruptedException {
		Executors.newSingleThreadExecutor().execute(new Consumer());
		Executors.newSingleThreadExecutor().execute(new Producer());
		
		// let the threads execute before terminating
		latch.countDown();
		latch.await(5, TimeUnit.SECONDS);
	}
	
	private class Producer implements Runnable{

		public void run() {
			String event = "Hello World!";
			
			mutex.acquireUninterruptibly();
			buffer.add(event);
			items.release();
			mutex.release();
			
			latch.countDown();
		}
	}
	
	private class Consumer implements Runnable {

		public void run() {
			
			items.acquireUninterruptibly();
			
			mutex.acquireUninterruptibly();
			String event = buffer.poll();
			mutex.release();
			
			System.out.println(event);
			latch.countDown();
		}
		
	}

}
