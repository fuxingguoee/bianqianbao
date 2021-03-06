package com.il360.bianqianbao.model.order;

import java.io.Serializable;
import java.util.List;

public class ArrayOfPayFeeOrder implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int code;
	private String desc;
	private List<PayFeeOrder> result;

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

	public List<PayFeeOrder> getResult() {
		return result;
	}

	public void setResult(List<PayFeeOrder> result) {
		this.result = result;
	}
	
	

}
