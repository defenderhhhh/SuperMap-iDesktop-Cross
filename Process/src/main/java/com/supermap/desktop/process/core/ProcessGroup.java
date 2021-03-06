package com.supermap.desktop.process.core;

import com.supermap.desktop.process.enums.RunningStatus;
import com.supermap.desktop.process.events.RunningListener;
import com.supermap.desktop.process.events.StatusChangeListener;
import com.supermap.desktop.process.parameter.interfaces.IParameterPanel;
import com.supermap.desktop.process.parameter.interfaces.IParameters;
import com.supermap.desktop.process.parameter.interfaces.datas.Inputs;
import com.supermap.desktop.process.parameter.interfaces.datas.Outputs;
import com.supermap.desktop.utilities.FileUtilities;

import java.util.ArrayList;

/**
 * @author XiaJT
 */
public class ProcessGroup implements IProcessGroup, IProcess {
	// 导出时IProcess输出路径类似文件路径 ProcessGroup1/ProcessGroup2/process
	// 导入时对应路径新建ProcessGroup
	private String name;
	private ProcessGroup parent;
	private ArrayList<IProcess> processes = new ArrayList<>();
	public String path;


	public ProcessGroup(ProcessGroup parent) {
		this.parent = parent;
	}

	@Override
	public IProcessGroup getParent() {
		return parent;
	}

	@Override
	public void setParent(ProcessGroup parent) {
		this.parent = parent;
	}

	@Override
	public int getChildCount() {
		return processes.size();
	}

	@Override
	public IProcess getProcessByIndex(int index) {
		return processes.get(index);
	}


	@Override
	public int addProcess(IProcess process) {
		if (!processes.contains(process)) {
			processes.add(process);
		}
		return processes.indexOf(process);
	}

	@Override
	public boolean removeProcess(IProcess process) {
		processes.remove(process);
		return !processes.contains(process);
	}

	/**
	 * 查找第一个key相等的Process,会递归查询ProcessGroup内的Process
	 * 未提供根据key只查询第一层Process方法
	 */
	@Override
	public IProcess getProcessByKey(String key) {
		for (IProcess process : processes) {
			if (process instanceof ProcessGroup) {
				IProcess result = ((ProcessGroup) process).getProcessByKey(key);
				if (result != null) {
					return result;
				}
			} else if (process.getKey().equals(key)) {
				return process;
			}
		}
		return null;
	}

	@Override
	public void setKey(String key) {
		if (parent == null || parent.isLegitName(key, this)) {
			this.name = key;
		}
	}

	@Override
	public boolean isLegitName(String name, IProcess process) {
		if (!FileUtilities.isContainUnLegitFileNameChars(name)) {
			if (processes == null || processes.size() <= 0) {
				return true;
			}
			boolean isProcessGroup = process instanceof IProcessGroup;
			for (IProcess iProcess : processes) {
				if (iProcess != process && iProcess instanceof IProcessGroup == isProcessGroup) {
					if (iProcess.getKey().equals(name)) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void setIconPath(String path) {
		this.path = path;
	}

	@Override
	public String getTitle() {
		return name;
	}

	@Override
	public RunningStatus getStatus() {
		return null;
	}

	@Override
	public Workflow getWorkflow() {
		return null;
	}

	@Override
	public void setWorkflow(Workflow workflow) {

	}

	//region 无用方法
	@Override
	public String getKey() {
		return "ProcessGroup";
	}

	@Override
	public int getSerialID() {
		return 0;
	}

	@Override
	public void setSerialID(int serialID) {

	}

	@Override
	public Inputs getInputs() {
		return null;
	}

	@Override
	public Outputs getOutputs() {
		return null;
	}

	@Override
	public IParameters getParameters() {
		return null;
	}

	@Override
	public boolean run() {
		return false;
	}

	@Override
	public void cancel() {

	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public void reset() {

	}

	@Override
	public void addRunningListener(RunningListener listener) {

	}

	@Override
	public void removeRunningListener(RunningListener listener) {

	}

	@Override
	public void addStatusChangeListener(StatusChangeListener listener) {

	}

	@Override
	public void removeStatusChangeListener(StatusChangeListener listener) {

	}

	@Override
	public IParameterPanel getComponent() {
		return null;
	}

	@Override
	public String toXml() {
		return null;
	}

	@Override
	public void fromXml(String xml) {

	}


	//endregion
}
