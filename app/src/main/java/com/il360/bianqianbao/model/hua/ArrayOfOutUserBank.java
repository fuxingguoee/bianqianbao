package com.il360.bianqianbao.model.hua;

import java.io.Serializable;
import java.util.List;

public class ArrayOfOutUserBank implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int totalCount;
	private List<OutUserBank> list;

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public List<OutUserBank> getList() {
		return list;
	}

	public void setList(List<OutUserBank> list) {
		this.list = list;
	}

}
