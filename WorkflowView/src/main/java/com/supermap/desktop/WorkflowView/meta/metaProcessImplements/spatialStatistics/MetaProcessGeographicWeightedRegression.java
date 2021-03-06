package com.supermap.desktop.WorkflowView.meta.metaProcessImplements.spatialStatistics;

import com.supermap.analyst.spatialstatistics.*;
import com.supermap.data.DatasetType;
import com.supermap.data.DatasetVector;
import com.supermap.data.FieldInfo;
import com.supermap.data.FieldType;
import com.supermap.desktop.Application;
import com.supermap.desktop.WorkflowView.meta.MetaKeys;
import com.supermap.desktop.WorkflowView.meta.MetaProcess;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.constraint.ipls.EqualDatasetConstraint;
import com.supermap.desktop.process.constraint.ipls.EqualDatasourceConstraint;
import com.supermap.desktop.process.parameter.ParameterDataNode;
import com.supermap.desktop.process.parameter.interfaces.IParameter;
import com.supermap.desktop.process.parameter.interfaces.datas.types.DatasetTypes;
import com.supermap.desktop.process.parameter.ipls.*;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.utilities.DatasetUtilities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author XiaJT
 */
public class MetaProcessGeographicWeightedRegression extends MetaProcess {
	private static final String INPUT_SOURCE_DATASET = CommonProperties.getString("String_GroupBox_SourceData");
	private static final String OUTPUT_DATASET = "GeographicWeightedRegression";

	private ParameterDatasourceConstrained datasourceConstraint = new ParameterDatasourceConstrained();
	private ParameterSingleDataset parameterSingleDataset = new ParameterSingleDataset(DatasetType.POINT, DatasetType.LINE, DatasetType.REGION);

	private ParameterComboBox parameterBandWidthType = new ParameterComboBox(ProcessProperties.getString("String_BandWidthType"));
	private ParameterNumber parameterDistanceTolerance = new ParameterNumber(ProcessProperties.getString("String_BandWidthDistanceTolerance"));
	private ParameterFieldGroup parameterExplanatory = new ParameterFieldGroup(ProcessProperties.getString("String_ExplanatoryFields"));
	private ParameterComboBox parameterKernelFunction = new ParameterComboBox(ProcessProperties.getString("String_KernelFunction"));
	private ParameterComboBox parameterKernelType = new ParameterComboBox(ProcessProperties.getString("String_KernelType"));
	private ParameterFieldComboBox parameterModelField = new ParameterFieldComboBox(ProcessProperties.getString("String_ModelField"));
	private ParameterNumber parameterNeighbors = new ParameterNumber(ProcessProperties.getString("String_Neighbors"));

	private ParameterSaveDataset parameterSaveDataset = new ParameterSaveDataset();

	public MetaProcessGeographicWeightedRegression() {
		initParameter();
		initParameterState();
		initConstraints();
	}

	private void initParameter() {
		FieldType[] fieldType = {FieldType.INT16, FieldType.INT32, FieldType.INT64, FieldType.SINGLE, FieldType.DOUBLE};
		parameterExplanatory.setFieldType(fieldType);
		parameterModelField.setFieldType(fieldType);
		parameterBandWidthType.setItems(new ParameterDataNode(ProcessProperties.getString("String_BindWidthType_AICC"), BandWidthType.AICC),
				new ParameterDataNode(ProcessProperties.getString("String_BindWidthType_BANDWIDTH"), BandWidthType.BANDWIDTH),
				new ParameterDataNode(ProcessProperties.getString("String_BindWidthType_CV"), BandWidthType.CV));
		parameterKernelFunction.setItems(new ParameterDataNode(ProcessProperties.getString("String_KernelFunction_BISQUARE"), KernelFunction.BISQUARE),
				new ParameterDataNode(ProcessProperties.getString("String_KernelFunction_BOXCAR"), KernelFunction.BOXCAR),
				new ParameterDataNode(ProcessProperties.getString("String_KernelFunction_GAUSSIAN"), KernelFunction.GAUSSIAN),
				new ParameterDataNode(ProcessProperties.getString("String_KernelFunction_TRICUBE"), KernelFunction.TRICUBE));
		parameterKernelType.setItems(new ParameterDataNode(ProcessProperties.getString("String_KernelType_ADAPTIVE"), KernelType.ADAPTIVE),
				new ParameterDataNode(ProcessProperties.getString("String_KernelType_FIXED"), KernelType.FIXED));

		ParameterCombine parameterCombineSourceDataset = new ParameterCombine();
		parameterCombineSourceDataset.addParameters(datasourceConstraint);
		parameterCombineSourceDataset.addParameters(parameterSingleDataset);
		parameterCombineSourceDataset.setDescribe(CommonProperties.getString("String_ColumnHeader_SourceData"));

		ParameterCombine parameterSetting = new ParameterCombine();

		final ParameterSwitch parameterSwitch = new ParameterSwitch();
		parameterSwitch.add("0", parameterDistanceTolerance);
		parameterSwitch.add("1", parameterNeighbors);
		parameterSwitch.switchParameter("1");

		parameterDistanceTolerance.setMinValue(0.0);
		parameterDistanceTolerance.setIsIncludeMin(false);
		parameterNeighbors.setMinValue(2);
		parameterNeighbors.setMaxBit(0);

		final ParameterSwitch parameterSwitchParent = new ParameterSwitch();
		parameterSwitchParent.add("0", parameterSwitch);
		parameterBandWidthType.addPropertyListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(ParameterComboBox.comboBoxValue)) {
					if (parameterBandWidthType.getSelectedData() == BandWidthType.AICC || parameterBandWidthType.getSelectedData() == BandWidthType.CV) {
						parameterSwitchParent.switchParameter((IParameter) null);
					} else {
						parameterSwitchParent.switchParameter(parameterSwitch);
					}
				}
			}
		});
		parameterKernelType.addPropertyListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(ParameterComboBox.comboBoxValue)) {
					if (parameterKernelType.getSelectedData() == KernelType.ADAPTIVE) {
						parameterSwitch.switchParameter("1");
					} else {
						parameterSwitch.switchParameter("0");
					}
				}
			}
		});


		parameterSetting.addParameters(parameterExplanatory, parameterKernelFunction, parameterModelField,
				parameterBandWidthType, parameterKernelType, parameterSwitchParent);
		parameterSetting.setDescribe(CommonProperties.getString("String_GroupBox_ParamSetting"));

		ParameterCombine parameterResultSet = new ParameterCombine();
		parameterResultSet.setDescribe(CommonProperties.getString("String_ResultSet"));
		parameterResultSet.addParameters(parameterSaveDataset);

		parameters.setParameters(parameterCombineSourceDataset, parameterSetting, parameterResultSet);
		parameters.addInputParameters(INPUT_SOURCE_DATASET, DatasetTypes.VECTOR, parameterCombineSourceDataset);
		parameters.addOutputParameters(OUTPUT_DATASET, DatasetTypes.VECTOR, parameterSaveDataset);
	}

	private void initConstraints() {
		EqualDatasourceConstraint equalDatasourceConstraint = new EqualDatasourceConstraint();
		equalDatasourceConstraint.constrained(datasourceConstraint, ParameterDatasourceConstrained.DATASOURCE_FIELD_NAME);
		equalDatasourceConstraint.constrained(parameterSingleDataset, ParameterSingleDataset.DATASOURCE_FIELD_NAME);

		EqualDatasetConstraint equalDatasetConstraint = new EqualDatasetConstraint();
		equalDatasetConstraint.constrained(parameterSingleDataset, ParameterSingleDataset.DATASET_FIELD_NAME);
		equalDatasetConstraint.constrained(parameterExplanatory, ParameterFieldGroup.FIELD_DATASET);
		equalDatasetConstraint.constrained(parameterModelField, ParameterFieldComboBox.DATASET_FIELD_NAME);
	}

	private void initParameterState() {
		DatasetVector defaultDatasetVector = DatasetUtilities.getDefaultDatasetVector();
		if (defaultDatasetVector != null) {
			datasourceConstraint.setSelectedItem(defaultDatasetVector.getDatasource());
			parameterSingleDataset.setSelectedItem(defaultDatasetVector);
			parameterExplanatory.setDataset(defaultDatasetVector);
			parameterModelField.setDataset(defaultDatasetVector);
			parameterModelField.setFieldName(defaultDatasetVector);
		}
		parameterDistanceTolerance.setSelectedItem("");
		parameterNeighbors.setSelectedItem("2");
		parameterSaveDataset.setDatasetName("result_geoWeightedRegression");
		parameterBandWidthType.setSelectedItem(parameterBandWidthType.getItemAt(1));
	}

	@Override
	public String getTitle() {
		return ProcessProperties.getString("String_geographicWeightedRegression");
	}


	@Override
	public boolean execute() {
		boolean isSuccessful = false;

		DatasetVector datasetVector;
		Object value = parameters.getInputs().getData(INPUT_SOURCE_DATASET).getValue();
		if (value != null && value instanceof DatasetVector) {
			datasetVector = (DatasetVector) value;
		} else {
			datasetVector = (DatasetVector) parameterSingleDataset.getSelectedItem();
		}
		GWRParameter gwrParameter = new GWRParameter();
		BandWidthType bandWidthType = (BandWidthType) parameterBandWidthType.getSelectedData();
		gwrParameter.setBandWidthType(bandWidthType);

		FieldInfo[] selectedFields = parameterExplanatory.getSelectedFields();
		if (selectedFields != null) {
			String[] explanatoryFields = new String[selectedFields.length];
			for (int i = 0; i < selectedFields.length; i++) {
				explanatoryFields[i] = selectedFields[i].getName();
			}
			gwrParameter.setExplanatoryFeilds(explanatoryFields);
		}

		gwrParameter.setKernelFunction((KernelFunction) parameterKernelFunction.getSelectedData());
		gwrParameter.setModelFeild(parameterModelField.getFieldName());
		KernelType kernelType = (KernelType) parameterKernelType.getSelectedData();
		gwrParameter.setKernelType(kernelType);
		if (bandWidthType == BandWidthType.BANDWIDTH) {
			if (kernelType == KernelType.ADAPTIVE) {
				gwrParameter.setNeighbors(Integer.valueOf((String) parameterNeighbors.getSelectedItem()));
			} else {
				try {
					gwrParameter.setDistanceTolerance(Double.valueOf((String) parameterDistanceTolerance.getSelectedItem()));
				} catch (NumberFormatException e) {
					Application.getActiveApplication().getOutput().output(ProcessProperties.getString("String_DistanceToleranceMustOverZero"));
				}
			}
		}
		try {
			SpatialRelModeling.addSteppedListener(steppedListener);
			GWRAnalystResult gwrAnalystResult = SpatialRelModeling.geographicWeightedRegression(datasetVector, parameterSaveDataset.getResultDatasource(),
					parameterSaveDataset.getResultDatasource().getDatasets().getAvailableDatasetName(parameterSaveDataset.getDatasetName()), gwrParameter);
			isSuccessful = gwrAnalystResult != null;
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e.getMessage());
		} finally {
			SpatialRelModeling.removeSteppedListener(steppedListener);
		}
		return isSuccessful;
	}

	@Override
	public String getKey() {
		return MetaKeys.GEOGRAPHIC_WEIGHTED_REGRESSION;
	}
}
