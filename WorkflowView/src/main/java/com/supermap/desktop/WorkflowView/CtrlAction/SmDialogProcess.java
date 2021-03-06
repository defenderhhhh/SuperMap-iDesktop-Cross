package com.supermap.desktop.WorkflowView.CtrlAction;

import com.supermap.desktop.WorkflowView.meta.MetaProcess;
import com.supermap.desktop.WorkflowView.tasks.SingleProgressPanel;
import com.supermap.desktop.process.tasks.ProcessWorker;
import com.supermap.desktop.ui.controls.GridBagConstraintsHelper;
import com.supermap.desktop.ui.controls.SmDialog;

import javax.swing.*;
import java.awt.*;

/**
 * @author XiaJT
 */
public class SmDialogProcess extends SmDialog {
	private MetaProcess metaProcess;

	public SmDialogProcess(MetaProcess metaProcess) {
		this.metaProcess = metaProcess;
		this.setTitle(metaProcess.getTitle());
		JPanel panel = (JPanel) metaProcess.getComponent().getPanel();
//		ProcessTask task = TaskUtil.getTask(metaProcess);
		SingleProgressPanel singleProgressPanel = new SingleProgressPanel(metaProcess.getTitle());
		singleProgressPanel.setTitleVisible(false);
		ProcessWorker worker = new ProcessWorker(metaProcess);
		singleProgressPanel.setWorker(worker);
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new GridBagLayout());
		this.add(scrollPane, new GridBagConstraintsHelper(0, 0, 1, 1).setWeight(1, 1).setFill(GridBagConstraints.BOTH));
		this.add(singleProgressPanel, new GridBagConstraintsHelper(0, 2, 1, 1).setWeight(1, 0).setFill(GridBagConstraintsHelper.HORIZONTAL));
		this.setMinimumSize(new Dimension(400, 600));
		this.setPreferredSize(new Dimension(400, 600));
		this.setLocationRelativeTo(null);
	}
}
