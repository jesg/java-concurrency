package concurrency.ch4;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class ReaderWriterLightSwitchTest {
	private String mutableData = "Initial Value";
	private final LightSwitch lightSwitch = new LightSwitch();
	private final Semaphore roomEmpty = new Semaphore(1);
	private final CountDownLatch latch = new CountDownLatch(31);
	
	private Executor executor = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	@Test
	public void test() throws InterruptedException {
		for(int i = 0; i < 20; i++){
			final int index = i;
			executor.execute(new Reader());
			if(i%2 == 0) executor.execute(new Writer(index));
		}
		
		latch.countDown();
		latch.await(5, TimeUnit.SECONDS);
	}
	
	private class Reader implements Runnable {

		public void run() {
			lightSwitch.lock(roomEmpty);
			
			System.out.println(mutableData);
			
			lightSwitch.unlock(roomEmpty);
			
			latch.countDown();
		}
		
	}
	
	private class Writer implements Runnable{
		private final int id;
		
		Writer(final int id){
			this.id = id;
		}
		
		public void run() {
			
			roomEmpty.acquireUninterruptibly();
			mutableData = "Writer " + id;
			roomEmpty.release();
			
			latch.countDown();
		}
	}

}
