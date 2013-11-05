package concurrency;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.junit.Test;

/**
 * Section 3.1 Signaling
 * 
 * Use a semaphore to print A1 before B1.
 * 
 * @author jesg
 *
 */
public class SemiphoreTest {
	/**
	 * Initialize the semaphore with no permits.
	 * 
	 * Remove the final keyword and see what happens.
	 */
	public final Semaphore semaphore = new Semaphore(0);

	@Test
	public void test() {
		
		/*
		 * Execute A1 on a single thread
		 */
		Executors.newSingleThreadExecutor()
			.submit(new Runnable() {
			
			public void run() {
				System.out.println("A1");
				semaphore.release(); // signal B1 to execute
			}
		});
		
		Executors.newSingleThreadExecutor()
			.submit(new Runnable() {
			
			public void run() {
				try {
					semaphore.acquire(); // block until A1 passes a permit
					System.out.println("B1");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		
	}

}
