package com.martini.fish_gen.service;

import com.martini.fish_gen.model.dto.TokenDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static com.martini.fish_gen.configuration.CustomSecurityConfiguration.AUTHORITIES_CLAIM;

@Service
public class TokenService {

    private static final String ISSUER = "fish-gen";

    private final JwtEncoder jwtEncoder;
    private final Duration expiration;

    public TokenService(JwtEncoder jwtEncoder, @Value("${jwt.expiration}") Duration expiration) {
        this.jwtEncoder = jwtEncoder;
        this.expiration = expiration;
    }

    public TokenDto generateToken(Authentication authentication) {
        Instant now = Instant.now();
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .claim(AUTHORITIES_CLAIM, authorities)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build(); // must match the key type
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenDto(token, "Bearer", expiration.toSeconds());
    }
}
