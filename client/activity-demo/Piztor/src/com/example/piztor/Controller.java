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
			System.out.println(r.contain.size());
			for (int i = 0; i < r.contain.size(); i++) {
				System.out.println(r.contain.get(i).toString());
			}
			if ((Integer) r.contain.firstElement() == 3) {
				// int n = (Integer) r.contain.get(1);
				// if (n > 0) {
				Rmsg t1 = (Rmsg) r.contain.get(2);
				Rmsg t2 = (Rmsg) r.contain.get(3);
				run.v.changMyLocation(t1.lot, t1.lat);
				run.v.changHerLocation(t2.lot, t2.lat);

				// }
			}
			System.out.println("yeal!");
		}
	}

	void recieveLocation(double x, double y) {
		System.out.println(x + "  xxxx " + y);
		run.v.changMyLocation(x, y);
	}
}
