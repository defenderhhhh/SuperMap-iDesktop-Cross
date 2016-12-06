package com.supermap.desktop.geometryoperation.editor;

/**
 * @author lixiaoyao
 */

import com.supermap.data.CursorType;
import com.supermap.data.DatasetVector;
import com.supermap.data.EditType;
import com.supermap.data.GeoLine;
import com.supermap.data.GeoRegion;
import com.supermap.data.Geometry;
import com.supermap.data.GeometryType;
import com.supermap.data.Point2D;
import com.supermap.data.Point2Ds;
import com.supermap.data.QueryParameter;
import com.supermap.data.Recordset;
import com.supermap.data.SpatialQueryMode;
import com.supermap.desktop.Application;
import com.supermap.desktop.geometryoperation.EditControllerAdapter;
import com.supermap.desktop.geometryoperation.EditEnvironment;
import com.supermap.desktop.geometryoperation.IEditController;
import com.supermap.desktop.geometryoperation.IEditModel;
import com.supermap.desktop.geometryoperation.NullEditController;
import com.supermap.desktop.geometryoperation.control.MapControlTip;
import com.supermap.desktop.mapeditor.MapEditorProperties;
import com.supermap.desktop.utilities.ListUtilities;
import com.supermap.mapping.Layer;
import com.supermap.ui.Action;
import com.supermap.ui.ActionChangedEvent;
import com.supermap.ui.GeometryEvent;
import com.supermap.ui.GeometrySelectedEvent;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConcertEditor extends AbstractEditor {

	private IEditController concertEditController = new EditControllerAdapter() {
		@Override
		public void actionChanged(EditEnvironment environment, ActionChangedEvent e) {
			if (e.getOldAction() == Action.VERTEXEDIT) {
				// @formatter:off
				// 组件在很多情况下会自动结束编辑状态，比如右键，比如框选一堆对象，
				// 比如当前操作对象所在图层变为不可编辑状态，这时候桌面自定义的 Editor 还没有结束编辑，处理一下
				// @formatter:on
				environment.stopEditor();
			}
		}

		@Override
		public void geometrySelected(EditEnvironment environment, GeometrySelectedEvent arg0) {
			if (!(environment.getEditModel() instanceof ConcertEditModel)) {
				return;
			}
			ConcertEditModel editModel = (ConcertEditModel) environment.getEditModel();
			editModel.isCanEdit = isCanConcertEdit(environment);
		}

		@Override
		public void geometryModified(EditEnvironment environment, GeometryEvent event) {
			if (!(environment.getEditModel() instanceof ConcertEditModel)) {
				return;
			}
			ConcertEditModel editModel = (ConcertEditModel) environment.getEditModel();
			if (editModel.isCanEdit) {
				runConcertEdit(environment);
			}
		}
	};


	@Override
	public void activate(EditEnvironment environment) {
		ConcertEditModel editModel;
		if (environment.getEditModel() instanceof ConcertEditModel) {
			editModel = (ConcertEditModel) environment.getEditModel();
		} else {
			editModel = new ConcertEditModel();
			environment.setEditModel(editModel);
		}
		editModel.isCanEdit = isCanConcertEdit(environment);
		editModel.oldMapControlAction = environment.getMapControl().getAction();
		environment.getMapControl().setAction(Action.VERTEXEDIT);

		if (environment.getMapControl().getAction() != Action.VERTEXEDIT) {
			// 因为这个功能是组件控制的，有一些导致 Action 设置失败的原因我们的封装无法知道，因此在这里处理一下漏网之鱼
			environment.stopEditor();
		} else {
			environment.setEditController(this.concertEditController);
			environment.getMap().refresh();
			editModel.tip.bind(environment.getMapControl());
		}
	}

	@Override
	public void deactivate(EditEnvironment environment) {
		if (environment.getEditModel() instanceof ConcertEditModel) {
			ConcertEditModel editModel = (ConcertEditModel) environment.getEditModel();
			try {
				environment.getMapControl().setAction(editModel.oldMapControlAction);
				clear(environment);
			} finally {
				editModel.tip.unbind();
				environment.setEditController(NullEditController.instance());
				environment.setEditModel(null);
			}
		}
	}

	@Override
	public boolean enble(EditEnvironment environment) {
		return environment.getEditProperties().getSelectedGeometryCount() == 1
				&& environment.getEditProperties().getEditableSelectedGeometryCount() == 1
				&& ListUtilities.isListOnlyContain(environment.getEditProperties().getSelectedGeometryTypes(),
				GeometryType.GEOLINE, GeometryType.GEOREGION);
	}

	@Override
	public boolean check(EditEnvironment environment) {
		return environment.getMapControl().getAction() == Action.VERTEXEDIT
				&& environment.getEditor() instanceof ConcertEditor;
	}

	/**
	 * 判断当前选中的对象有没有共同节点的对象，如果有就返回true，当作协调编辑处理
	 * 否则，当作节点编辑处理
	 */
	private boolean isCanConcertEdit(EditEnvironment environment) {
		ConcertEditModel editModel = (ConcertEditModel) environment.getEditModel();
		List<Layer> layers = environment.getEditProperties().getSelectedLayers();
		Recordset selectRecordset = null;
		boolean result = false;

		if (layers.size() == 0) {
			return false;
		}

		for (Layer layer : layers) {
			selectRecordset = layer.getSelection().toRecordset();

			if (!editModel.hasCommonNodeGeometryIDs.containsKey(layer)) {
				editModel.hasCommonNodeGeometryIDs.put(layer, new ArrayList<Integer>());
			}

			if (selectRecordset.getRecordCount() == 1) {
				editModel.oldGeometry = selectRecordset.getGeometry();
			}
			Recordset resultRecordset = queryGeometryTouchSelectedGeometry(selectRecordset, selectRecordset.getDataset());
			if (resultRecordset.getRecordCount() >= 1) {
				result = true;
				resultRecordset.moveFirst();
				for (int i = 0; i < resultRecordset.getRecordCount(); ++i) {
					editModel.hasCommonNodeGeometryIDs.get(layer).add(resultRecordset.getID());
					resultRecordset.moveNext();
				}
			}
		}
		return result;
	}
	/**
	 * 定义空间查询
	 */
	private Recordset queryGeometryTouchSelectedGeometry(Recordset selectedRecordset, DatasetVector nowDatasetVector) {
		Recordset resultRecordset = null;

		QueryParameter parameter = new QueryParameter();
		parameter.setCursorType(CursorType.DYNAMIC);
		parameter.setSpatialQueryMode(SpatialQueryMode.TOUCH);
		parameter.setHasGeometry(true);
		parameter.setSpatialQueryObject(selectedRecordset);

		resultRecordset = nowDatasetVector.query(parameter);
		return resultRecordset;
	}

	/**
	 * 进行协调编辑
	 */
	private void runConcertEdit(EditEnvironment environment) {
		ConcertEditModel editModel = (ConcertEditModel) environment.getEditModel();
		environment.getMapControl().getEditHistory().batchBegin();
		Recordset recordset = null;
		Recordset sourceRecordset = null;
		try {
			for (Layer layer : editModel.hasCommonNodeGeometryIDs.keySet()) {
				recordset = layer.getSelection().toRecordset();
				Geometry selectedGeometry = recordset.getGeometry();
				if (selectedGeometry == null) {// 不是很好的处理，
					continue;
				}

				boolean moveOrDelete = isMoveOrDelete(selectedGeometry, environment);
				sourceRecordset = ((DatasetVector) layer.getDataset()).getRecordset(false, CursorType.DYNAMIC);

				for (int i = 0; i < editModel.hasCommonNodeGeometryIDs.get(layer).size(); ++i) {
					Integer id = editModel.hasCommonNodeGeometryIDs.get(layer).get(i);
					sourceRecordset.seekID(id);
					Geometry tempGeometry = sourceRecordset.getGeometry();
					if (tempGeometry==null){
						continue;
					}
					environment.getMapControl().getEditHistory().add(EditType.MODIFY, sourceRecordset, true);
					Geometry newGeometry = nodeEdit(tempGeometry,moveOrDelete,environment);
					newGeometry.setStyle(tempGeometry.getStyle());
					sourceRecordset.edit();
					sourceRecordset.setGeometry(newGeometry);
					sourceRecordset.update();
					newGeometry.dispose();
					tempGeometry.dispose();
				}
				selectedGeometry.dispose();
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex.toString());
		} finally {
			if (sourceRecordset != null) {
				sourceRecordset.close();
				sourceRecordset.dispose();
			}
			if (recordset != null) {
				recordset.close();
				recordset.dispose();
			}
			environment.getMapControl().getEditHistory().batchEnd();
			environment.getMap().refresh();
			editModel.isCanEdit = isCanConcertEdit(environment);
		}
	}

	/**
	 * 获取当前移动的节点是那个节点并保留没移动前的节点和移动后新的节点
	 */
	private Point2D moveNode(Point2Ds oldPoints, Point2Ds selectPoints, EditEnvironment environment) {
		ConcertEditModel editModel = (ConcertEditModel) environment.getEditModel();
		Point2D movePoint2D = new Point2D();
		try {
			for (int i = 0; i < oldPoints.getCount(); ++i) {
				Point2D oldPoint = oldPoints.getItem(i);
				Point2D selectPoint = selectPoints.getItem(i);
				if (!oldPoint.equals(selectPoint)) {
					movePoint2D = oldPoint;
					editModel.moveAfterPoint2D = selectPoint;
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex.toString());
		}
		return movePoint2D;
	}

	/**
	 * 获取当前删除的节点是那个节点并保留删除前那个节点的信息
	 */
	private Point2D deleteNode(Point2Ds oldPoints, Point2Ds selectPoints) {
		Point2D deletePoint2D = new Point2D();
		try {
			for (int i = 0; i < oldPoints.getCount(); ++i) {
				Point2D oldPoint = oldPoints.getItem(i);
				for (int j = 0; j < selectPoints.getCount(); ++j) {
					Point2D selectPoint = selectPoints.getItem(j);
					if (oldPoint.equals(selectPoint)) {
						break;
					} else if (!oldPoint.equals(selectPoint) && j == (selectPoints.getCount() - 1)) {
						deletePoint2D = oldPoint;
					}
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex.toString());
		}
		return deletePoint2D;
	}

	private Point2Ds geometryToPoint2Ds(Geometry geometry) {
		Point2Ds resultPoint2Ds = new Point2Ds();
		try {
			if (geometry.getType() == GeometryType.GEOLINE) {
				for (int i = 0; i < ((GeoLine)geometry).getPartCount(); ++i) {
					Point2Ds tempPoint2Ds = ((GeoLine)geometry).getPart(i);
					for (int j = 0; j < tempPoint2Ds.getCount(); ++j) {
						resultPoint2Ds.add(tempPoint2Ds.getItem(j));
					}
				}
			} else if (geometry.getType() == GeometryType.GEOREGION) {
				for (int i = 0; i < ((GeoRegion)geometry).getPartCount(); ++i) {
					Point2Ds tempPoint2Ds = ((GeoRegion)geometry).getPart(i);
					for (int j = 0; j < tempPoint2Ds.getCount(); ++j) {
						resultPoint2Ds.add(tempPoint2Ds.getItem(j));
					}
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex.toString());
		}
		return resultPoint2Ds;
	}

	/**
	 * 判断当前进行的是移动节点还是删除节点操作，
	 */
	private boolean isMoveOrDelete(Geometry nowSelectGeometry, EditEnvironment environment) {
		ConcertEditModel editModel = (ConcertEditModel) environment.getEditModel();
		boolean result = false;
		Point2Ds selectGeoPoints = new Point2Ds();
		Point2Ds oldGeoPoints = new Point2Ds();

		selectGeoPoints = geometryToPoint2Ds(nowSelectGeometry);
		oldGeoPoints = geometryToPoint2Ds(editModel.oldGeometry);

		if (selectGeoPoints.getCount() == oldGeoPoints.getCount()) {
			editModel.changePoint2D = moveNode(oldGeoPoints, selectGeoPoints, environment);
		} else {
			editModel.changePoint2D = deleteNode(oldGeoPoints, selectGeoPoints);
			result = true;
		}
		return result;
	}

	private Geometry nodeEdit(Geometry inputGeometry, boolean moveOrDelete, EditEnvironment environment) {
		ConcertEditModel editModel = (ConcertEditModel) environment.getEditModel();
		Point2Ds inputGeoPoint2Ds = geometryToPoint2Ds(inputGeometry);
		try {
			int deleteID = -1;
			for (int i = 0; i < inputGeoPoint2Ds.getCount(); ++i) {
				Point2D tempPoint2D = inputGeoPoint2Ds.getItem(i);
				if (tempPoint2D.equals(editModel.changePoint2D)) {
					if (moveOrDelete) {
						deleteID = i;
						break;
					} else {
						inputGeoPoint2Ds.setItem(i, editModel.moveAfterPoint2D);
						break;
					}
				}
			}
			if (deleteID != -1) {
				inputGeoPoint2Ds.remove(deleteID);
			}
			if (inputGeometry.getType()==GeometryType.GEOREGION) {
				((GeoRegion) inputGeometry).setPart(0, inputGeoPoint2Ds);
			}else if (inputGeometry.getType()==GeometryType.GEOLINE ){
				if (inputGeoPoint2Ds.getCount() >= 2) {//  当线对象删除节点后，如果只剩一个点会抛异常，进行控制
					((GeoLine) inputGeometry).setPart(0, inputGeoPoint2Ds);
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex.toString());
		}
		return inputGeometry;
	}

	private void clear(EditEnvironment environment) {
		if (!(environment.getEditModel() instanceof ConcertEditModel)) {
			return;
		}
		ConcertEditModel editModel = (ConcertEditModel) environment.getEditModel();
		environment.getMapControl().setAction(Action.SELECT2);
		editModel.clear();
	}

	private class ConcertEditModel implements IEditModel {
		public Action oldMapControlAction = Action.SELECT2;
		public MapControlTip tip = new MapControlTip();
		private JLabel tipLabel = new JLabel(MapEditorProperties.getString("String_GeometryOperation_ConcertEdit"));
		public boolean isCanEdit = false;//true表示当前选中的对象可以进行协调编辑，false则相反
		public Geometry oldGeometry = null;//选中的对象协调编辑前的状态
		public Point2D changePoint2D = new Point2D();//协调编辑改变前的点
		public Point2D moveAfterPoint2D = new Point2D();//协调编辑改变后的点
		public Map<Layer, List<Integer>> hasCommonNodeGeometryIDs = new HashMap<>();

		public ConcertEditModel() {
			this.tip.addLabel(this.tipLabel);
		}

		public void setTipMessage(String tipMessage) {
			this.tipLabel.setText(tipMessage);
			this.tipLabel.repaint();
		}

		public void clear() {
			this.oldMapControlAction = Action.SELECT2;
			this.isCanEdit = false;
			this.oldGeometry = null;
			this.hasCommonNodeGeometryIDs.clear();
			this.tipLabel.setText(MapEditorProperties.getString("String_GeometryOperation_ConcertEdit"));
		}
	}
}