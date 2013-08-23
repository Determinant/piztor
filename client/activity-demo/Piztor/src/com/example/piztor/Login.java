package com.example.piztor;

public class Login {
	MainActivity main;
	int tk;
	
	Login(MainActivity main) {
		this.main = main;
	}
	
	void success(int token) {
		//main.start();
		tk = token;
		System.out.println("token!");
	}
	
	void failed() {
		System.out.println("fuck!!");
	}
}
