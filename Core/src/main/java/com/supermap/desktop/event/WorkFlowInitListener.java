package com.supermap.desktop.event;

import com.supermap.desktop.Interface.IWorkflow;
import org.w3c.dom.Element;

/**
 * @author XiaJT
 */
public abstract class WorkFlowInitListener {
	public abstract IWorkflow init(Element element);
}
