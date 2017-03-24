package com.supermap.desktop.process.parameter.ParameterPanels;

import com.supermap.desktop.process.enums.ParameterType;
import com.supermap.desktop.process.parameter.events.ParameterCombineBuildPanelListener;
import com.supermap.desktop.process.parameter.implement.ParameterCombine;
import com.supermap.desktop.process.parameter.interfaces.IParameter;
import com.supermap.desktop.process.parameter.interfaces.ParameterPanelDescribe;
import com.supermap.desktop.ui.controls.GridBagConstraintsHelper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author XiaJT
 */
@ParameterPanelDescribe(parameterPanelType = ParameterType.COMBINE)
public class ParameterCombinePanel extends SwingPanel implements ParameterCombineBuildPanelListener {

	private ParameterCombine parameterCombine;

	public ParameterCombinePanel(IParameter parameterCombine) {
		super(parameterCombine);
		this.parameterCombine = ((ParameterCombine) parameterCombine);
		((ParameterCombine) parameterCombine).addParameterCombineBuildPanelListeners(this);
	}

	private void buildPanel() {
		int x = 0, y = 0;
		if (panel != null) {
			panel.removeAll();
		} else {
			panel = new JPanel();
			panel.setLayout(new GridBagLayout());
		}
		ArrayList<IParameter> parameterList = parameterCombine.getParameterList();
		String combineType = parameterCombine.getCombineType();
		int weightIndex = parameterCombine.getWeightIndex();
		for (IParameter parameter : parameterList) {
			int weightX = combineType.equals(ParameterCombine.VERTICAL) ? 1 : (weightIndex == -1 || weightIndex == x ? 1 : 0);
			int weightY = combineType.equals(ParameterCombine.HORIZONTAL) ? 1 : (weightIndex == -1 || weightIndex == y ? 1 : 0);
			panel.add(((JPanel) parameter.getParameterPanel().getPanel()), new GridBagConstraintsHelper(x, y, 1, 1).setWeight(weightX, weightY).setAnchor(GridBagConstraints.CENTER).setFill(GridBagConstraints.HORIZONTAL).setAnchor(GridBagConstraints.NORTH).setFill(GridBagConstraints.BOTH).setInsets(y > 0 ? 5 : 0, x > 0 ? 5 : 0, 0, 0));
			if (combineType.equals(ParameterCombine.VERTICAL)) {
				y++;
			} else {
				x++;
			}
		}
	}

	@Override
	public void rebuild() {
		buildPanel();
	}
}
