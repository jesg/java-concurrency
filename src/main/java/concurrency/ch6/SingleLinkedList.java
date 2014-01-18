package concurrency.ch6;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import concurrency.ch4.LightSwitch;


public class SingleLinkedList<T> {
	private final Semaphore insertMutex = new Semaphore(1);
	private final Semaphore noSearcher = new Semaphore(1);
	private final Semaphore noInsert = new Semaphore(1);
	private final LightSwitch searchSwitch = new LightSwitch();
	private final LightSwitch insertSwitch = new LightSwitch();
	
	private Node<T> head;
	private final AtomicInteger size = new AtomicInteger(0);
	
	public T get(int i){ 
		searchSwitch.lock(noSearcher);
		T result = getNode(i).head(); 
		searchSwitch.unlock(noSearcher);
		return result;
	}
	public int size(){ return size.get(); }
	
	private Node<T> getNode(int i) throws IndexOutOfBoundsException {
		if( i < 0 ) throw new IndexOutOfBoundsException("Negative index");
		
		Node<T> localHead = head;
//		iterate from the end of the list
		for(int index = size.get() - 1;;index--){
			if( i == index )
				return localHead;
			else if( localHead == null )
				throw new IndexOutOfBoundsException("Current index: " + index);
			else
				localHead = localHead.tail();
		}
	}
	
	public void add(T data){
		insertSwitch.lock(noInsert);
		insertMutex.acquireUninterruptibly();
		
		_add(data);
		
		insertMutex.release();
		insertSwitch.unlock(noInsert);
	}
	
	/*
	 * add to the end of the list
	 */
	private void _add(T data){
		head = new Node<T>(data, head);
		size.getAndIncrement();
	}
	
	public void remove(int i){
		noSearcher.acquireUninterruptibly();
		noInsert.acquireUninterruptibly();
		
		_remove(i);
		
		noInsert.release();
		noSearcher.release();
	}
	
	// single threaded remove
	private void _remove(int i){
		if( i == (size.get() - 1) && size.get() == 1){
			head = null;
		}else if( i == (size.get() - 1) ){
			head = head.tail();
		}else {
			Node<T> previous = getNode(i - 1);
			previous.setTail(previous.tail().tail());
			previous.tail().setTail(null); // release the reference
		}
		size.getAndDecrement();
	}
	
	private static class Node<T> {
		private final T head;
		private Node<T> tail;
		
		Node(T head, Node<T> tail) {
			this.head = head;
			this.tail = tail;
		}
		
		private T head(){ return head; }
		private Node<T> tail(){return tail; }
		private void setTail(Node<T> node){ this.tail = node;}
	}
}
