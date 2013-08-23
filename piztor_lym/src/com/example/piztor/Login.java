package com.example.piztor;

public class Login {
	MainActivity main;
	
	Login(MainActivity main) {
		this.main = main;
	}
	
	void success(int token) {
		main.start();
		System.out.println("token!");
	}
	
	void failed() {
		System.out.println("fuck!!");
	}
}
