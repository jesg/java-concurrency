package concurrency.ch4;

import java.util.concurrent.Semaphore;

public class LightSwitch {
	private int counter = 0;
	private final Semaphore mutex = new Semaphore(1);
	
	public void lock(Semaphore semaphore){
		mutex.acquireUninterruptibly();
		++counter;
		if(counter == 1) semaphore.acquireUninterruptibly();
		mutex.release();
	}

	public void unlock(Semaphore semaphore){
		mutex.acquireUninterruptibly();
		--counter;
		if(counter == 0) semaphore.release();
		mutex.release();
	}
}
