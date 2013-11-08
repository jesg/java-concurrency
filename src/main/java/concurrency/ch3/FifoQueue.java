package concurrency.ch3;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class FifoQueue {
	private final Semaphore mutex = new Semaphore(1);
	private final Queue<Semaphore> queue = new LinkedList<Semaphore>();

	public void await(){
		Semaphore localSemaphore = new Semaphore(0);
		
		mutex.acquireUninterruptibly();
		queue.add(localSemaphore);
		mutex.release();
		
		localSemaphore.acquireUninterruptibly();
	}
	
	public void signal(){
		Semaphore localSemaphore;
		mutex.acquireUninterruptibly();
		localSemaphore = queue.poll();
		mutex.release();
		
		localSemaphore.release();
	}
}
