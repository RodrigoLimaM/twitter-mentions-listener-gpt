package br.com.twitter.mentions.listener.gpt.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class OAuth1HeaderGenerator {

    private final String consumerKey;
    private final String consumerSecret;
    private final String signatureMethod;
    private final String accessToken;
    private final String tokenSecret;
    private final String version;

    public OAuth1HeaderGenerator(final String consumerKey, final String consumerSecret, final String accessToken, final String tokenSecret) {
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.accessToken = accessToken;
        this.tokenSecret = tokenSecret;
        this.signatureMethod = "HMAC-SHA1";
        this.version = "1.0";
    }

    private static final String OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    private static final String OAUTH_TOKEN = "oauth_token";
    private static final String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
    private static final String OAUTH_TIMESTAMP = "oauth_timestamp";
    private static final String OAUTH_NONCE = "oauth_nonce";
    private static final String OAUTH_VERSION = "oauth_version";
    private static final String OAUTH_SIGNATURE = "oauth_signature";
    private static final String HMAC_SHA_1 = "HmacSHA1";

    public String generateHeader(final String httpMethod, final String url, final Map<String, String> requestParams) {
        final var base = new StringBuilder();
        final var nonce = getNonce();
        final var timestamp = getTimestamp();
        final var baseSignatureString = generateSignatureBaseString(httpMethod, url, requestParams, nonce, timestamp);
        final var signature = encryptUsingHmacSHA1(baseSignatureString);
        base.append("OAuth ");
        append(base, OAUTH_CONSUMER_KEY, consumerKey);
        append(base, OAUTH_TOKEN, accessToken);
        append(base, OAUTH_SIGNATURE_METHOD, signatureMethod);
        append(base, OAUTH_TIMESTAMP, timestamp);
        append(base, OAUTH_NONCE, nonce);
        append(base, OAUTH_VERSION, version);
        append(base, OAUTH_SIGNATURE, signature);
        base.deleteCharAt(base.length() - 1);
        System.out.println("header : " + base);
        return base.toString();
    }

    private String generateSignatureBaseString(final String httpMethod, final String url, final Map<String, String> requestParams, final String nonce, final String timestamp) {
        Map<String, String> params = new HashMap<>();
        if (requestParams != null) {
            requestParams.forEach((key, value) -> put(params, key, value));
        }
        put(params, OAUTH_CONSUMER_KEY, consumerKey);
        put(params, OAUTH_NONCE, nonce);
        put(params, OAUTH_SIGNATURE_METHOD, signatureMethod);
        put(params, OAUTH_TIMESTAMP, timestamp);
        put(params, OAUTH_TOKEN, accessToken);
        put(params, OAUTH_VERSION, version);
        final var sortedParams = params.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        final var base = new StringBuilder();
        sortedParams.forEach((key, value) -> base.append(key).append("=").append(value).append("&"));
        base.deleteCharAt(base.length() - 1);
        return httpMethod.toUpperCase() + "&" + encode(url) + "&" + encode(base.toString());
    }

    private String encryptUsingHmacSHA1(final String input) {
        final var secret = encode(consumerSecret) + "&" + encode(tokenSecret);
        final var keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        final var key = new SecretKeySpec(keyBytes, HMAC_SHA_1);
        Mac mac;
        try {
            mac = Mac.getInstance(HMAC_SHA_1);
            mac.init(key);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
        final var signatureBytes = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getEncoder().encode(signatureBytes));
    }

    private String encode(final String value) {
        String encoded = "";
        try {
            encoded = URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final var sb = new StringBuilder();
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            if (focus == '*') {
                sb.append("%2A");
            } else if (focus == '+') {
                sb.append("%20");
            } else if (focus == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
                sb.append('~');
                i += 2;
            } else {
                sb.append(focus);
            }
        }
        return sb.toString();
    }

    private void put(final Map<String, String> map, final String key, final String value) {
        map.put(encode(key), encode(value));
    }

    private void append(final StringBuilder builder, final String key, final String value) {
        builder.append(encode(key)).append("=\"").append(encode(value)).append("\",");
    }

    private String getNonce() {
        final var leftLimit = 48; // numeral '0'
        final var rightLimit = 122; // letter 'z'
        final var targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1).filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();

    }

    private String getTimestamp() {
        return String.valueOf(Math.round((new Date()).getTime() / 1000.0));
    }

}
