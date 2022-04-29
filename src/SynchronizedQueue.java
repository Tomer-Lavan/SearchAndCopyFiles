/**
 * A synchronized bounded-size queue for multithreaded producer-consumer applications.
 *
 * @param <T> Type of data items
 */
public class SynchronizedQueue<T> {

	private  T[] buffer;
	private  int size;
	private  int producers;
	private  int startOfQueue;
	private  int endOfQueue;

	// TODO: Add more private members here as necessary

	/**
	 * Constructor. Allocates a buffer (an array) with the given capacity and
	 * resets pointers and counters.
	 * @param capacity Buffer capacity
	 */
	@SuppressWarnings("unchecked")
	public SynchronizedQueue(int capacity) {
		this.buffer = (T[])(new Object[capacity]);
		this.producers = 0;
		this.startOfQueue = -1;
		this.endOfQueue = -1;
		this.size = 0;
		// TODO: Add more logic here as necessary
	}

	/**
	 * Dequeues the first item from the queue and returns it.
	 * If the queue is empty but producers are still registered to this queue,
	 * this method blocks until some item is available.
	 * If the queue is empty and no more items are planned to be added to this
	 * queue (because no producers are registered), this method returns null.
	 *
	 * @return The first item, or null if there are no more items
	 * @see #registerProducer()
	 * @see #unregisterProducer()
	 */
	public T dequeue() {
		synchronized (this) {
			while(this.size == 0 && this.producers > 0) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					System.exit(1);
				}
			}
			if (this.size == 0 && this.producers == 0) {
				return null;
			}
			T item = cyclicDequeue();
			return item;
		}
	}

	/**
	 * Enqueues an item to the end of this queue. If the queue is full, this
	 * method blocks until some space becomes available.
	 *
	 * @param item Item to enqueue
	 */
	public void enqueue(T item) {
		synchronized (this) {
			while(this.buffer.length == this.size) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					System.exit(1);
				}
			}
			cyclicEnqueue(item);
		}
	}

	/**
	 * Returns the capacity of this queue
	 * @return queue capacity
	 */
	public int getCapacity() {
		return this.buffer.length;
	}

	/**
	 * Returns the current size of the queue (number of elements in it)
	 * @return queue size
	 */
	public int getSize() {
		synchronized (this){
			return this.size;
		}
	}
	/**
	 * Registers a producer to this queue. This method actually increases the
	 * internal producers counter of this queue by 1. This counter is used to
	 * determine whether the queue is still active and to avoid blocking of
	 * consumer threads that try to dequeue elements from an empty queue, when
	 * no producer is expected to add any more items.
	 * Every producer of this queue must call this method before starting to
	 * enqueue items, and must also call <see>{@link #unregisterProducer()}</see> when
	 * finishes to enqueue all items.
	 *
	 * @see #dequeue()
	 * @see #unregisterProducer()
	 */
	public void registerProducer() {
		synchronized (this) {
			// TODO: This should be in a critical section
			this.producers++;
			this.notifyAll();
		}
	}

	/**
	 * Unregisters a producer from this queue. See <see>{@link #registerProducer()}</see>.
	 *
	 * @see #dequeue()
	 * @see #registerProducer()
	 */
	public void unregisterProducer () {
		// TODO: This should be in a critical section
		synchronized (this) {
			this.producers--;
			this.notifyAll();
		}
	}

	private T cyclicDequeue() {
		T item = this.buffer[this.startOfQueue];
		this.buffer[this.startOfQueue] = null;
		if (this.startOfQueue == this.endOfQueue) {
			startOfQueue = -1;
			endOfQueue = -1;
		}
		else {
			this.startOfQueue = (this.startOfQueue + 1) % this.buffer.length;
		}
		this.size--;
		this.notifyAll();
		return item;
	}

	private void cyclicEnqueue(T item) {
		if (this.startOfQueue == -1) {
			this.startOfQueue = 0;
		}
		endOfQueue = (endOfQueue + 1) % this.buffer.length;
		this.buffer[endOfQueue] = item;
		this.size++;
		this.notifyAll();
	}
}
