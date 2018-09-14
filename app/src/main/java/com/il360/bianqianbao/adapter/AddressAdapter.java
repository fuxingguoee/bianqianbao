package com.il360.bianqianbao.adapter;

import java.util.List;

import com.il360.bianqianbao.R;
import com.il360.bianqianbao.model.address.Address;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AddressAdapter extends BaseAdapter {
	private Context context;
	private LayoutInflater mInflater;
	private List<Address> list;
	private int flag;

	public AddressAdapter(List<Address> list, Context context, int flag) {
		this.context = context;
		this.list = list;
		this.flag = flag;
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
		if (convertView == null) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.listitem_string, null);
			holder.tvListString = (TextView) convertView.findViewById(R.id.tv_list_string);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (flag == 1) {
			holder.tvListString.setText(list.get(position).getProvince());
		} else if (flag == 2) {
			holder.tvListString.setText(list.get(position).getCity());
		} else if (flag == 3) {
			holder.tvListString.setText(list.get(position).getArea());
		}

		return convertView;
	}

	class ViewHolder {
		TextView tvListString;
	}
}
