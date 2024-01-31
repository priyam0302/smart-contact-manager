package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
		// For the HTML Page to be visible instead of home text, we need to remove the
		// rest controller annotation.
	}

	@RequestMapping(value = "/about", method = RequestMethod.GET)
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}

	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public String signup(Model model) {
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User()); // Sending an object of the user. This might be empty or may be having
												// values of a user from db.
		return "signup";
	}

	@RequestMapping(value = "/signin", method = RequestMethod.GET)
	public String login(Model model) {
		model.addAttribute("title", "Login - Smart Contact Manager");
		return "login";
	}

	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult newResult,
			@RequestParam(value = "agreement", defaultValue = "false") Boolean agreement, Model model,
			HttpSession session) {
		try {

			// Model Attribute basically binds the form values to the java objects. So
			// whatever value we'll get from the form, those will be populated in the user
			// object. And since the agreement is not a part of the user object, we are
			// fetching it separately.

			if (!agreement) {
				throw new Exception("Accept the terms and conditions");
			}

			if (newResult.hasErrors()) {
				// The bindingResult will bring the validation results.
				model.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			// This will store the password in an encrypted format in the database

			System.out.println(agreement);
			System.out.println(user.toString());
			userRepository.save(user);
			model.addAttribute("user", new User());
			// Setting an attribute in session which will be of type - Object of class
			// message.
			session.setAttribute("message", new Message("User successfully registered", "alert-success"));
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong! " + e.getMessage(), "alert-danger"));
			return "signup";
		}
	}
}
