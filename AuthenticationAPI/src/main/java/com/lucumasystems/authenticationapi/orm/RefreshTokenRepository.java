package com.lucumasystems.authenticationapi.orm;

import com.lucumasystems.authenticationapi.entity.RefreshToken;
import com.lucumasystems.authenticationapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Query("SELECT r FROM RefreshToken r INNER JOIN r.user u WHERE u = :user_detail")
    Optional<RefreshToken> findTokenByUser(@Param("user_detail") User user);
    @Query("SELECT r FROM RefreshToken r WHERE r.token = :token ")
    Optional<RefreshToken> findByToken(@Param("token") String refreshToken);
}
