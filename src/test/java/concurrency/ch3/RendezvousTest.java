package concurrency.ch3;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.junit.Test;

/**
 * - a1 must occur before b2
 * - b1 must occur before a2
 * 
 * @author Jason Gowan
 *
 */
public class RendezvousTest {
	public final Semaphore a = new Semaphore(0);
	public final Semaphore b = new Semaphore(0);
	
	@Test
	public void test() {
		Executors.newSingleThreadExecutor()
			.submit(new Runnable() {
			
			public void run() {
				System.out.println("a1");
				a.release(); // signal b2
				try {
					b.acquire();
					System.out.println("a2");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		});
		Executors.newSingleThreadExecutor()
			.submit(new Runnable() {
			
			public void run() {
				System.out.println("b1");
				b.release(); // signal a2
				try {
					a.acquire();
					System.out.println("b2");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		});
	}

}
