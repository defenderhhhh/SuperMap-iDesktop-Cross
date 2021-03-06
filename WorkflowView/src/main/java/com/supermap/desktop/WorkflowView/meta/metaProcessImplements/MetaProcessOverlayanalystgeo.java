package com.supermap.desktop.WorkflowView.meta.metaProcessImplements;

import com.supermap.data.Dataset;
import com.supermap.desktop.Application;
import com.supermap.desktop.WorkflowView.meta.MetaKeys;
import com.supermap.desktop.WorkflowView.meta.MetaProcess;
import com.supermap.desktop.controls.ControlsProperties;
import com.supermap.desktop.lbs.Interface.IServerService;
import com.supermap.desktop.lbs.params.CommonSettingCombine;
import com.supermap.desktop.lbs.params.JobResultResponse;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.constraint.ipls.EqualDatasourceConstraint;
import com.supermap.desktop.process.events.RunningEvent;
import com.supermap.desktop.process.messageBus.NewMessageBus;
import com.supermap.desktop.process.parameter.ParameterDataNode;
import com.supermap.desktop.process.parameter.interfaces.datas.types.BasicTypes;
import com.supermap.desktop.process.parameter.interfaces.datas.types.Type;
import com.supermap.desktop.process.parameter.ipls.*;
import com.supermap.desktop.process.parameters.ParameterPanels.DefaultOpenServerMap;
import com.supermap.desktop.progress.Interface.IUpdateProgress;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.properties.CoreProperties;
import com.supermap.desktop.utilities.CursorUtilities;
import com.supermap.desktop.utilities.DatasetUtilities;

import java.util.concurrent.CancellationException;

/**
 * 单对象叠加
 *
 * @author XiaJT
 */
public class MetaProcessOverlayanalystgeo extends MetaProcess {

	private ParameterIServerLogin parameterIServerLogin = new ParameterIServerLogin();
	private ParameterBigDatasourceDatasource parameterSourceDatasource;
	private ParameterSingleDataset parameterSourceDataset;

	private ParameterBigDatasourceDatasource parameterOverlayDatasource;
	private ParameterSingleDataset parameterOverlayDataset;

	private ParameterComboBox parameterOverlayTypeComboBox;


	public MetaProcessOverlayanalystgeo() {
		initComponents();
		initComponentState();
		initConstraint();
		initListener();
	}

	private void initComponents() {
		parameterSourceDatasource = new ParameterBigDatasourceDatasource();
		parameterSourceDatasource.setDescribe(CommonProperties.getString("String_SourceDatasource"));
		parameterSourceDataset = new ParameterSingleDataset();
		parameterSourceDataset.setDescribe(CommonProperties.getString("String_Label_SourceDataset"));
		parameterOverlayDatasource = new ParameterBigDatasourceDatasource();
		parameterOverlayDatasource.setDescribe(ProcessProperties.getString("String_overlayDatasource"));
		parameterOverlayDataset = new ParameterSingleDataset();
		parameterOverlayDataset.setDescribe(ProcessProperties.getString("String_overlayDataset"));

		parameterOverlayTypeComboBox = new ParameterComboBox(CoreProperties.getString("String_OverlayAnalystType"));
		parameterOverlayTypeComboBox.setItems(
				new ParameterDataNode(CoreProperties.getString("String_OverlayAnalystMethod_Clip"), "clip"),
				new ParameterDataNode(CoreProperties.getString("String_OverlayAnalystMethod_Iserver_Intersect"), "interset"),
				new ParameterDataNode(CoreProperties.getString("String_OverlayAnalystMethod_Erase"), "erase"),
				new ParameterDataNode(CoreProperties.getString("String_OverlayAnalystMethod_Union"), "union"),
				new ParameterDataNode(CoreProperties.getString("String_OverlayAnalystMethod_XOR"), "xor"),
				new ParameterDataNode(CoreProperties.getString("String_OverlayAnalystMethod_Update"), "update"),
				new ParameterDataNode(CoreProperties.getString("String_OverlayAnalystMethod_Identity"), "identity")
		);

		ParameterCombine parameterCombineSource = new ParameterCombine();
		parameterCombineSource.setDescribe(ControlsProperties.getString("String_GroupBox_SourceDataset"));
		parameterCombineSource.addParameters(parameterSourceDatasource, parameterSourceDataset);
		ParameterCombine parameterCombineOverlay = new ParameterCombine();
		parameterCombineOverlay.setDescribe(CommonProperties.getString("String_GroupBox_OverlayDataset"));
		parameterCombineOverlay.addParameters(parameterOverlayDatasource, parameterOverlayDataset);
		ParameterCombine parameterCombineSetting = new ParameterCombine();
		parameterCombineSetting.setDescribe(ProcessProperties.getString("String_setParameter"));
		parameterCombineSetting.addParameters(parameterOverlayTypeComboBox);

		parameters.addParameters(parameterIServerLogin, parameterCombineSource, parameterCombineOverlay, parameterCombineSetting);
		parameters.addInputParameters("source", Type.UNKOWN, parameterCombineSource);// 缺少对应的类型
		parameters.addInputParameters("overlay", Type.UNKOWN, parameterCombineOverlay);// 缺少对应的类型
		parameters.addOutputParameters("OverlayResult", BasicTypes.STRING, null);
	}

	private void initComponentState() {
		Dataset defaultBigDataStoreDataset = DatasetUtilities.getDefaultBigDataStoreDataset();
		if (defaultBigDataStoreDataset != null) {
			parameterSourceDatasource.setSelectedItem(defaultBigDataStoreDataset.getDatasource());
			parameterSourceDataset.setSelectedItem(defaultBigDataStoreDataset);

			parameterOverlayDatasource.setSelectedItem(defaultBigDataStoreDataset.getDatasource());
			parameterOverlayDataset.setSelectedItem(defaultBigDataStoreDataset);

		}
	}

	private void initConstraint() {
		EqualDatasourceConstraint equalSourceDatasource = new EqualDatasourceConstraint();
		equalSourceDatasource.constrained(parameterSourceDatasource, ParameterDatasource.DATASOURCE_FIELD_NAME);
		equalSourceDatasource.constrained(parameterSourceDataset, ParameterSingleDataset.DATASOURCE_FIELD_NAME);

		EqualDatasourceConstraint equalOverlayDatasource = new EqualDatasourceConstraint();
		equalOverlayDatasource.constrained(parameterOverlayDatasource, ParameterDatasource.DATASOURCE_FIELD_NAME);
		equalOverlayDatasource.constrained(parameterSourceDataset, ParameterSingleDataset.DATASOURCE_FIELD_NAME);
	}

	private void initListener() {

	}

	@Override
	public String getTitle() {
		return ProcessProperties.getString("String_overlayanaly");
	}

	@Override
	public boolean execute() {
		try {
			fireRunning(new RunningEvent(this, 0, "start"));
			IServerService service = parameterIServerLogin.login();
			Dataset sourceDataset = parameterSourceDataset.getSelectedDataset();
			Dataset overlayDataset = parameterOverlayDataset.getSelectedDataset();
			String overlayType = (String) parameterOverlayTypeComboBox.getSelectedData();
			CommonSettingCombine datasetSource = new CommonSettingCombine("datasetSource", sourceDataset.getName());
			CommonSettingCombine input = new CommonSettingCombine("input", "");
			input.add(datasetSource);

			CommonSettingCombine datasetOverlay = new CommonSettingCombine("datasetOverlay", overlayDataset.getName());
			CommonSettingCombine mode = new CommonSettingCombine("mode", overlayType);
			CommonSettingCombine analyst = new CommonSettingCombine("analyst", "");
			analyst.add(datasetOverlay, mode);

			CommonSettingCombine commonSettingCombine = new CommonSettingCombine("", "");
			commonSettingCombine.add(input, analyst);
			JobResultResponse response = service.queryResult(MetaKeys.OVERLAYANALYSTGEO, commonSettingCombine.getFinalJSon());
			CursorUtilities.setWaitCursor();
			if (null != response) {
				NewMessageBus messageBus = new NewMessageBus(response, new IUpdateProgress() {
					@Override
					public boolean isCancel() {
						return false;
					}

					@Override
					public void setCancel(boolean isCancel) {

					}

					@Override
					public void updateProgress(int percent, String remainTime, String message) throws CancellationException {
						fireRunning(new RunningEvent(MetaProcessOverlayanalystgeo.this, percent, message, -1));
					}

					@Override
					public void updateProgress(String message, int percent, String currentMessage) throws CancellationException {

					}

					@Override
					public void updateProgress(int percent, int totalPercent, String remainTime, String message) throws CancellationException {

					}

					@Override
					public void updateProgress(int percent, String recentTask, int totalPercent, String message) throws CancellationException {

					}
				}, DefaultOpenServerMap.INSTANCE);
				messageBus.run();
			}
			fireRunning(new RunningEvent(this, 100, "finished"));
			parameters.getOutputs().getData("OverlayResult").setValue("");// TODO: 2017/6/26 也许没结果,but
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
			return false;
		} finally {
			CursorUtilities.setDefaultCursor();
		}
		return true;
	}

	@Override
	public String getKey() {
		return MetaKeys.OVERLAYANALYSTGEO;
	}
}
