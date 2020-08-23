package com.mgrin.thau.configurations.jwt;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.annotation.PostConstruct;

import com.auth0.jwt.algorithms.Algorithm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

@Component
@ConditionalOnExpression(
    "${thau.jwt.token_lifetime:0} != 0 " +
    "and (" + 
        "(\"${thau.jwt.rsa.private_key:0}\" != \"0\" and \"${thau.jwt.rsa.public_key:0}\" != \"0\") " +
        "or " +
        "(\"${thau.jwt.hmac.secret:0}\" != \"0\") " +
    ")"
)
public class JWTConfiguration {

    @Value("${thau.jwt.hmac.secret:0}")
    private String hmacSecret;

    @Value("${thau.jwt.rsa.private_key:0}")
    private String privateKeyBase64;

    @Value("${thau.jwt.rsa.public_key:0}")
    private String publicKeyBase64;

    @Value("${thau.jwt.token_lifetime}")
    private long tokenLifetime;

    private String encryptionAlgorithm;
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    private Algorithm algorithm;

    @PostConstruct
    public void createEncryptionKeys() throws Exception {
        if (!privateKeyBase64.equals("0") && !publicKeyBase64.equals("0")) {
            String privateKeyValue = new String(Base64Utils.decodeFromString(privateKeyBase64));
            String publicKeyValue = new String(Base64Utils.decodeFromString(publicKeyBase64));

            privateKeyValue = privateKeyValue.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
            publicKeyValue = publicKeyValue.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");;
    
            KeyFactory kf = KeyFactory.getInstance("RSA");
            
            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64Utils.decodeFromString(privateKeyValue));
            privateKey = (RSAPrivateKey) kf.generatePrivate(keySpecPKCS8);

            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64Utils.decodeFromString(publicKeyValue));
            publicKey = (RSAPublicKey) kf.generatePublic(keySpecX509);

            algorithm = Algorithm.RSA512(publicKey, privateKey);
            encryptionAlgorithm = "RSA";
        } else if (!hmacSecret.equals("0")) {
            algorithm = Algorithm.HMAC512(hmacSecret);
            encryptionAlgorithm = "HMAC";
        }
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public long getTokenLifetime() {
        return tokenLifetime;
    }
}