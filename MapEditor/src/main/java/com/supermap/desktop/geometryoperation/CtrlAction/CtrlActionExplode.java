package com.supermap.desktop.geometryoperation.CtrlAction;

import com.supermap.desktop.Interface.IBaseItem;
import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.geometryoperation.editor.ExplodeEditor;
import com.supermap.desktop.geometryoperation.editor.IEditor;

public class CtrlActionExplode extends CtrlActionEditorBase {

	private ExplodeEditor editor = new ExplodeEditor();

	public CtrlActionExplode(IBaseItem caller, IForm formClass) {
		super(caller, formClass);
		// TODO Auto-generated constructor stub
	}

	@Override
	public IEditor getEditor() {
		return this.editor;
	}
}
