package com.supermap.desktop.WorkflowView.meta.metaProcessImplements;

import com.supermap.data.Dataset;
import com.supermap.data.Datasource;
import com.supermap.desktop.Application;
import com.supermap.desktop.WorkflowView.meta.MetaKeys;
import com.supermap.desktop.WorkflowView.meta.MetaProcess;
import com.supermap.desktop.lbs.Interface.IServerService;
import com.supermap.desktop.lbs.params.CommonSettingCombine;
import com.supermap.desktop.lbs.params.JobResultResponse;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.events.RunningEvent;
import com.supermap.desktop.process.messageBus.NewMessageBus;
import com.supermap.desktop.process.parameter.ParameterDataNode;
import com.supermap.desktop.process.parameter.interfaces.IParameterPanel;
import com.supermap.desktop.process.parameter.interfaces.datas.types.Type;
import com.supermap.desktop.process.parameter.ipls.*;
import com.supermap.desktop.process.parameters.ParameterPanels.DefaultOpenServerMap;
import com.supermap.desktop.progress.Interface.IUpdateProgress;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.utilities.CursorUtilities;

import java.util.concurrent.CancellationException;

/**
 * Created by caolp on 2017-05-26.
 */
public class MetaProcessSimpleDensity extends MetaProcess {
	private ParameterIServerLogin parameterIServerLogin = new ParameterIServerLogin();
	ParameterInputDataType parameterInputDataType = new ParameterInputDataType();
	private ParameterComboBox parameterComboBoxAnalyseType = new ParameterComboBox(ProcessProperties.getString("String_AnalyseType"));
	private ParameterComboBox parameterComboBoxMeshType = new ParameterComboBox(ProcessProperties.getString("String_MeshType"));
	private ParameterTextField parameterIndex = new ParameterTextField(ProcessProperties.getString("String_Index"));
	private ParameterTextField parameterBounds= new ParameterTextField(ProcessProperties.getString("String_AnalystBounds"));
	private ParameterTextField parameterMeshSize = new ParameterTextField(ProcessProperties.getString("String_MeshSize"));
	private ParameterComboBox parameterMeshSizeUnit = new ParameterComboBox(ProcessProperties.getString("String_MeshSizeUnit"));
	private ParameterTextField parameterRadius= new ParameterTextField(ProcessProperties.getString("String_Radius"));
	private ParameterComboBox parameterRadiusUnit= new ParameterComboBox(ProcessProperties.getString("String_RadiusUnit"));
	private ParameterComboBox parameterAreaUnit= new ParameterComboBox(ProcessProperties.getString("String_AreaUnit"));


	public MetaProcessSimpleDensity() {
		initComponents();
	}

	private void initComponents() {
		ParameterDataNode parameterDataNode = new ParameterDataNode(ProcessProperties.getString("String_SimplePointDensity"), "0");
		parameterComboBoxAnalyseType.setItems(parameterDataNode);
		parameterComboBoxAnalyseType.setSelectedItem(parameterDataNode);
		parameterComboBoxMeshType.setItems(new ParameterDataNode(ProcessProperties.getString("String_QuadrilateralMesh"), "0"),
				new ParameterDataNode(ProcessProperties.getString("String_HexagonalMesh"), "1"));

		//流程图中不支持在地图中绘制范围，范围表示与iServer的表示相同
		parameterBounds.setSelectedItem("-74.050,40.650,-73.850,40.850");
		parameterIndex.setSelectedItem("col7,col8");
		parameterMeshSize.setSelectedItem("10");
		parameterMeshSizeUnit.setItems(new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Meter"), "Meter"),
				new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Kilometer"), "Kilometer"),
				new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Yard"), "Yard"),
				new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Foot"), "Foot"),
				new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Mile"), "Mile")
		);
		parameterRadius.setSelectedItem("100");
		parameterRadiusUnit.setItems(new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Meter"), "Meter"),
				new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Kilometer"), "Kilometer"),
				new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Yard"), "Yard"),
				new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Foot"), "Foot"),
				new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Mile"), "Mile")
		);
		parameterAreaUnit.setItems(new ParameterDataNode(CommonProperties.getString("String_AreaUnit_Meter"), "SquareMeter"),
				new ParameterDataNode(CommonProperties.getString("String_AreaUnit_Kilometer"), "SquareKiloMeter"),
				new ParameterDataNode(CommonProperties.getString("String_AreaUnit_Hectare"), "Hectare"),
				new ParameterDataNode(CommonProperties.getString("String_AreaUnit_Are"), "Are"),
				new ParameterDataNode(CommonProperties.getString("String_AreaUnit_Acre"), "Acre"),
				new ParameterDataNode(CommonProperties.getString("String_AreaUnit_Foot"), "SquareFoot"),
				new ParameterDataNode(CommonProperties.getString("String_AreaUnit_Yard"), "SquareYard"),
				new ParameterDataNode(CommonProperties.getString("String_AreaUnit_Mile"), "SquareMile")
		);
		ParameterCombine parameterCombineSetting = new ParameterCombine();
		parameterCombineSetting.setDescribe(ProcessProperties.getString("String_setParameter"));
		ParameterCombine parameterCombineAlaysis = new ParameterCombine();
		parameterCombineAlaysis.setDescribe(ProcessProperties.getString("String_AnalystSet"));
		parameterCombineAlaysis.addParameters(parameterComboBoxAnalyseType,
				parameterComboBoxMeshType,
				parameterIndex,
				parameterBounds,
				parameterMeshSize,
				parameterMeshSizeUnit,
				parameterRadius,
				parameterRadiusUnit,
				parameterAreaUnit
		);
		parameterCombineSetting.addParameters(
				parameterInputDataType,
				parameterCombineAlaysis
		);
		parameters.setParameters(
				parameterIServerLogin,
				parameterCombineSetting
		);
		parameters.getOutputs().addData("SimpleDensityResult", Type.UNKOWN);
	}

	@Override
	public String getTitle() {
		return ProcessProperties.getString("String_SimpleDensityAnalyst");
	}

	@Override
	public IParameterPanel getComponent() {
		return this.parameters.getPanel();
	}

	@Override
	public boolean execute() {
		try {
			fireRunning(new RunningEvent(this, 0, "start"));
			IServerService service = parameterIServerLogin.login();
			CommonSettingCombine input = new CommonSettingCombine("input", "");
			CommonSettingCombine datasetInfo = new CommonSettingCombine("datasetInfo", "");
			if(parameterInputDataType.parameterDataInputWay.getSelectedData().toString().equals("0")){
				CommonSettingCombine filePath = new CommonSettingCombine("filePath",parameterInputDataType.parameterHDFSPath.getSelectedItem().toString());
				CommonSettingCombine xIndex = new CommonSettingCombine("xIndex",parameterInputDataType.parameterTextFieldXIndex.getSelectedItem().toString());
				CommonSettingCombine yIndex = new CommonSettingCombine("yIndex",parameterInputDataType.parameterTextFieldYIndex.getSelectedItem().toString());
				CommonSettingCombine separator = new CommonSettingCombine("separator",parameterInputDataType.parameterTextFieldSeparator.getSelectedItem().toString());
				input.add(filePath,xIndex,yIndex,separator);
			}else if(parameterInputDataType.parameterDataInputWay.getSelectedData().toString().equals("1")){
				CommonSettingCombine type = new CommonSettingCombine("type",parameterInputDataType.parameterDataSouceType.getSelectedItem().toString());
				CommonSettingCombine url = new CommonSettingCombine("url",parameterInputDataType.parameterDataSoucePath.getSelectedItem().toString());
				CommonSettingCombine datasetName = new CommonSettingCombine("datasetName",parameterInputDataType.parameterDatasetName.getSelectedItem().toString());
				CommonSettingCombine datasetType = new CommonSettingCombine("datasetType",(String) parameterInputDataType.parameterDatasetType.getSelectedData());
				CommonSettingCombine numSlices = new CommonSettingCombine("numSlices",parameterInputDataType.parameterSpark.getSelectedItem().toString());
				datasetInfo.add(type,url,datasetName,datasetType);
				input.add(datasetInfo,numSlices);
			}else{
				Dataset sourceDataset = parameterInputDataType.parameterSourceDataset.getSelectedDataset();
				CommonSettingCombine dataSourceName = new CommonSettingCombine("dataSourceName",((Datasource)parameterInputDataType.parameterSourceDatasource.getSelectedItem()).getAlias());
				CommonSettingCombine name = new CommonSettingCombine("name",sourceDataset.getName());
				CommonSettingCombine type = new CommonSettingCombine("type",(String) parameterInputDataType.parameterDatasetType1.getSelectedData());
				CommonSettingCombine engineType = new CommonSettingCombine("engineType",parameterInputDataType.parameterEngineType.getSelectedItem().toString());
				CommonSettingCombine server = new CommonSettingCombine("server",parameterInputDataType.parameterTextFieldAddress.getSelectedItem().toString());
				CommonSettingCombine dataBase = new CommonSettingCombine("dataBase",parameterInputDataType.parameterDataBaseName.getSelectedItem().toString());
				CommonSettingCombine user = new CommonSettingCombine("user",parameterInputDataType.parameterTextFieldUserName.getSelectedItem().toString());
				CommonSettingCombine password = new CommonSettingCombine("password",parameterInputDataType.parameterTextFieldPassword.getSelectedItem().toString());
				CommonSettingCombine datasourceConnectionInfo = new CommonSettingCombine("datasourceConnectionInfo", "");
				datasourceConnectionInfo.add(engineType,server,dataBase,user,password);
				datasetInfo.add(type,name,dataSourceName,datasourceConnectionInfo);
				input.add(datasetInfo);
			}
			CommonSettingCombine method = new CommonSettingCombine("method",(String) parameterComboBoxAnalyseType.getSelectedData());
			CommonSettingCombine meshType = new CommonSettingCombine("meshType",(String) parameterComboBoxMeshType.getSelectedData());
			CommonSettingCombine fields = new CommonSettingCombine("fields",(String) parameterIndex.getSelectedItem());
			CommonSettingCombine bounds = new CommonSettingCombine("bounds",parameterBounds.getSelectedItem().toString());
			CommonSettingCombine meshSize = new CommonSettingCombine("meshSize",parameterMeshSize.getSelectedItem().toString());
			CommonSettingCombine meshSizeUnit = new CommonSettingCombine("meshSizeUnit",(String)parameterMeshSizeUnit.getSelectedData());
			CommonSettingCombine radius = new CommonSettingCombine("radius",parameterRadius.getSelectedItem().toString());
			CommonSettingCombine radiusUnit = new CommonSettingCombine("radiusUnit",(String)parameterRadiusUnit.getSelectedData());
			CommonSettingCombine areaUnit = new CommonSettingCombine("areaUnit",(String)parameterAreaUnit.getSelectedData());
			CommonSettingCombine analyst = new CommonSettingCombine("analyst", "");
			analyst.add(method,meshType,fields,bounds,meshSize,meshSizeUnit,radius,radiusUnit,areaUnit);

			CommonSettingCombine commonSettingCombine = new CommonSettingCombine("", "");
			commonSettingCombine.add(input, analyst);
			JobResultResponse response = service.queryResult(MetaKeys.SIMPLE_DENSITY, commonSettingCombine.getFinalJSon());
			CursorUtilities.setWaitCursor();
			if (null != response) {
				CursorUtilities.setDefaultCursor();
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
						fireRunning(new RunningEvent(MetaProcessSimpleDensity.this, percent, message, -1));
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
			parameters.getOutputs().getData("SimpleDensityResult").setValue("");// // TODO: 2017/5/26
			CursorUtilities.setDefaultCursor();
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
			return false;
		}
		return true;
	}

	@Override
	public String getKey() {
		return MetaKeys.SIMPLE_DENSITY;
	}
}

