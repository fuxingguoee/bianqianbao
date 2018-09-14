package com.il360.bianqianbao.adapter;

import java.util.List;

import com.il360.bianqianbao.R;
import com.il360.bianqianbao.model.user.Bank;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BankAdapter extends BaseAdapter{
	private Context context;
	private LayoutInflater mInflater;
	private List<Bank> list;
	
	public BankAdapter(List<Bank> list, Context context) {
		this.context = context;
		this.list = list;
	}
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if(convertView==null){
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.bank_list_childitem, null);
			holder.bankName = (TextView) convertView.findViewById(R.id.bankName);			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		holder.bankName.setText(list.get(position).getBankName());
		return convertView;
	}
	class ViewHolder{
		TextView bankName;
	}
}
