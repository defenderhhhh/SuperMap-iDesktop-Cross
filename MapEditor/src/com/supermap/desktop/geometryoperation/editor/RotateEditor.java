package com.supermap.desktop.geometryoperation.editor;

import java.util.ArrayList;

import com.supermap.data.EditType;
import com.supermap.data.Geometry;
import com.supermap.data.Point2D;
import com.supermap.data.Recordset;
import com.supermap.data.Rectangle2D;
import com.supermap.desktop.Application;
import com.supermap.desktop.geometry.Abstract.ICompoundFeature;
import com.supermap.desktop.geometry.Abstract.ILineFeature;
import com.supermap.desktop.geometry.Abstract.IPointFeature;
import com.supermap.desktop.geometry.Abstract.IRegionFeature;
import com.supermap.desktop.geometry.Abstract.ITextFeature;
import com.supermap.desktop.geometryoperation.EditEnvironment;
import com.supermap.desktop.geometryoperation.control.JDialogRotateParams;
import com.supermap.desktop.ui.controls.DialogResult;
import com.supermap.desktop.utilties.DoubleUtilties;
import com.supermap.desktop.utilties.ListUtilties;
import com.supermap.desktop.utilties.MapUtilties;
import com.supermap.mapping.Layer;

public class RotateEditor extends AbstractEditor {

	@Override
	public void activate(EditEnvironment environment) {
		try {
			Point2D center = getCenter(environment);
			JDialogRotateParams dialog = new JDialogRotateParams(center);
			if (dialog.showDialog() == DialogResult.OK) {
				rotate(environment, new Point2D(dialog.getCenterX(), dialog.getCenterY()), dialog.getAngle());
			}
		} catch (Exception e) {
			Application.getActiveApplication().getOutput().output(e);
		} finally {
			environment.activateEditor(NullEditor.INSTANCE);
		}
	}

	@Override
	public boolean enble(EditEnvironment environment) {
		return environment.getEditProperties().getEditableSelectedGeometryCount() > 0
				&& ListUtilties.isListOnlyContain(environment.getEditProperties().getEditableSelectedGeometryTypeFeatures(), ICompoundFeature.class,
						ITextFeature.class, IPointFeature.class, IRegionFeature.class, ILineFeature.class);
	}

	private Point2D getCenter(EditEnvironment environment) {
		Point2D center = Point2D.getEMPTY();

		if (environment.getEditProperties().getEditableSelectedGeometryCount() == 1) {

			// 只有一个对象，直接取对象的内点，作为旋转基点
			Recordset recordset = environment.getActiveEditableLayer().getSelection().toRecordset();
			recordset.moveFirst();
			Geometry selectedGeometry = recordset.getGeometry();
			center = selectedGeometry.getInnerPoint();
			recordset.close();
			recordset.dispose();
			selectedGeometry.dispose();
		} else {

			// 多个对象，取所有对象外接矩形的中心点，作为旋转基点
			Rectangle2D bounds = new Rectangle2D(0, 0, 0, 0);
			ArrayList<Layer> layers = MapUtilties.getLayers(environment.getMap());

			for (Layer layer : layers) {
				if (layer.isEditable() && layer.getSelection().getCount() > 0) {
					Recordset recordset = layer.getSelection().toRecordset();

					try {
						if (DoubleUtilties.equals(bounds.getWidth(), 0d, 8)) {
							bounds = recordset.getBounds();
						} else {
							bounds.union(recordset.getBounds());
						}
					} finally {
						recordset.close();
						recordset.dispose();
					}
				}
			}
		}
		return center;
	}

	private void rotate(EditEnvironment environment, Point2D basePoint, double angle) {
		environment.getMapControl().getEditHistory().batchBegin();

		try {
			ArrayList<Layer> layers = MapUtilties.getLayers(environment.getMap());

			for (Layer layer : layers) {
				if (layer.isEditable() && layer.getSelection().getCount() > 0) {
					Recordset recordset = layer.getSelection().toRecordset();

					try {
						// 这句话会导致 recordset 的指针到最后
						environment.getMapControl().getEditHistory().add(EditType.MODIFY, recordset, false);
						recordset.moveFirst();

						while (!recordset.isEOF()) {
							Geometry geo = recordset.getGeometry();

							try {
								geo.rotate(basePoint, angle);
								recordset.edit();
								recordset.setGeometry(geo);
								recordset.update();
							} finally {
								if (geo != null) {
									geo.dispose();
								}
								recordset.moveNext();
							}
						}
					} finally {
						if (recordset != null) {
							recordset.close();
							recordset.dispose();
						}
					}
				}
			}
		} catch (Exception ex) {
			Application.getActiveApplication().getOutput().output(ex);
		} finally {
			environment.getMapControl().getEditHistory().batchEnd();
			environment.getMapControl().getMap().refresh();
		}
	}
}