package com.ai.image_description_app.service;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AIImageDescriptionService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.model:meta-llama/llama-4-scout-17b-16e-instruct}")
    private String model;

    @Value("${groq.base-url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    public String generateDescription(File imageFile) {
        if (apiKey == null || apiKey.isBlank()) {
            return "AI description generation failed: Groq API key not set";
        }

        try {
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String contentType = Files.probeContentType(imageFile.toPath());
            if (contentType == null || contentType.isBlank()) {
                contentType = "image/jpeg";
            }
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String dataUrl = "data:" + contentType + ";base64," + base64;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            String url = baseUrl + "/responses";
            Map<String, Object> payload = Map.of(
                "model", model,
                "input", List.of(
                    Map.of(
                        "role", "user",
                        "content", List.of(
                            Map.of("type", "input_text", "text", "Describe this image in one short paragraph."),
                            Map.of("type", "input_image", "image_url", dataUrl, "detail", "auto")
                        )
                    )
                ),
                "max_output_tokens", 200
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            String body = response.getBody();
            if (body == null || body.isBlank()) {
                return "AI description generation failed: Empty response";
            }

            ObjectMapper mapper = new ObjectMapper();
            try {
                Map<?, ?> responseMap = mapper.readValue(body, Map.class);

                Object outputText = responseMap.get("output_text");
                if (outputText != null && !outputText.toString().isBlank()) {
                    return outputText.toString().trim();
                }

                Object output = responseMap.get("output");
                if (output instanceof List) {
                    StringBuilder combined = new StringBuilder();
                    for (Object item : (List<?>) output) {
                        if (!(item instanceof Map)) {
                            continue;
                        }
                        Map<?, ?> itemMap = (Map<?, ?>) item;
                        if (!"message".equals(String.valueOf(itemMap.get("type")))) {
                            continue;
                        }
                        Object content = itemMap.get("content");
                        if (content instanceof List) {
                            for (Object part : (List<?>) content) {
                                if (!(part instanceof Map)) {
                                    continue;
                                }
                                Map<?, ?> partMap = (Map<?, ?>) part;
                                if ("output_text".equals(String.valueOf(partMap.get("type")))) {
                                    Object text = partMap.get("text");
                                    if (text != null && !text.toString().isBlank()) {
                                        if (combined.length() > 0) {
                                            combined.append("\n");
                                        }
                                        combined.append(text.toString().trim());
                                    }
                                }
                            }
                        }
                    }
                    if (combined.length() > 0) {
                        return combined.toString();
                    }
                }
            } catch (Exception ex) {
                // If not JSON, return raw body
            }

            return body;
        } catch (RestClientException e) {
            return "AI description generation failed: API error - " + e.getMessage();
        } catch (Exception e) {
            return "AI description generation failed: " + e.getMessage();
        }
    }
}
