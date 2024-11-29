package com.lucumasystems.authenticationapi.service;

import com.lucumasystems.authenticationapi.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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
public class JwtService {
    @Value("${token}")
    private String tokenSecretKey;
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Extract the token excluding "Bearer "
        } else {
            log.error("Invalid JWT token in Authorization header");
            throw new IllegalArgumentException("Invalid JWT token in Authorization header");
        }
    }

    public String getUsername(String jwtToken) {
        return extractClaim(jwtToken, claims -> claims.get("username", String.class));
    }

    public int getUserId(String jwtToken) {
        return extractClaim(jwtToken, claims -> claims.get("userId", Integer.class));
    }

    public List<String> getRoles(String jwtToken) {
        return extractClaim(jwtToken, claims -> claims.get("roles", List.class));
    }
    public List<String> getPermissions(String jwtToken) {
        return extractClaim(jwtToken, claims -> claims.get("permissions", List.class));
    }

    private <T> T extractClaim(String tokenSecretKey, Function<Claims,T> fn) {
        final Claims claims = extractAllClaims(tokenSecretKey);
        return fn.apply(claims);
    }


    public boolean isValidToken(String token, UserDetails applicationUser) {
        final String username = getUsername(token);
        return username.equals(applicationUser.getUsername()) && !isExpired(token);
    }

    public String generate(Map<String,Object> map, User user, Date expiry){
        log.info("Expiry date {}",expiry);
        Date date = new Date(System.currentTimeMillis());
        return Jwts.builder()
                .subject(user.getUsername())
                .claim(user.getUsername(), map)
                .issuedAt(date)
                .expiration(expiry)
                .signWith(getSecretKey())
                .compact();
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

    private boolean isExpired(String tokenSecretKey){
        return extractClaim(tokenSecretKey,Claims::getExpiration).before(new Date(System.currentTimeMillis()));
    }

    private Claims extractAllClaims(String token){
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
