package com.supermap.desktop.utilities;

import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.Interface.IFormManager;
import com.supermap.desktop.Interface.IWorkflow;
import com.supermap.desktop.enums.WindowType;
import com.supermap.desktop.properties.CoreProperties;

import javax.swing.*;
import java.util.ArrayList;

/**
 * @author XiaJT
 */
public class WorkFlowUtilities {
	public static void deleteProcess(ArrayList<IWorkflow> workflows) {
		try {
			String message = CoreProperties.getString("String_ProcessDelete_Confirm");
			if (workflows.size() == 1) {
				message = message + "\r\n" + String.format(CoreProperties.getString("String_ProcessDelete_Confirm_One"), workflows.get(0).getName());
			} else {
				message = message + "\r\n" + String.format(CoreProperties.getString("String_ProcessDelete_Confirm_Multi"), workflows.size());
			}
			if (JOptionPaneUtilities.showConfirmDialog(message) == JOptionPane.OK_OPTION) {
				for (IWorkflow workflow : workflows) {
					IFormManager formManager = Application.getActiveApplication().getMainFrame().getFormManager();
					for (int count = formManager.getCount() - 1; count >= 0; count--) {
						IForm form = formManager.get(count);
						if (form.getWindowType() == WindowType.WORKFLOW && form.getText().equals(workflow.getName())) {
							form.setNeedSave(false);
							formManager.close(form);
							break;
						}
					}
					Application.getActiveApplication().removeWorkflow(workflow);
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	public static boolean isWorkFlowNameExist(String workFlowName) {
		ArrayList<IWorkflow> workFlows = Application.getActiveApplication().getWorkflows();
		for (IWorkflow workFlow : workFlows) {
			if (workFlow.getName().equals(workFlowName)) {
				return true;
			}
		}

		IFormManager formManager = Application.getActiveApplication().getMainFrame().getFormManager();
		for (int i = 0; i < formManager.getCount(); i++) {
			if (formManager.get(i).getWindowType() == WindowType.WORKFLOW && formManager.get(i).getText().equals(workFlowName)) {
				return true;
			}
		}
		return false;
	}
}
