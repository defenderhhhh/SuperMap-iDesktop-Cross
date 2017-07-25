package com.supermap.desktop.process.meta.metaProcessImplements.DataRun;

import com.supermap.data.*;
import com.supermap.desktop.Application;
import com.supermap.desktop.controls.ControlsProperties;
import com.supermap.desktop.process.ProcessProperties;
import com.supermap.desktop.process.constraint.implement.DatasourceConstraint;
import com.supermap.desktop.process.constraint.implement.EqualDatasourceConstraint;
import com.supermap.desktop.process.meta.MetaKeys;
import com.supermap.desktop.process.meta.MetaProcess;
import com.supermap.desktop.process.parameter.ParameterDataNode;
import com.supermap.desktop.process.parameter.implement.*;
import com.supermap.desktop.process.parameter.interfaces.IParameters;
import com.supermap.desktop.process.parameter.interfaces.datas.types.DatasetTypes;
import com.supermap.desktop.properties.CommonProperties;
import com.supermap.desktop.utilities.DatasetUtilities;

/**
 * Created by yuanR on 2017/7/24
 * 点密度聚合
 *
 */
public class MetaProcessAggregatePoints extends MetaProcess {

	private final static String INPUT_DATA = CommonProperties.getString("String_GroupBox_SourceData");
	private final static String OUTPUT_DATA = "AggregateResult";

	private ParameterDatasourceConstrained sourceDatasource;
	private ParameterSingleDataset dataset;

	// 距离
	private ParameterNumber parameterNumberDistance;
	// 阀值
	private ParameterNumber parameterNumberMinPilePointCount;
	// 单位
	private ParameterComboBox parameterComboBoxUnit;

	private ParameterSaveDataset saveDataset;

	public MetaProcessAggregatePoints() {
		initParameters();
		initParameterConstraint();
		initParametersState();

	}

	private void initParameters() {
		sourceDatasource = new ParameterDatasourceConstrained();
		dataset = new ParameterSingleDataset(DatasetType.POINT);

		parameterNumberDistance = new ParameterNumber(ProcessProperties.getString("String_AggregatePoints_Distance"));
		parameterNumberDistance.setMaxBit(22);
		parameterNumberDistance.setMinValue(0);
		parameterNumberDistance.setIsIncludeMin(false);
		parameterNumberDistance.setRequisite(true);

		parameterNumberMinPilePointCount = new ParameterNumber(ProcessProperties.getString("String_AggregatePoints_MinPilePointCount"));
		parameterNumberMinPilePointCount.setMaxBit(0);
		parameterNumberMinPilePointCount.setMinValue(2);
		parameterNumberMinPilePointCount.setIsIncludeMin(true);
		parameterNumberMinPilePointCount.setRequisite(true);

		parameterComboBoxUnit = new ParameterComboBox(null);
		parameterComboBoxUnit.addItem(new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Millimeter"), Unit.MILIMETER));
		parameterComboBoxUnit.addItem(new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Centimeter"), Unit.CENTIMETER));
		parameterComboBoxUnit.addItem(new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Decimeter"), Unit.DECIMETER));
		parameterComboBoxUnit.addItem(new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Meter"), Unit.METER));
		parameterComboBoxUnit.addItem(new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Kilometer"), Unit.KILOMETER));
		parameterComboBoxUnit.addItem(new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Mile"), Unit.MILE));
		parameterComboBoxUnit.addItem(new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Inch"), Unit.INCH));
		parameterComboBoxUnit.addItem(new ParameterDataNode(CommonProperties.getString("String_DistanceUnit_Foot"), Unit.FOOT));

		this.saveDataset = new ParameterSaveDataset();

		// 源数据
		ParameterCombine parameterCombineSourceData = new ParameterCombine();
		parameterCombineSourceData.addParameters(sourceDatasource, dataset);
		parameterCombineSourceData.setDescribe(ControlsProperties.getString("String_GroupBox_SourceDataset"));

		//参数设置
		ParameterCombine parameterCombineParent = new ParameterCombine(ParameterCombine.HORIZONTAL);
		parameterCombineParent.addParameters(parameterNumberDistance);
		parameterCombineParent.addParameters(parameterComboBoxUnit);
		parameterCombineParent.setWeightIndex(0);
		ParameterCombine parameterCombineSet = new ParameterCombine();
		parameterCombineSet.addParameters(parameterCombineParent, parameterNumberMinPilePointCount);
		parameterCombineSet.setDescribe(CommonProperties.getString("String_GroupBox_ParamSetting"));

		// 结果数据
		ParameterCombine parameterCombineResultData = new ParameterCombine();
		parameterCombineResultData.setDescribe(CommonProperties.getString("String_GroupBox_ResultData"));
		parameterCombineResultData.addParameters(saveDataset);

		parameters.setParameters(parameterCombineSourceData, parameterCombineSet, parameterCombineResultData);
		this.parameters.addInputParameters(INPUT_DATA, DatasetTypes.POINT, parameterCombineSourceData);
		this.parameters.addOutputParameters(OUTPUT_DATA, DatasetTypes.REGION, parameterCombineResultData);
	}

	private void initParameterConstraint() {
		Dataset defaultDataset = DatasetUtilities.getDefaultDataset(DatasetType.POINT);
		if (defaultDataset != null) {
			sourceDatasource.setSelectedItem(defaultDataset.getDatasource());
			dataset.setSelectedItem(defaultDataset);
			saveDataset.setResultDatasource(defaultDataset.getDatasource());
			saveDataset.setSelectedItem(OUTPUT_DATA);
		}

		parameterNumberDistance.setSelectedItem(1000);
		parameterComboBoxUnit.setSelectedItem(Unit.METER);
		parameterNumberMinPilePointCount.setSelectedItem(4);

	}

	private void initParametersState() {
		EqualDatasourceConstraint equalDatasourceConstraint = new EqualDatasourceConstraint();
		equalDatasourceConstraint.constrained(sourceDatasource, ParameterDatasource.DATASOURCE_FIELD_NAME);
		equalDatasourceConstraint.constrained(dataset, ParameterSingleDataset.DATASOURCE_FIELD_NAME);

		DatasourceConstraint.getInstance().constrained(saveDataset, ParameterSaveDataset.DATASOURCE_FIELD_NAME);
	}


	@Override
	public boolean execute() {
		boolean isSuccessful = false;
		try {
			DatasetVector sourceDataset = null;
			if (this.getParameters().getInputs().getData(INPUT_DATA) != null
					&& this.getParameters().getInputs().getData(INPUT_DATA).getValue() instanceof DatasetVector) {
				sourceDataset = (DatasetVector) this.getParameters().getInputs().getData(INPUT_DATA).getValue();
			} else {
				sourceDataset = (DatasetVector) this.dataset.getSelectedItem();
			}

			Recordset recordset = sourceDataset.getRecordset(false, CursorType.STATIC);

			Point2Ds point2Ds = new Point2Ds();
			while (!recordset.isEOF()) {
				GeoPoint geoPoint = (GeoPoint) recordset.getGeometry();
				Point2D point2D = new Point2D(geoPoint.getX(), geoPoint.getY());
				point2Ds.add(point2D);
				recordset.moveNext();
			}

			PrjCoordSys prjCoordSys = sourceDataset.getPrjCoordSys();
			Double distance = Double.valueOf(parameterNumberDistance.getSelectedItem().toString());
			Unit unit = (Unit) parameterComboBoxUnit.getSelectedData();
			int minPilePointCount = Integer.valueOf(parameterNumberMinPilePointCount.getSelectedItem().toString());

			// 组件给的接口貌似有问题-yuanR
			int[] ints = Geometrist.aggregatePoints(point2Ds, prjCoordSys, distance, unit, minPilePointCount);
			GeoRegion[] geoRegions = Geometrist.aggregatePointsToRegions(point2Ds, prjCoordSys, distance, unit, minPilePointCount);


			// 新建数据集
			if (geoRegions.length > 0) {
				DatasetVectorInfo datasetVectorInfo = new DatasetVectorInfo(saveDataset.getDatasetName(), DatasetType.REGION);
				datasetVectorInfo.setEncodeType(sourceDataset.getEncodeType());
				DatasetVector newDataset = saveDataset.getResultDatasource().getDatasets().create(datasetVectorInfo);

				Recordset recordsetResult = newDataset.getRecordset(false, CursorType.DYNAMIC);

				for (int i = 0; i < geoRegions.length; i++) {
					recordsetResult.addNew((Geometry) geoRegions[i]);
					recordsetResult.refresh();
				}
			} else {
				isSuccessful = false;
			}

		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		} finally {

		}
		return isSuccessful;
	}


	@Override
	public IParameters getParameters() {
		return parameters;
	}

	@Override
	public String getKey() {
		return MetaKeys.AggregatePoints;
	}

	@Override
	public String getTitle() {
		return ProcessProperties.getString("String_AggregatePoints");
	}


}