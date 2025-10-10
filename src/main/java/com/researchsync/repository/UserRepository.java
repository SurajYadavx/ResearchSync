package com.researchsync.repository;

import com.researchsync.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    List<User> findByUserType(User.UserType userType);

    List<User> findByEmailContainingIgnoreCase(String emailFragment);

    List<User> findByNameContainingIgnoreCase(String nameFragment);

    @Query("SELECT u FROM User u WHERE u.university = :university")
    List<User> findByUniversity(@Param("university") String university);

    @Query("SELECT u FROM User u WHERE u.department = :department")
    List<User> findByDepartment(@Param("department") String department);

    @Query("SELECT u FROM User u WHERE u.isVerified = true")
    List<User> findVerifiedUsers();

    @Query("SELECT u FROM User u WHERE u.isVerified = false")
    List<User> findUnverifiedUsers();

    boolean existsByEmail(String email);
}