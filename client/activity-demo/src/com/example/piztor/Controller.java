package com.example.piztor;

import java.io.Serializable;
import android.content.Intent;

public class Controller {
	String userToken;
	Running run;

	Controller() {
		run = null;
		userToken = null;
	}

	void setRun(Running run) {
		this.run = run;
	}

	void recieveInfo(Myrespond r) {
		if (r.wrong != null) {
			System.out.println(r.wrong);
		} else {
			System.out.println("yeal!");
		}
	}

	void recieveLocation(double x, double y) {
		run.v.drawLocation(x, y);
	}
}
