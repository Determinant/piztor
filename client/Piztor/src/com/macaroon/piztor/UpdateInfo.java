package com.macaroon.piztor;

import java.lang.ref.WeakReference;
import java.util.Vector;

import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.macaroon.piztor.Settings.ReCall;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateInfo extends PiztorAct {
	TextView userName, nickName;
	EditText oldPass, newPass;
	Button submit;

	static class ReCall extends Handler {
		WeakReference<UpdateInfo> outerClass;

		ReCall(UpdateInfo activity) {
			outerClass = new WeakReference<UpdateInfo>(activity);
		}

		@Override
		public void handleMessage(Message m) {
			UpdateInfo out = outerClass.get();
			if (out == null) {
				System.out.println("act被回收了");
			}
			switch (m.what) {
			case Res.Login:// 上传自己信息成功or失败
				Log.d("update location", "successfull");
				break;
			case Res.UserInfo:// 得到用户信息
				ResUserInfo userInfo = (ResUserInfo) m.obj;
				System.out.println("revieve ........" + userInfo.uinfo.size());
				Vector<RUserInfo> uinfo = userInfo.uinfo;
				for (RUserInfo info : uinfo) {
					System.out
							.println(info.latitude + "     " + info.longitude);
					UserInfo r = out.mapInfo.getUserInfo(info.uid);
					if (r != null) {
						r.setInfo(info.gid.company, info.gid.section, info.sex,
								info.nickname);
						r.setLocation(info.latitude, info.longitude);
					} else {
						r = new UserInfo(info.uid);
						r.setInfo(info.gid.company, info.gid.section, info.sex,
								info.nickname);
						r.setLocation(info.latitude, info.longitude);
						out.mapInfo.addUserInfo(r);
					}
				}
				break;
			case Res.Logout:// 登出
				out.appMgr.trigger(AppMgr.logout);
				break;
			case Res.SendMessage:
				Log.d(LogInfo.resquest, "send message successfully");
				break;
			case Res.SetPassword:
				ResSetPassword res = (ResSetPassword) m.obj;
				out.receiveMessage("修改成功，请重新登录");
				out.appMgr.trigger(AppMgr.logout);
				break;
			case -1:
				EException eException = (EException) m.obj;
				if (eException.Etype == EException.EPasswordFailedException) 
					out.receiveMessage("密码错误");
				else {
					out.appMgr.trigger(AppMgr.logout);
				}
				break;
			default:
				break;
			}
		}
	}
	ReCall handler = new ReCall(this);
	void receiveMessage(String msg) {
		Log.d("recieve message", msg);
		Toast toast = Toast.makeText(getApplicationContext(), msg,
				Toast.LENGTH_LONG);
		toast.show();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		id = "updateInfo";
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_updateinfo);
	}

	@Override
	protected void onStart() {
		super.onStart();
		oldPass = (EditText) findViewById(R.id.password_old);
		newPass = (EditText) findViewById(R.id.password_new);
		submit = (Button) findViewById(R.id.btn_submit);
		userName = (TextView) findViewById(R.id.username);
		nickName = (TextView) findViewById(R.id.nickname);
		userName.setText(app.username);
		nickName.setText(app.mapInfo.myInfo.nickname);
		submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String oldpass = oldPass.getText().toString();
				String newpass = newPass.getText().toString();
				ReqSetPassword req = new ReqSetPassword(app.token,
						app.username, oldpass, newpass, System
								.currentTimeMillis(), 3000);
				transam.send(req);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		transam.setHandler(handler);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.update_info, menu);
		return true;
	}

}
