package concurrency.ch4;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ProducerConsumerFiniteBufferTest {
	private final int n = 20;
	private final int bufferSize = 2;
	private final Semaphore mutex = new Semaphore(1);
	private final Semaphore items = new Semaphore(0);
	private Queue<String> buffer = new ArrayDeque<String>(bufferSize);
	private final Semaphore spaces = new Semaphore(bufferSize);
	private final CountDownLatch latch = new CountDownLatch(20*2 + 1);
	
	private Executor consumerExecutor = Executors.newSingleThreadExecutor();
	private Executor producerExecutor = Executors.newFixedThreadPool(2);

	@Test
	public void test() throws InterruptedException {
		
		consumerExecutor.execute(new Consumer());
		producerExecutor.execute(new Producer());
		producerExecutor.execute(new Producer());
		
		// let the threads execute before terminating
		latch.countDown();
		latch.await(5, TimeUnit.SECONDS);
	}
	
	private class Producer implements Runnable{

		public void run() {
			for(int i = 0; i < n/2; i++){
				String event = "Hello World!";
				
				spaces.acquireUninterruptibly();
				mutex.acquireUninterruptibly();
				
				buffer.add(event);
				
				items.release();
				mutex.release();
				
				latch.countDown();
			}
		}
	}
	
	private class Consumer implements Runnable {

		public void run() {
			for(int i = 0; i < n; i++){
				items.acquireUninterruptibly();
				
				mutex.acquireUninterruptibly();
				
				String event = buffer.poll();
				
				mutex.release();
				spaces.release();
				
				System.out.println(event);
				latch.countDown();
			}
		}
		
	}

}
