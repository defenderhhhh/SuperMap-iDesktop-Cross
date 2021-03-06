package com.supermap.desktop.process.tasks;

import com.supermap.desktop.Application;
import com.supermap.desktop.process.core.IProcess;
import com.supermap.desktop.process.events.RunningEvent;
import com.supermap.desktop.process.events.RunningListener;
import com.supermap.desktop.properties.CoreProperties;

import java.text.MessageFormat;

/**
 * Created by highsad on 2017/6/22.
 */
public class ProcessWorker extends Worker<SingleProgress> {
	private IProcess process;
	private RunningHandler runningHandler = new RunningHandler();

	public ProcessWorker(IProcess process) {
		if (process == null) {
			throw new NullPointerException();
		}
		this.process = process;
		this.process.addRunningListener(this.runningHandler);
	}

	public IProcess getProcess() {
		return this.process;
	}

	@Override
	public String getTitle() {
		return this.process == null ? "null" : this.process.getTitle();
	}

	@Override
	protected boolean doWork() {
		return process.run();
	}

	private class RunningHandler implements RunningListener {
		@Override
		public void running(RunningEvent e) {
			try {
				if (isCancelled) {
					e.setCancel(true);
				} else {
					update(new SingleProgress(e.getProgress(), e.getMessage(), MessageFormat.format(CoreProperties.getString("String_RemainTime"), e.getRemainTime())));
				}
			} catch (Exception e1) {
				e.setCancel(true);
				Application.getActiveApplication().getOutput().output(e1);
			}
		}
	}
}
