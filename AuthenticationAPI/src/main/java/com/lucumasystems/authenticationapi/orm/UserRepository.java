package com.lucumasystems.authenticationapi.orm;

import com.lucumasystems.authenticationapi.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.enabled = true")
    Optional<User> findActiveUserByUsername(@Param("username") String username);
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.enabled = true")
    Optional<User> findActiveUserById(@Param("id") int id);
    @Query("SELECT u FROM User u WHERE u.enabled = true ")
    Page<User> findAllPaged(Pageable pageable);

}
