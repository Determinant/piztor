package com.macaroon.piztor;

import java.util.*;

import android.annotation.SuppressLint;

@SuppressLint("UseSparseArrays")
public class ActMgr {
	final static int Create = -1;
	// event
	PiztorAct act;
	ActStatus nowStatus;
	HashMap<ActStatus, HashMap<Integer, ActStatus>> mp;

	ActMgr(PiztorAct act, ActStatus nowStatus, ActStatus[] r) {
		this.act = act;
		this.nowStatus = nowStatus;
		nowStatus.enter(Create);
		mp = new HashMap<ActStatus, HashMap<Integer, ActStatus>>();
		for (int i = 0; i < r.length; i++) {
			mp.put(r[i], new HashMap<Integer, ActStatus>());
		}
	}

	void trigger(int event) {
		System.out.println(act.id + " : " + event);
		if (mp.get(nowStatus).containsKey(event)) {
			nowStatus.leave(event);
			nowStatus = mp.get(nowStatus).get(event);
			nowStatus.enter(event);
		} else if (AppMgr.mp.get(act.getClass()).containsKey(event)) {
			AppMgr.trigger(event);
		} else {
			System.out.println("can not trigger the event at " + act.id + " : "
					+ event);
		}
	}

	void add(ActStatus a, int event, ActStatus b) {
		if (mp.containsKey(a)) {
			HashMap<Integer, ActStatus> h = mp.get(a);
			h.put(event, b);
			mp.put(a, h);
		} else {
			HashMap<Integer, ActStatus> h = new HashMap<Integer, ActStatus>();
			h.put(event, b);
			mp.put(a, h);
		}
	}
}

abstract class ActStatus {
	abstract void enter(int e);

	abstract void leave(int e);
}

class EmptyStatus extends ActStatus {
	@Override
	void enter(int e) {
	}

	@Override
	void leave(int e) {
	}

}
