package com.example.piztor;

import java.io.Serializable;
import android.content.Intent;

public class Controller {
	String userToken;
	Running run;
	
	class Person {
		int id;
		double locationX, locationY;
	}
	

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
		System.out.println(x + "  xxxx " + y);
		run.v.changMyLocation(x, y);
	}
}
