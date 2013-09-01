package com.macaroon.piztor;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
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
                out.appMgr.trigger(AppMgr.logout);
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
            	out.reDraw(out.listGroup);
            	out.app.sublist =(Vector<RGroup>) out.listGroup.clone();
            	break;
            case -1:
            	EException eException = (EException) m.obj;
            	/////////////////TODO
				if (eException.Etype == EException.ESubscribeFailedException) {
					out.receiveMessage("关注信息无效");
					for (RGroup i : out.listGroup) {
						Log.d("sub", i.company  + "   " + i.section);
					}
					for(RGroup i : out.app.sublist) {
						Log.d("sub", "***" + i.company + "   " + i.section);
					}
					out.reDraw(out.app.sublist);
				}
				else out.appMgr.trigger(AppMgr.logout);
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
    private Set<Integer> recSubcribe;
    MySimpleAdapter simpleAdapter;
    
    public void reDraw(Vector<RGroup> list) {
    	recSubcribe = new HashSet<Integer>();
    	listGroup = new Vector<RGroup>();
    	for (RGroup i : list) {
    		if (i.section == 255) {
    			recSubcribe.add(i.company);
    		}
    	}
    	mList.clear();
    	simpleAdapter.notifyDataSetChanged();
    	for (int i : recSubcribe) {
    		HashMap<String, Object> map = new HashMap<String, Object>();
        	map.put("subscribe_text", i + "连");
        	mList.add(map);
        	RGroup listItem = new RGroup(i, 255);
			listGroup.add(listItem);
        	simpleAdapter.notifyDataSetChanged();
    	}
    	for (RGroup i : list) {
    		if (recSubcribe.contains(i.company)) continue;
    		HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("subscribe_text", i.company + "连 " + i.section + "班");
            mList.add(map);
            listGroup.add(i);
            simpleAdapter.notifyDataSetChanged();
    	}
    }

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
        Toast toast = Toast.makeText(getApplicationContext(),msg, 2000);
		toast.setGravity(Gravity.TOP, 0, 120);
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
        mShowInfo = (TextView) findViewById(R.id.textView1);
        mShowInfo.setText("若要关注整个连请将班级号留空");
        
        listGroup = new Vector<RGroup>();
        recSubcribe = new HashSet<Integer>();
        
        app = (myApp) getApplication();
        app.transam.setHandler(handler);
        mListView = (ListView) findViewById(R.id.listView1);
        edit_company = (EditText) findViewById(R.id.subscribe_company);
        edit_section = (EditText) findViewById(R.id.subscribe_section);
        mList = new ArrayList<HashMap<String, Object>>();
        
        simpleAdapter = new MySimpleAdapter(this, mList,
                R.layout.subscribe_item, new String[] { "subscribe_text",
                        "btnadd" },
                new int[] { R.id.textView1, R.id.button_add });
        
        mListView.setAdapter(simpleAdapter);
        // TODO get current subscribe
        
        for (RGroup i : ((myApp) getApplication()).sublist) {
        	if (i.section == 255)
        		recSubcribe.add(i.company);
        }
        for (int i : recSubcribe) {
        	HashMap<String, Object> map = new HashMap<String, Object>();
        	map.put("subscribe_text", i + "连");
        	mList.add(map);
        	
        	RGroup listItem = new RGroup(i, 255);
        	listGroup.add(listItem);
        	simpleAdapter.notifyDataSetChanged();
        }
        for (RGroup i : ((myApp) getApplication()).sublist) {
        	if (i.section == 255) continue;
        	//else
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("subscribe_text", i.company + "连 " + i.section + "班");
            mList.add(map);
            
            RGroup listItem = new RGroup(i.company, i.section);
            listGroup.add(listItem);
            simpleAdapter.notifyDataSetChanged();
        }
        Button btnadd = (Button) findViewById(R.id.button_add);
        btnadd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            	
            	int cc,ss;
            	//Log.d("sub", edit_company.getText().toString() + "   " + edit_section.getText().toString());
            	if (edit_company.getText().toString().length() == 0) {
            		mShowInfo.setText("连号为空,请输入正确的连队、班级号(关注整个连，则班号留空)");
            		Log.d("sub", "连号为空");
                	return;
            	} else {
            		cc = Integer.parseInt(edit_company.getText().toString());
            		if (cc <= 0 || cc > 40) {
            			mShowInfo.setText("连号超限,请输入正确的连队、班级号(关注整个连，则班号留空)");
            			Log.d("sub", "连号超限" + cc); 
                		return;
            		}
            	}
            	
            	if (edit_section.getText().toString().length() == 0) {
            		ss = 255;
            		Log.d("sub", "订阅全连" + cc);
            	} else {
            		ss = Integer.parseInt(edit_section.getText().toString());
            		if (ss <= 0 || ss > 20) {
            			mShowInfo.setText("班号超限,请输入正确的连队、班级号(关注整个连，则班号留空)");
            			Log.d("sub", "班号超限" + ss);
            			return;
            		}
            	}
            		
                // TODO get real company and section number
                if (recSubcribe.contains(cc)) return;
                
                if (ss == 255) {
                	recSubcribe.add(cc);
                	for (int i = 0; i < listGroup.size(); i++) {
                		if (listGroup.size() > 0)
                			if (listGroup.get(i).company == cc) {
                				listGroup.remove(i);
                				mList.remove(i);
                				i--;
                				simpleAdapter.notifyDataSetChanged();
                			}
                	}
                	
                	RGroup listItem = new RGroup(cc, 255);
                	listGroup.add(listItem);
                	HashMap<String, Object> map = new HashMap<String, Object>();
                	map.put("subscribe_text", cc + "连");
                	mList.add(map);
                	simpleAdapter.notifyDataSetChanged();
                	subscribe();
                	mShowInfo.setText("已关注第" + cc + "连");
                	return;
                }
                RGroup listItem = new RGroup(cc, ss);
                for (RGroup i : listGroup) {
                	if (i.company == cc && i.section == ss)
                		return;
                }
                	listGroup.add(listItem);
                
                	HashMap<String, Object> map = new HashMap<String, Object>();
                	map.put("subscribe_text", cc + "连 " + ss + "班");
                	mList.add(map);
                
                	subscribe();
                
                	simpleAdapter.notifyDataSetChanged();
                	mShowInfo.setText("已关注第" + cc + "连" + ss + "班");
            }
        });
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
                case BUTTON_DELETE:
                	if (listGroup.get(msg.arg1).section == 255)
                		recSubcribe.remove(listGroup.get(msg.arg1).company);
                    mList.remove(msg.arg1);
                    listGroup.remove(msg.arg1);
                    subscribe();
                    notifyDataSetChanged();
                    mShowInfo.setText("删除了第" + (msg.arg1 + 1) + "条关注");
                    break;
                }
            }

        };

    }
}