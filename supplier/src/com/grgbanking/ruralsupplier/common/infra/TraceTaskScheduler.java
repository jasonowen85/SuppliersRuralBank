package com.grgbanking.ruralsupplier.common.infra;

public class TraceTaskScheduler extends WrapTaskScheduler {
	public TraceTaskScheduler(com.grgbanking.ruralsupplier.common.infra.TaskScheduler wrap) {
		super(wrap);
	}

	@Override
	public void reschedule(com.grgbanking.ruralsupplier.common.infra.Task task) {
		trace("reschedule " + task.dump(true));
		
		super.reschedule(task);
	}
	
	private final void trace(String msg) {

	}
}
