package com.supermap.desktop.process.events;

import java.util.EventListener;

/**
 * Created by highsad on 2017/6/21.
 */
public interface MatrixNodeAddedListener<T extends Object> extends EventListener {
	void matrixNodeAdded(MatrixNodeAddedEvent<T> e);
}
