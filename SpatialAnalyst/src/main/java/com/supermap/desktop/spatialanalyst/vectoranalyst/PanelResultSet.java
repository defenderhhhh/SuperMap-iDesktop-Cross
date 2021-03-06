package com.supermap.desktop.spatialanalyst.vectoranalyst;

import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.spatialanalyst.SpatialAnalystProperties;
import com.supermap.desktop.ui.SMFormattedTextField;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.text.NumberFormatter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;

public class PanelResultSet extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private JCheckBox checkBoxUnionBuffer;
	private JCheckBox checkBoxRemainAttributes;
	private JCheckBox checkBoxDisplayInMap;
	private JCheckBox checkBoxDisplayInScene;
	private JLabel labelSemicircleLineSegment;
	private SMFormattedTextField textFieldSemicircleLineSegment;
	private final static String TEXT_VALUE = "100";

	public JCheckBox getCheckBoxUnionBuffer() {
		return checkBoxUnionBuffer;
	}

	public void setCheckBoxUnionBuffer(JCheckBox checkBoxUnionBuffer) {
		this.checkBoxUnionBuffer = checkBoxUnionBuffer;
	}

	public JCheckBox getCheckBoxRemainAttributes() {
		return checkBoxRemainAttributes;
	}

	public void setCheckBoxRemainAttributes(JCheckBox checkBoxRemainAttributes) {
		this.checkBoxRemainAttributes = checkBoxRemainAttributes;
	}

	public JCheckBox getCheckBoxDisplayInMap() {
		return checkBoxDisplayInMap;
	}

	public void setCheckBoxDisplayInMap(JCheckBox checkBoxDisplayInMap) {
		this.checkBoxDisplayInMap = checkBoxDisplayInMap;
	}

	public JCheckBox getCheckBoxDisplayInScene() {
		return checkBoxDisplayInScene;
	}

	public void setCheckBoxDisplayInScene(JCheckBox checkBoxDisplayInScene) {
		this.checkBoxDisplayInScene = checkBoxDisplayInScene;
	}

	public JLabel getLabelSemicircleLineSegment() {
		return labelSemicircleLineSegment;
	}

	public void setLabelSemicircleLineSegment(JLabel labelSemicircleLineSegment) {
		this.labelSemicircleLineSegment = labelSemicircleLineSegment;
	}

	public SMFormattedTextField getTextFieldSemicircleLineSegment() {
		return textFieldSemicircleLineSegment;
	}

	public void setTextFieldSemicircleLineSegment(SMFormattedTextField textFieldSemicircleLineSegment) {
		this.textFieldSemicircleLineSegment = textFieldSemicircleLineSegment;
	}

	public PanelResultSet() {
		initComponent();
		initResources();
		setOtherPanelResultSetLayout();
//		setOtherPanelResultSetLayout();
	}

	private void initComponent() {
		this.checkBoxUnionBuffer = new JCheckBox("UnionBuffer");
		this.checkBoxRemainAttributes = new JCheckBox("RemainInAttributes");
		this.checkBoxRemainAttributes.setSelected(true);
		this.checkBoxDisplayInMap = new JCheckBox("DisplayInMap");
		this.checkBoxDisplayInMap.setSelected(true);
		this.checkBoxDisplayInScene = new JCheckBox("DisPlayInScene");
		this.checkBoxDisplayInScene.setVisible(false);
		this.labelSemicircleLineSegment = new JLabel("SemicircleLineSegment");

		NumberFormatter numberFormatter = new NumberFormatter(NumberFormat.getInstance());
		numberFormatter.setValueClass(Long.class);
		this.textFieldSemicircleLineSegment = new SMFormattedTextField(numberFormatter);
		this.textFieldSemicircleLineSegment.setText(TEXT_VALUE);
		this.textFieldSemicircleLineSegment.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				char keyChar = e.getKeyChar();
				if (keyChar > '9' || keyChar < '0') {
					e.consume();
				}
			}
		});
	}

	private void initResources() {
		this.checkBoxUnionBuffer.setText(SpatialAnalystProperties.getString("String_UnionBufferItem"));
		this.checkBoxRemainAttributes.setText(SpatialAnalystProperties.getString("String_RetainAttribute"));
		this.checkBoxDisplayInMap.setText(SpatialAnalystProperties.getString("String_DisplayInMap"));
		this.checkBoxDisplayInScene.setText(SpatialAnalystProperties.getString("String_DisplayInScene"));
		this.labelSemicircleLineSegment.setText(SpatialAnalystProperties.getString("String_Label_SemicircleLineSegment"));
	}

//	private void setPanelResultSetLayout() {
//		this.setBorder(BorderFactory.createTitledBorder(CommonProperties.getString("String_GroupBox_ParamSetting")));
//		GroupLayout panelResultSetLayout = new GroupLayout(this);
//		panelResultSetLayout.setAutoCreateContainerGaps(true);
//		panelResultSetLayout.setAutoCreateGaps(true);
//		this.setLayout(panelResultSetLayout);
//
//		//@formatter:off
//		panelResultSetLayout.setHorizontalGroup(panelResultSetLayout.createSequentialGroup()
//				.addGroup(panelResultSetLayout.createParallelGroup(Alignment.LEADING)
//						.addComponent(this.checkBoxUnionBuffer)
//						.addComponent(this.checkBoxDisplayInMap)
//						.addComponent(this.labelSemicircleLineSegment))
//				.addGroup(panelResultSetLayout.createParallelGroup(Alignment.LEADING)
//						.addComponent(this.checkBoxRemainAttributes)
//						.addComponent(this.checkBoxDisplayInScene)
//						.addComponent(this.textFieldSemicircleLineSegment,5,5,Short.MAX_VALUE)));
//
//		panelResultSetLayout.setVerticalGroup(panelResultSetLayout.createSequentialGroup()
//				.addGroup(panelResultSetLayout.createParallelGroup(Alignment.LEADING)
//						.addComponent(this.checkBoxUnionBuffer)
//						.addComponent(this.checkBoxRemainAttributes))
//				.addGroup(panelResultSetLayout.createParallelGroup(Alignment.LEADING)
//						.addComponent(this.checkBoxDisplayInMap)
//						.addComponent(this.checkBoxDisplayInScene))
//				.addGroup(panelResultSetLayout.createParallelGroup(Alignment.BASELINE)
//						.addComponent(this.labelSemicircleLineSegment)
//						.addComponent(this.textFieldSemicircleLineSegment)));
//
//		//@formatter:on
//	}

	public void setOtherPanelResultSetLayout() {
		this.setBorder(BorderFactory.createTitledBorder(CommonProperties.getString("String_GroupBox_ParamSetting")));
		GroupLayout panelResultSetLayout = new GroupLayout(this);
		panelResultSetLayout.setAutoCreateContainerGaps(true);
		panelResultSetLayout.setAutoCreateGaps(true);
		this.setLayout(panelResultSetLayout);

		//@formatter:off
		panelResultSetLayout.setHorizontalGroup(panelResultSetLayout.createSequentialGroup()
				.addGroup(panelResultSetLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(this.checkBoxUnionBuffer)
						.addComponent(this.checkBoxDisplayInMap)
						.addComponent(this.checkBoxRemainAttributes)
						.addComponent(this.labelSemicircleLineSegment))
				.addGroup(panelResultSetLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(this.textFieldSemicircleLineSegment)));

		panelResultSetLayout.setVerticalGroup(panelResultSetLayout.createSequentialGroup()
				.addGroup(panelResultSetLayout.createSequentialGroup()
						.addComponent(this.checkBoxUnionBuffer)
						.addComponent(this.checkBoxDisplayInMap)
						.addComponent(this.checkBoxRemainAttributes))
				.addGroup(panelResultSetLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(this.labelSemicircleLineSegment)
						.addComponent(this.textFieldSemicircleLineSegment)));
		//@formatter:on
	}

	/**
	 * 创建面板是否可用方法
	 * 2017.3.2 yuanR
	 *
	 * @param isEnable
	 */
	public void setPanelEnable(boolean isEnable) {
		this.checkBoxUnionBuffer.setEnabled(isEnable);
		this.checkBoxRemainAttributes.setEnabled(isEnable);
		this.checkBoxDisplayInMap.setEnabled(isEnable);
		this.checkBoxDisplayInScene.setEnabled(isEnable);
		this.textFieldSemicircleLineSegment.setEnabled(isEnable);
	}
}