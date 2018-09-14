package com.il360.bianqianbao.model.address;

import java.io.Serializable;
import java.util.List;

public class ArrayOfUserAddress implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int code;
	private String desc;
	private List<UserAddress> result;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<UserAddress> getResult() {
		return result;
	}

	public void setResult(List<UserAddress> result) {
		this.result = result;
	}

}
