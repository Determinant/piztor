package com.macaroon.piztor;

import java.lang.ref.WeakReference;
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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.macaroon.piztor.RGroup;
import com.macaroon.piztor.Settings.ReCall;

public class SubscribeSettings extends PiztorAct {

    private static final int BUTTON_ADD = 1;
    private static final int BUTTON_DELETE = 2;

    static class ReCall extends Handler {
        WeakReference<SubscribeSettings> outerClass;

        ReCall(SubscribeSettings activity) {
            outerClass = new WeakReference<SubscribeSettings>(activity);
        }

        @Override
        public void handleMessage(Message m) {
            SubscribeSettings out = outerClass.get();
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
                out.actMgr.trigger(AppMgr.logout);
                break;
            case Res.PushMessage:
                ResPushMessage pushMessage = (ResPushMessage) m.obj;
                out.receiveMessage(pushMessage.message);
                break;
            case Res.SendMessage:
                Log.d(LogInfo.resquest, "send message successfully");
                break;
            case Res.PushLocation:
                ResPushLocation pushLocation = (ResPushLocation) m.obj;
                out.upMapInfo(pushLocation.l);
                break;
            case Res.PushMarker:
                ResPushMarker pushMarker = (ResPushMarker) m.obj;
                MarkerInfo markerInfo = new MarkerInfo();
                markerInfo.level = pushMarker.level;
                markerInfo.markerPoint = new GeoPoint((int)(pushMarker.latitude * 1e6), (int)(pushMarker.longitude * 1e6));
                markerInfo.markerTimestamp = pushMarker.deadline;
                out.mapInfo.markerInfo = markerInfo;
                break;
            case Res.Subscription:
            	out.app.sublist = out.listGroup;
            	break;
            case -1:
                out.actMgr.trigger(AppMgr.logout);
            default:
                break;
            }
        }
    }
    ReCall handler = new ReCall(this);
    
    private ListView mListView;
    private ArrayList<HashMap<String, Object>> mList;
    private TextView mShowInfo;
    private EditText edit_company;
    private EditText edit_section;
    private Vector<RGroup> listGroup;
    

    void upMapInfo(Vector<RLocation> l) {
        for (RLocation i : l) {
            UserInfo info = mapInfo.getUserInfo(i.id);
            if (info != null) {
                info.setLocation(i.latitude, i.longitude);
            } else {
                info = new UserInfo(i.id);
                info.setLocation(i.latitude, i.longitude);
                mapInfo.addUserInfo(info);
            }
        }
    }
    
    void receiveMessage(String msg) {
        Log.d("recieve message", msg);
        Toast toast = Toast.makeText(getApplicationContext(), msg,
                Toast.LENGTH_LONG);
        toast.show();
    }
    
    void subscribe() {
        ReqSubscription req = new ReqSubscription(app.token, app.username,
                listGroup.size(), listGroup, System.currentTimeMillis(), 3000);
        app.transam.send(req);
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscribe_settings);
        listGroup = new Vector<RGroup>();
        app = (myApp) getApplication();
        app.transam.setHandler(handler);
        mListView = (ListView) findViewById(R.id.listView1);
        edit_company = (EditText) findViewById(R.id.subscribe_company);
        edit_section = (EditText) findViewById(R.id.subscribe_section);
        mList = new ArrayList<HashMap<String, Object>>();
        final MySimpleAdapter simpleAdapter = new MySimpleAdapter(this, mList,
                R.layout.subscribe_item, new String[] { "subscribe_text",
                        "btnadd" },
                new int[] { R.id.textView1, R.id.button_add });
        mListView.setAdapter(simpleAdapter);
        // TODO get current subscribe
        for (RGroup i : ((myApp) getApplication()).sublist) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            int cc = i.company;
            int ss = i.section;
            
            map.put("subscribe_text", cc + "连 " + ss + "班");
            mList.add(map);
            
            RGroup listItem = new RGroup(cc, ss);
            listGroup.add(listItem);
            simpleAdapter.notifyDataSetChanged();
        }
        Button btnadd = (Button) findViewById(R.id.button_add);
        btnadd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int cc = Integer.parseInt(edit_company.getText().toString());
                int ss = Integer.parseInt(edit_section.getText().toString());
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("subscribe_text", cc + "连 " + ss + "班");
                mList.add(map);
                
                RGroup listItem = new RGroup(cc, ss);
                listGroup.add(listItem);
                subscribe();
                
                simpleAdapter.notifyDataSetChanged();
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
                    mHandler.obtainMessage(BUTTON_DELETE, mPosition, 0)
                            .sendToTarget();
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
                    RGroup listItem = listGroup.get(msg.arg1);
                    listGroup.remove(msg.arg1);
                    subscribe();
                    notifyDataSetChanged();
                    mShowInfo.setText("删除了第" + (msg.arg1 + 1) + "条订阅");
                    break;
                }
            }

        };

    }
}