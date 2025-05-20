package com.github.jaguililla.appointments.it;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.undertow.util.Headers.REALM;
import static java.nio.charset.StandardCharsets.UTF_8;

public class JwtTokenManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenManager.class);
    private static final Base64.Decoder BASE64_DECODER = Base64.getMimeDecoder();

    private final Algorithm ALGORITHM;

    public JwtTokenManager() {
        try {
            RSAPrivateKey privateKey = readRsaPrivateKey("jwt/sign.key.pem");
            RSAPublicKey publicKey = readRsaPublicKey("jwt/sign.pub.pem");
            ALGORITHM = Algorithm.RSA256(publicKey, privateKey);
        }
        catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String createToken(String issuer) {
        var token = JWT
            .create()
            .withExpiresAt(Instant.now().plusSeconds(3600 * 24 * 365))
            .withIssuedAt(Instant.now())
            .withJWTId(UUID.randomUUID().toString())
            .withIssuer(issuer)
            .withAudience("account")
            .withSubject("subject")
            .withClaim("typ", "Bearer")
            .withClaim("allowed-origins", List.of("/*"))
            .sign(ALGORITHM);

        LOGGER.info("ISSUER: {}\n{}", issuer, token);
        return token;
    }

    private String createToken(String server, int port) {
        return createToken("%s:%d%s".formatted(server, port, REALM));
    }

    private static RSAPrivateKey readRsaPrivateKey(String resource)
        throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {

        var keyFactory = KeyFactory.getInstance("RSA");
        var keySpec = new PKCS8EncodedKeySpec(readPem(resource));
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    private static RSAPublicKey readRsaPublicKey(String resource)
        throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {

        var keyFactory = KeyFactory.getInstance("RSA");
        var keySpec = new X509EncodedKeySpec(readPem(resource));
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    private static byte[] readPem(String resource) throws IOException {
        var content = new ClassPathResource(resource).getContentAsString(UTF_8);
        var privateKeyPEM = content
            .replaceAll("-----(BEGIN|END) (PRIVATE|PUBLIC) KEY-----", "")
            .replaceAll(System.lineSeparator(), "");

        return BASE64_DECODER.decode(privateKeyPEM);
    }

    /**
     * Create test tokens.
     */
    public static void main(String... args) {
        var jwtTokenManager = new JwtTokenManager();
        Map.of(
            "http://openid.mock", 9876,
            "http://localhost", 9876
        )
        .forEach((endpoint, port) -> jwtTokenManager.createToken(endpoint, port));
    }
}
