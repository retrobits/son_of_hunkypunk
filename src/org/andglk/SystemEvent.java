package org.andglk;

public class SystemEvent extends Event implements Runnable {
	private final Runnable mRunnable;

	public SystemEvent(Runnable r) {
		super(null);
		mRunnable = r;
	}
	
	@Override
	public void run() {
		mRunnable.run();
	}
}
