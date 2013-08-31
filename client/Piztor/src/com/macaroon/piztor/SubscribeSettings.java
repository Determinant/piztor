package com.macaroon.piztor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.macaroon.piztor.RGroup;

public class SubscribeSettings extends Activity {

	private static final int BUTTON_ADD = 1;
	private static final int BUTTON_DELETE = 2;

	private ListView mListView;
	private ArrayList<HashMap<String, Object>> mList;
	private TextView mShowInfo;
	private EditText edit_company;
	private EditText edit_section;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.subscribe_settings);

		mListView = (ListView) findViewById(R.id.listView1);
		edit_company = (EditText) findViewById(R.id.subscribe_company);
		edit_section = (EditText) findViewById(R.id.subscribe_section);
		mList = new ArrayList<HashMap<String, Object>>();
		final MySimpleAdapter simpleAdapter = new MySimpleAdapter(this, mList,
				R.layout.subscribe_item, new String[] {"subscribe_text", "btnadd"}, new int[] {R.id.textView1, R.id.button_add});
		mListView.setAdapter(simpleAdapter);
		//TODO get current subscribe
		for (RGroup i : Infomation.sublist) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			int cc = i.company;
			int ss = i.section;
			map.put("subscribe_text", cc+ "连 " + ss + "班");
		}
		Button btnadd = (Button)findViewById(R.id.button_add);
		btnadd.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int cc = Integer.parseInt(edit_company.getText().toString());
				int ss = Integer.parseInt(edit_section.getText().toString());
				simpleAdapter.notifyDataSetChanged();
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("subscribe_text", cc + "连 " + ss + "班");
				mList.add(map);
				mShowInfo.setText("添加了一条订阅");
			}
		});
		mShowInfo = (TextView) findViewById(R.id.textView1);
	}

	private class MySimpleAdapter extends SimpleAdapter {

		public MySimpleAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final int mPosition = position;
			convertView = super.getView(position, convertView, parent);
			Button buttonDelete = (Button) convertView
					.findViewById(R.id.button_delete);
			buttonDelete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					mHandler.obtainMessage(BUTTON_DELETE, mPosition, 0).sendToTarget();
				}
			});
			return convertView;
		}

		private Handler mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				switch (msg.what) {
				case BUTTON_ADD:
					HashMap<String, Object> map = new HashMap<String, Object>();
					mList.add(map);
					notifyDataSetChanged();
					break;
					
				case BUTTON_DELETE:
					mList.remove(msg.arg1);
					notifyDataSetChanged();
					mShowInfo.setText("删除了第" + (msg.arg1 + 1) + "条订阅");
					break;
				}
			}

		};

	}
}