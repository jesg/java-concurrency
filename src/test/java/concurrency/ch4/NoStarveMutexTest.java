package concurrency.ch4;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class NoStarveMutexTest {
	private int room1 = 0;
	private int room2 = 0;
	private Semaphore mutex = new Semaphore(1);
	private Semaphore t1 = new Semaphore(1);
	private Semaphore t2 = new Semaphore(0);
	private final int n = 10;
	private final CountDownLatch latch = new CountDownLatch(n + 1);
	private final CountDownLatch latch2	= new CountDownLatch(n);

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws InterruptedException {
		for(int i = 0; i < n; i++){
			Executors.newSingleThreadExecutor()
				.submit(new Runnable() {
					
					public void run() {
						latch2.countDown();
						try {
							latch2.await(); // start all threads at the same time
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						mutex.acquireUninterruptibly();
						++room1;
						mutex.release();
						
						t1.acquireUninterruptibly();
						++room2;
						mutex.acquireUninterruptibly();
						--room1;
						
						if( room1 == 0 ){
							mutex.release();
							t2.release();
						}else{
							mutex.release();
							t1.release();
						}
						
						t2.acquireUninterruptibly();
						--room2;
						
						System.out.println(Thread.currentThread());
						
						if( room2 == 0 ){
							t1.release();
						}else{
							t2.release();
						}
						
						latch.countDown();
					}
				});
		}
		
		latch.countDown();
		latch.await(5, TimeUnit.SECONDS);
	}

}
