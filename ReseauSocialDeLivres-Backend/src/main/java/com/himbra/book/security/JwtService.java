package com.himbra.book.security;

import com.himbra.book.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.jwtExpiration}")
    private Long jwtExpiration;
    @Value("${jwt.secretKey}")
    private String secretKey;
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        var authorities = userDetails.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).toList();
        return Jwts
                .builder().setSubject(userDetails.getUsername())
                .setExpiration(new Date(System.currentTimeMillis()+jwtExpiration))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .claim("authorities",authorities)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }
    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
    public  boolean validateJwtToken(String authToken, UserDetails userDetails){
        final String username=getUsernameFromJwtToken(authToken);
        return (username.equals(userDetails.getUsername()))&& !isTokenExpired(authToken);
    }

    private boolean isTokenExpired(String authToken) {
        return extractClaim(authToken, Claims::getExpiration).before(new Date());
    }

    public  Claims extractAllClaims(String authToken){
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken).getBody();}

    public String getUsernameFromJwtToken(String token){
        return extractAllClaims(token).getSubject();
        //  SecurityContextHolder.getContext().
    }
    public <T> T extractClaim(String token, Function<Claims,T> claimResolver){
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

}
