package com.example.subscriptionservice.subscription.repository;

import com.example.subscriptionservice.subscription.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
}