package com.supermap.desktop.controls.GeometryPropertyBindWindow;

import com.supermap.desktop.Interface.IForm;
import com.supermap.desktop.Interface.IFormTabular;
import com.supermap.desktop.ui.FormManager;
import com.supermap.desktop.ui.mdi.IMdiContainer;
import com.supermap.desktop.ui.mdi.MdiGroup;
import com.supermap.desktop.ui.mdi.MdiPage;
import com.supermap.desktop.ui.mdi.MdiPane;
import com.supermap.desktop.ui.mdi.events.*;
import com.supermap.desktop.ui.mdi.layout.ILayoutStrategy;
import com.supermap.desktop.ui.mdi.layout.SplitLayoutStrategy;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by xie on 2016/12/22.
 */
public class BindLayoutStrategy implements ILayoutStrategy {

	private FormManager container;
	private MdiGroup tabularGroup;
	private JSplitPane contentSplit;
	private ArrayList<MdiGroup> mapGroups;
	private HashMap<MdiGroup, JSplitPane> splits;

	private PageAddedHandler pageAddedHandler = new PageAddedHandler();

	public BindLayoutStrategy(FormManager container) {
		this.container = container;
		this.tabularGroup = new MdiGroup(container);
		this.mapGroups = new ArrayList<>();
		this.splits = new HashMap<>();

		this.contentSplit = new JSplitPane();
		this.contentSplit.setOrientation(JSplitPane.VERTICAL_SPLIT);
		this.contentSplit.setBorder(null);
		this.contentSplit.setOneTouchExpandable(false);
		this.contentSplit.setLeftComponent(null);
		this.contentSplit.setRightComponent(this.tabularGroup);
		this.contentSplit.setResizeWeight(0.7);
		if (this.contentSplit.getUI() instanceof BasicSplitPaneUI) {
			BasicSplitPaneDivider divider = ((BasicSplitPaneUI) this.contentSplit.getUI()).getDivider();
			divider.setBorder(null);
		}
		this.container.add(this.contentSplit, BorderLayout.CENTER);
	}

	public MdiGroup getTabularGroup() {
		return this.tabularGroup;
	}

	@Override
	public IMdiContainer getContainer() {
		return this.container;
	}

	/**
	 * 添加一个 group，只要不是 tabularGroup 就放在上半部分水平并列布局
	 *
	 * @param group
	 * @return
	 */
	@Override
	public boolean addGroup(MdiGroup group) {
		boolean result = false;
		if (!validateGroup(group) || group == this.tabularGroup) {
			return false;
		}

		if (!this.splits.containsKey(group)) {
			this.splits.put(group, createSplit(group));
		}

		this.mapGroups.add(group);
		JSplitPane split = this.splits.get(group);
		JSplitPane preSplit = findPreSplit(group);

		if (preSplit != null) {
			setNextSplit(preSplit, split);
		} else {
			((MdiPane) getContainer()).add(split, BorderLayout.CENTER);
			resetDividerSize(split);
			((MdiPane) getContainer()).revalidate();
		}
		adjustDividerProportion();
		result = true;
		return result;
	}

	@Override
	public boolean removeGroup(MdiGroup group) {
		boolean result = false;
		if (!validateGroup(group)) {
			return false;
		}

		if (group != this.tabularGroup) {

			// 将要移除的 group 不是 tabularGroup
			JSplitPane split = this.splits.get(group);
			JSplitPane preSplit = findPreSplit(group);
			JSplitPane nextSplit = findNextSplit(group);

			if (preSplit != null) {
				setNextSplit(preSplit, nextSplit);
			} else {

				// 没有上级 split 了，就把自己从 content 中移除
				this.contentSplit.setLeftComponent(null);
				this.contentSplit.revalidate();
				this.contentSplit.repaint();
				if (nextSplit != null) {
					this.contentSplit.setLeftComponent(nextSplit);
				}
			}
			this.mapGroups.remove(group);
		} else {
			this.contentSplit.setRightComponent(null);
		}
		return true;
	}

	private JSplitPane findPreSplit(MdiGroup group) {
		int index = this.mapGroups.indexOf(group);
		MdiGroup preGroup = index > 0 ? this.mapGroups.get(index - 1) : null;
		return preGroup == null ? null : this.splits.get(preGroup);
	}

	private JSplitPane findNextSplit(MdiGroup group) {
		int index = this.mapGroups.indexOf(group);
		MdiGroup nextGroup = index >= 0 && index + 1 < this.mapGroups.size() ? this.mapGroups.get(index + 1) : null;
		return nextGroup == null ? null : this.splits.get(nextGroup);
	}

	private void setNextSplit(JSplitPane split, JSplitPane nextSplit) {
		if (split == null) {
			return;
		}

		if (nextSplit != null) {
			split.setRightComponent(nextSplit);
		} else {
			split.setRightComponent(null);
		}
		resetDividerSize(split);
	}

	public boolean validateGroup(MdiGroup group) {
		return group != null && group.getMdiContainer() == this.container;
	}

	@Override
	public void layoutGroups() {
		this.container.addGroup(this.tabularGroup);

		MdiPage[] pages = this.container.getPages();
		for (int i = 0; i < pages.length; i++) {
			MdiPage page = pages[i];

			if (page.getComponent() instanceof IFormTabular) {
				this.tabularGroup.addPage(page);
			} else {
				MdiGroup group = this.container.createGroup();
				addGroup(group);
				group.addPage(page);
			}
		}
	}

	@Override
	public void reset() {
		this.container.remove(this.contentSplit);
		this.mapGroups.clear();
		this.splits.clear();
	}

	private JSplitPane createSplit(MdiGroup group) {
		JSplitPane splitPane = new JSplitPane();

		// remove the border from the split pane
		splitPane.setBorder(null);
		splitPane.setOneTouchExpandable(false);
		splitPane.setLeftComponent(null);
		splitPane.setRightComponent(null);

		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(group);

		if (splitPane.getUI() instanceof BasicSplitPaneUI) {
			BasicSplitPaneDivider divider = ((BasicSplitPaneUI) splitPane.getUI()).getDivider();
			divider.setBorder(null);
		}
		return splitPane;
	}

	/**
	 * 设置 Divider 的大小，如果 rightComponent 为 null，设置为 0，否则设置为 3
	 *
	 * @param splitPane
	 */
	private void resetDividerSize(JSplitPane splitPane) {
		splitPane.setDividerSize(splitPane.getRightComponent() == null ? 0 : 3);
	}

	/**
	 * 调整 DividerProportion，为其计算一个合适的 DividerProportion
	 */
	private void adjustDividerProportion() {
		for (int i = 0; i < this.mapGroups.size(); i++) {
			this.splits.get(this.mapGroups.get(i)).setResizeWeight(1d / (this.mapGroups.size() - i));
		}
	}

	private class PageAddedHandler implements PageAddingListener {

		@Override
		public void pageAdding(PageAddingEvent e) {

			// 验证一下，属性表如果没添加到 tabularGroup 里，或者场景/地图窗口添加到 tabularGroup 里了，就纠正一下
			if (e.getOperation() == Operation.ADD) {
				MdiPage page = e.getPage();
				MdiGroup group = page.getGroup();

				if (page != null) {
					if (page.getComponent() instanceof IFormTabular && group != BindLayoutStrategy.this.tabularGroup) {

						// 如果将要添加一个 IFormTabular 到非指定的 tabularGroup，那么就处理一下，请它过去
						e.setCancel(true);
						BindLayoutStrategy.this.tabularGroup.addPage(page);
					} else if (page.getComponent() instanceof IForm && group == BindLayoutStrategy.this.tabularGroup) {

						// 如果将要添加一个非 IFormTabular 的 IForm 到指定的 tabularGroup，那么就处理一下，请它离开
						e.setCancel(true);
						BindLayoutStrategy.this.container.getSelectedGroup().addPage(page);
					}
				}
			}
		}
	}

}
