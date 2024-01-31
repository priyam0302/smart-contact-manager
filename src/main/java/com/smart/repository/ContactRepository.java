package com.smart.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {

	@Query("FROM Contact c where c.user.id = :userId")
	public Page<Contact> findContactsByUserId(@Param("userId") int userId, Pageable pageable);

	// By giving this name to the search method without the query, the spring data
	// jpa will automatically create it body based on the method's name. It will
	// find the list of contacts of the logged in user based on the query
	public List<Contact> findByNameContainingAndUser(String name, User user);

}
