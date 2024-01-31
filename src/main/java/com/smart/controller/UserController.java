package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repository.ContactRepository;
import com.smart.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String email = principal.getName();
		User user = userRepository.getUserByEmail(email);
		model.addAttribute("user", user);
	}

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String dashboard(Model model) {
		model.addAttribute("title", "Dashboard - Smart Contact Manager");
		return "normal/user_dashboard";
	}

	@RequestMapping(value = "/add-contacts", method = RequestMethod.GET)
	public String addContacts(Model model) {
		model.addAttribute("title", "Add Contact - Smart Contact Manager");
		model.addAttribute("contact", new Contact());
		return "normal/addContacts";
	}

	@RequestMapping(value = "/process-contact", method = RequestMethod.POST)
	public String registerContact(@Valid @ModelAttribute("contact") Contact contact, BindingResult newResult,
			@RequestParam("fileImage") MultipartFile file, Model model, Principal principal, HttpSession session) {

		try {
			String email = principal.getName();
			User user = userRepository.getUserByEmail(email);

			if (newResult.hasErrors()) {
				model.addAttribute("contact", contact);
				return "normal/addContacts";
			}

			if (file.isEmpty()) {
				contact.setImage("contact.png");
				throw new Exception("File is empty");
			} else {
				// We will be saving the name of the file in contact table and original file
				// will be saved in the img folder and whenever we want to fetch the file, we'll
				// get the name from the db and will match from the folder.
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile(); // This is the place where file is saved
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				// The above will be the path of the file with the name
				// The image will be saved in the target folder of the project.

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				// Here we are copying the received file to the desired location

				System.out.println("File is saved");
			}

			contact.setUser(user);
			user.getContacts().add(contact);
			userRepository.save(user);
			model.addAttribute("contact", new Contact());
			session.setAttribute("message", new Message("Contact successfully added", "alert-success"));
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("contact", contact);
			session.setAttribute("message", new Message("Something went wrong! " + e.getMessage(), "alert-danger"));
		}
		return "normal/addContacts";

	}

	@RequestMapping(value = "/view-contacts/{page}", method = RequestMethod.GET)
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "View Contacts - Smart Contact Manager");
		String email = principal.getName();
		User user = userRepository.getUserByEmail(email);
		Pageable pageAble = PageRequest.of(page, 3);
		Page<Contact> contactList = contactRepository.findContactsByUserId(user.getId(), pageAble);
		model.addAttribute("contactList", contactList);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contactList.getTotalPages());

		return "normal/showContacts";
	}

	@RequestMapping(value = "/contact/{cId}", method = RequestMethod.GET)
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		model.addAttribute("title", "Contact Details - Smart Contact Manager");
		Optional<Contact> contactOptional = contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		String email = principal.getName();
		User user = userRepository.getUserByEmail(email);
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
		}

		return "normal/contactDetails";
	}

	@RequestMapping(value = "/delete/{cId}", method = RequestMethod.GET)
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal,
			HttpSession session) {
		model.addAttribute("title", "View Contacts - Smart Contact Manager");
		Optional<Contact> contactOptional = contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		String email = principal.getName();
		User user = userRepository.getUserByEmail(email);

		String imgName = contact.getImage();
		File deleteFile;
		try {
			deleteFile = new ClassPathResource("static/img").getFile();
			Path path = Paths.get(deleteFile.getAbsolutePath() + File.separator + imgName);
			Files.delete(path);
		} catch (IOException e) {
			e.printStackTrace();
		}

		user.getContacts().remove(contact);
		userRepository.save(user);

		session.setAttribute("message", new Message("Contact Deleted Successfully ", "alert-success"));

		return "redirect:/user/view-contacts/0";
	}

	@RequestMapping(value = "/update/{cId}", method = RequestMethod.POST)
	public String updateContact(@PathVariable("cId") Integer cId, Model model, Principal principal,
			HttpSession session) {

		model.addAttribute("title", "Update Contact - Smart Contact Manager");
		Optional<Contact> contactOptional = contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		model.addAttribute("contact", contact);
		return "normal/updateForm";
	}

	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute("contact") Contact contact,
			@RequestParam("fileImage") MultipartFile file, Model model, HttpSession session, Principal principal) {

		try {
			Contact oldContact = contactRepository.findById(contact.getcId()).get();
			String imgName = oldContact.getImage();
			if (!file.isEmpty()) {
				// Deleting the existing file that the user is changing
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, imgName);
				file1.delete();

				// Updating new image
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			} else {
				contact.setImage(oldContact.getImage());
			}
			String email = principal.getName();
			User user = userRepository.getUserByEmail(email);
			contact.setUser(user);
			contactRepository.save(contact);

		} catch (Exception e) {
			e.printStackTrace();
		}

		model.addAttribute("title", "Update Contact - Smart Contact Manager");

		session.setAttribute("message", new Message("Contact successfully updated", "alert-success"));
		return "normal/updateForm";
	}

	@RequestMapping(value = "/profile", method = RequestMethod.GET)
	public String profile(Model model) {

		model.addAttribute("title", "My Profile - Smart Contact Manager");
		return "normal/myProfile";
	}

	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	public String settings(Model model) {

		model.addAttribute("title", "Settings - Smart Contact Manager");
		return "normal/settings";
	}

	@RequestMapping(value = "/change-password", method = RequestMethod.POST)
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {

		String email = principal.getName();
		User user = userRepository.getUserByEmail(email);

		if (bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
			user.setPassword(bCryptPasswordEncoder.encode(newPassword));
			userRepository.save(user);
			session.setAttribute("message", new Message("Password Successfully changed", "alert-success"));
		} else {
			session.setAttribute("message", new Message("Old Password is incorrect", "alert-danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}

}