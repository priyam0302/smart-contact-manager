package com.smart.controller;

import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ForgotController {
	Random random = new Random(1000);

	@RequestMapping(value = "/forgot", method = RequestMethod.GET)
	public String openEmailForm() {
		return "forgotEmail";
	}

	@RequestMapping(value = "/send-otp", method = RequestMethod.POST)
	public String sendOtp(@RequestParam("email") String email) {
		System.out.println(email);
		int otp = random.nextInt(999999);
		System.out.println(otp);
		return "verifyOtp";
	}
}
