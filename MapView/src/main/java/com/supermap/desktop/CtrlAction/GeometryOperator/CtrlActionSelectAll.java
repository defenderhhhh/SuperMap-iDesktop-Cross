package com.supermap.desktop.CtrlAction.GeometryOperator;

import com.supermap.desktop.Application;
import com.supermap.desktop.FormMap;
import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.Interface.IFormMap;
import com.supermap.desktop.implement.CtrlAction;
import com.supermap.desktop.utilities.MapUtilities;
import com.supermap.mapping.Layer;

import java.util.ArrayList;

public class CtrlActionSelectAll extends CtrlAction {
	public CtrlActionSelectAll(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
	}

	@Override
	public void run() {
		try {
			FormMap formMap = (FormMap) Application.getActiveApplication().getActiveForm();
			formMap.selectAll();
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		}
	}

	@Override
	public boolean enable() {
		boolean result = false;
		if (Application.getActiveApplication().getActiveForm() instanceof IFormMap) {
			IFormMap formMap = (IFormMap) Application.getActiveApplication().getActiveForm();
			ArrayList<Layer> layers = MapUtilities.getLayers(formMap.getMapControl().getMap());
			for (Layer layer : layers) {
				if (layer.isVisible() && layer.isSelectable()) {
					result = true;
				}
			}
		}
		return result;
	}
}
