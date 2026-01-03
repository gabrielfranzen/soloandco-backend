package utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.*;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

  private static volatile PrivateKey PRIVATE;
  private static volatile PublicKey  PUBLIC;

  private static String cfg(String prop, String env, String def) {
    var p = System.getProperty(prop);
    if (p != null && !p.isBlank()) return p;
    p = System.getenv(env);
    return p != null && !p.isBlank() ? p : def;
  }

  private static synchronized void ensureKeysLoaded() {
    if (PRIVATE != null && PUBLIC != null) return;
    var privPath = cfg("jwt.privateKeyPath", "JWT_PRIVATE_KEY_PATH", null);
    var pubPath  = cfg("jwt.publicKeyPath",  "JWT_PUBLIC_KEY_PATH",  null);
    if (privPath == null || pubPath == null)
      throw new IllegalStateException("Defina jwt.privateKeyPath e jwt.publicKeyPath");

    try {
      PRIVATE = loadPrivateKeyPem(Path.of(privPath));
      PUBLIC  = loadPublicKeyPem(Path.of(pubPath));
    } catch (Exception e) {
      throw new IllegalStateException("Falha ao carregar chaves RSA: " + e.getMessage(), e);
    }
  }

  private static PrivateKey loadPrivateKeyPem(Path path) throws IOException, GeneralSecurityException {
    String pem = Files.readString(path)
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replaceAll("\\s", "");
    byte[] der = Base64.getDecoder().decode(pem);
    return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
  }

  private static PublicKey loadPublicKeyPem(Path path) throws IOException, GeneralSecurityException {
    String pem = Files.readString(path)
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replaceAll("\\s", "");
    byte[] der = Base64.getDecoder().decode(pem);
    return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
  }

  public static String generateToken(String subject, String roles, long minutes) {
    ensureKeysLoaded();
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(subject)
        .claims(Map.of("roles", roles == null ? "" : roles))
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(minutes * 60)))
        .signWith(PRIVATE, Jwts.SIG.RS256)   // API nova
        .compact();
  }

  public static Jws<Claims> parse(String jwt) throws JwtException {
    ensureKeysLoaded();
    // verifyWith + parseSignedClaims: API nova 0.12+
    return Jwts.parser()
        .verifyWith(PUBLIC)
        .build()
        .parseSignedClaims(jwt);
  }
}