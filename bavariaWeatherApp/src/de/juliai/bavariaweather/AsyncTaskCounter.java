package de.juliai.bavariaweather;

/**
 * 
 * @author JuliaI
 * 
 */
public class AsyncTaskCounter {

	private int counter;

	private AsyncTaskCounterCallback callback;

	/**
	 * @param goal
	 * @param callback
	 */
	public AsyncTaskCounter(final int goal,
			final AsyncTaskCounterCallback callback) {
		this.counter = goal;
		this.callback = callback;
	}

	/**
	 * called by each async-task
	 */
	public void finished() {
		this.counter = this.counter - 1;

		if (this.counter == 0) {
			callback.onFinishedAllTasks();
		}
	}

	/**
	 * 
	 * @author JuliaI
	 * 
	 */
	public interface AsyncTaskCounterCallback {
		void onFinishedAllTasks();
	}
}
