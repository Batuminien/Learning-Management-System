package com.lsm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.private-key-id}")
    private String privateKeyId;

    @Value("${firebase.private-key}")
    private String privateKey;

    @Value("${firebase.client-email}")
    private String clientEmail;

    @Value("${firebase.client-id}")
    private String clientId;

    @Value("${firebase.auth-uri}")
    private String authUri;

    @Value("${firebase.token-uri}")
    private String tokenUri;

    @Value("${firebase.auth-provider-cert-url}")
    private String authProviderCertUrl;

    @Value("${firebase.client-cert-url}")
    private String clientCertUrl;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // Clean up private key - replace literal \n with real line breaks
        String cleanedPrivateKey = privateKey
                .replace("\\n", "\n")
                .replace("\"", "");

        // Create JSON-style credentials
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "service_account");
        credentials.put("project_id", projectId);
        credentials.put("private_key_id", privateKeyId);
        credentials.put("private_key", cleanedPrivateKey);
        credentials.put("client_email", clientEmail);
        credentials.put("client_id", clientId);
        credentials.put("auth_uri", authUri);
        credentials.put("token_uri", tokenUri);
        credentials.put("auth_provider_x509_cert_url", authProviderCertUrl);
        credentials.put("client_x509_cert_url", clientCertUrl);

        // Convert to JSON string
        ObjectMapper mapper = new ObjectMapper();
        String credentialsJson = mapper.writeValueAsString(credentials);

        // Create credentials from JSON string
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ByteArrayInputStream(credentialsJson.getBytes()));

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();

        // Check if default app exists
        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }
}