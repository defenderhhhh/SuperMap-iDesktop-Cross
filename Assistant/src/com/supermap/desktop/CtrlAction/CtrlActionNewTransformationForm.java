package com.supermap.desktop.CtrlAction;

import com.supermap.data.Datasource;
import com.supermap.data.Datasources;
import com.supermap.desktop.Application;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.ui.controls.DialogResult;

/**
 * @author XiaJT
 */
public class CtrlActionNewTransformationForm extends CtrlAction {
	public CtrlActionNewTransformationForm(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run() {
		JDialogNewTransformationForm jDialogNewTransformationForm = new JDialogNewTransformationForm();
		if (jDialogNewTransformationForm.showDialog() == DialogResult.OK) {
// TODO: 2016/7/7  
		}
	}

	@Override
	public boolean enable() {
		Datasources datasources = Application.getActiveApplication().getWorkspace().getDatasources();
		for (int i = 0; i < datasources.getCount(); i++) {
			Datasource datasource = datasources.get(i);
			if (datasource.getDatasets().getCount() > 0 && !datasource.isReadOnly()) {
				return true;
			}
		}
		return false;
	}
}