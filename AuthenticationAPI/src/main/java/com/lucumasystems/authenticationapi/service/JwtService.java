package com.lucumasystems.authenticationapi.service;

import com.lucumasystems.authenticationapi.entity.RefreshToken;
import com.lucumasystems.authenticationapi.entity.User;
import com.lucumasystems.authenticationapi.orm.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {
    @Value("${token}")
    private String tokenSecretKey;
    private final RefreshTokenRepository repository;

    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Extract the token excluding "Bearer "
        } else {
            log.error("Invalid JWT token in Authorization header");
            throw new IllegalArgumentException("Invalid JWT token in Authorization header");
        }
    }

    public String getUsername(String jwtToken) {
        return extractClaim(jwtToken, claims -> {
            Map<String, Object> adminDetails = claims.get("admin", Map.class);
            return (String) adminDetails.get("username");
        });
    }

    public int getUserId(String jwtToken) {
        return extractClaim(jwtToken, claims -> {
            Map<String, Object> adminDetails = claims.get("admin", Map.class);
            return (int) adminDetails.get("userId");
        });
    }

    public List<String> getRoles(String jwtToken) {
        return extractClaim(jwtToken, claims -> {
            Map<String, Object> adminDetails = claims.get("admin", Map.class);
            return (List<String>) adminDetails.get("role");
        });
    }

    public List<String> getPermissions(String jwtToken) {
        return extractClaim(jwtToken, claims -> {
            Map<String, Object> adminDetails = claims.get("admin", Map.class);
            return (List<String>) adminDetails.get("permissions");
        });
    }
    public  <T> T extractClaim(String tokenSecretKey, Function<Claims,T> fn) {
        final Claims claims = extractAllClaims(tokenSecretKey);
        return fn.apply(claims);
    }


    public boolean isValidToken(String token, UserDetails applicationUser) {
        final String username = getUsername(token);
        RefreshToken refreshToken = repository.findByToken(token).orElse(null);
        if (refreshToken == null) {
            return false;
        }
        return username.equals(applicationUser.getUsername()) && !isExpired(token);
    }

    public String refreshToken(String token, User applicationUser,LocalDateTime expiresAt) {
        RefreshToken previousToken = repository.findTokenByUser(applicationUser).orElse(null);
        RefreshToken refreshToken;
        if(previousToken == null){
            refreshToken = RefreshToken.builder()
                    .token(token)
                    .user(applicationUser)
                    .expiryDate(expiresAt)
                    .build();
            repository.save(refreshToken);
            return refreshToken.getToken();
        }
        refreshToken = previousToken;
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(expiresAt);
        repository.save(refreshToken);
        return refreshToken.getToken();
    }

    public String generate(Map<String,Object> map, User user, Date expiry){
        log.info("Expiry date {}",expiry);
        Date date = new Date(System.currentTimeMillis());
        String token =  Jwts.builder()
                .subject(user.getUsername())
                .claim(user.getUsername(), map)
                .issuedAt(date)
                .expiration(expiry)
                .signWith(getSecretKey())
                .compact();
        return refreshToken(token,user,LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    public static Date getDate(int value, char unit) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        switch (unit){
            case 'H'-> currentDateTime = currentDateTime.plusHours(value);
            case 'M'-> currentDateTime = currentDateTime.plusMinutes(value);
            case 'S'-> currentDateTime = currentDateTime.plusSeconds(value);
            case 'D'-> currentDateTime = currentDateTime.plusDays(value);
            case 'Y'-> currentDateTime = currentDateTime.plusYears(value);
            default -> throw new IllegalArgumentException("Invalid parameters");
        }
        log.info("Created date => {}",currentDateTime);
        return Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public boolean isExpired(String tokenSecretKey){
        return extractClaim(tokenSecretKey,Claims::getExpiration).before(new Date(System.currentTimeMillis()));
    }
    public Claims extractAllClaims(String token){
        return Jwts
                .parser()
                .setSigningKey(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    private Key getSecretKey(){
        byte[] keyBytes = Decoders.BASE64.decode(tokenSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }



}
