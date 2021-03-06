package io.smallrye.jwt.build.impl;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.eclipse.microprofile.jwt.Claims;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.keys.X509Util;

import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import io.smallrye.jwt.build.JwtEncryptionBuilder;
import io.smallrye.jwt.build.JwtSignatureBuilder;
import io.smallrye.jwt.build.JwtSignatureException;

/**
 * Default JWT Claims Builder
 *
 */
class JwtClaimsBuilderImpl extends JwtSignatureImpl implements JwtClaimsBuilder, JwtSignatureBuilder {

    JwtClaimsBuilderImpl() {

    }

    JwtClaimsBuilderImpl(String jsonLocation) {
        super(parseJsonToClaims(jsonLocation));
    }

    JwtClaimsBuilderImpl(Map<String, Object> claimsMap) {
        super(fromMapToJwtClaims(claimsMap));
    }

    private static JwtClaims fromMapToJwtClaims(Map<String, Object> claimsMap) {
        JwtClaims claims = new JwtClaims();
        @SuppressWarnings("unchecked")
        Map<String, Object> newMap = (Map<String, Object>) prepareValue(claimsMap);
        for (Map.Entry<String, Object> entry : newMap.entrySet()) {
            claims.setClaim(entry.getKey(), entry.getValue());
        }
        return claims;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaimsBuilder claim(String name, Object value) {
        claims.setClaim(name, prepareValue(value));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaimsBuilder issuer(String issuer) {
        claims.setIssuer(issuer);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaimsBuilder audience(String audience) {
        return audience(Collections.singleton(audience));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaimsBuilder audience(Set<String> audiences) {
        claims.setAudience(audiences.stream().collect(Collectors.toList()));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaimsBuilder subject(String subject) {
        claims.setSubject(subject);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaimsBuilder upn(String upn) {
        claims.setClaim(Claims.upn.name(), upn);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaimsBuilder preferredUserName(String preferredUserName) {
        claims.setClaim(Claims.preferred_username.name(), preferredUserName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaimsBuilder issuedAt(long issuedAt) {
        claims.setIssuedAt(NumericDate.fromSeconds(issuedAt));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaimsBuilder expiresAt(long expiredAt) {
        claims.setExpirationTime(NumericDate.fromSeconds(expiredAt));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaimsBuilder groups(String group) {
        return groups(Collections.singleton(group));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtClaimsBuilder groups(Set<String> groups) {
        claims.setClaim("groups", groups.stream().collect(Collectors.toList()));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtSignatureBuilder jws() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtSignatureBuilder header(String name, Object value) {
        if ("alg".equals(name)) {
            return algorithm(toSignatureAlgorithm((String) value));
        } else {
            headers.put(name, value);
            return this;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtSignatureBuilder algorithm(SignatureAlgorithm algorithm) {
        headers.put("alg", algorithm.name());
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtSignatureBuilder keyId(String keyId) {
        headers.put("kid", keyId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtSignatureBuilder thumbprint(X509Certificate cert) {
        headers.put("x5t", X509Util.x5t(cert));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtSignatureBuilder thumbprintS256(X509Certificate cert) {
        headers.put("x5t#S256", X509Util.x5tS256(cert));
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtEncryptionBuilder innerSign(PrivateKey signingKey) throws JwtSignatureException {
        return super.innerSign(signingKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtEncryptionBuilder innerSign(SecretKey signingKey) throws JwtSignatureException {
        return super.innerSign(signingKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtEncryptionBuilder innerSign() throws JwtSignatureException {
        return super.innerSign();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String json() {
        JwtBuildUtils.setDefaultJwtClaims(claims);
        return claims.toJson();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JwtEncryptionBuilder jwe() {
        JwtBuildUtils.setDefaultJwtClaims(claims);
        return new JwtEncryptionImpl(claims.toJson());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object prepareValue(Object value) {
        if (value instanceof Collection) {
            return ((Collection) value).stream().map(o -> prepareValue(o)).collect(Collectors.toList());
        }

        if (value instanceof Map) {
            Map<String, Object> map = (Map) value;
            Map<String, Object> newMap = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                newMap.put(entry.getKey(), prepareValue(entry.getValue()));
            }
            return newMap;
        }

        if (value instanceof JsonValue) {
            return convertJsonValue((JsonValue) value);
        }

        if (value instanceof Number || value instanceof Boolean) {
            return value;
        }

        return value.toString();
    }

    private static Object convertJsonValue(JsonValue jsonValue) {
        if (jsonValue instanceof JsonString) {
            String jsonString = jsonValue.toString();
            return jsonString.toString().substring(1, jsonString.length() - 1);
        } else if (jsonValue instanceof JsonNumber) {
            JsonNumber jsonNumber = (JsonNumber) jsonValue;
            if (jsonNumber.isIntegral()) {
                return jsonNumber.longValue();
            } else {
                return jsonNumber.doubleValue();
            }
        } else if (jsonValue == JsonValue.TRUE) {
            return true;
        } else if (jsonValue == JsonValue.FALSE) {
            return false;
        } else {
            return null;
        }
    }

    private static JwtClaims parseJsonToClaims(String jsonLocation) {
        return JwtBuildUtils.parseJwtClaims(jsonLocation);
    }

    private static SignatureAlgorithm toSignatureAlgorithm(String value) {
        try {
            return SignatureAlgorithm.fromAlgorithm(value);
        } catch (Exception ex) {
            throw ImplMessages.msg.unsupportedSignatureAlgorithm(value, ex);
        }
    }
}
