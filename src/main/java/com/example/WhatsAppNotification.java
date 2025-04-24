package com.example;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

public class WhatsAppNotification implements Notifiable {
    private static final String API_BASE_URL = "https://graph.facebook.com/v17.0/";
    private static final String MESSAGING_PRODUCT = "whatsapp";
    
    private final String phoneNumberId;
    private final String accessToken;
    private final int timeoutMs;



    public WhatsAppNotification(String phoneNumberId, String accessToken) {
        this(phoneNumberId, accessToken, 5000);
    }



    public WhatsAppNotification(String phoneNumberId, String accessToken, int timeoutMs) {
        this.phoneNumberId = phoneNumberId;
        this.accessToken = accessToken;
        this.timeoutMs = timeoutMs;
    }


    
    @Override
    public void sendNotification(String recipient, String message) throws NotificationException {
        try (CloseableHttpClient httpClient = createHttpClient()) {
            String formattedRecipient = formatNumber(recipient);
            validateNumber(formattedRecipient);
            
            HttpPost httpPost = createRequest(formattedRecipient, message);
            
            int statusCode = httpClient.execute(httpPost)
                .getStatusLine()
                .getStatusCode();
            
            if (statusCode < 200 || statusCode >= 300) {
                throw new NotificationException("WhatsApp API returned error: HTTP " + statusCode);
            }
        } catch (Exception e) {
            throw new NotificationException("Failed to send WhatsApp message", e);
        }
    }

    private CloseableHttpClient createHttpClient() {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(timeoutMs)
            .setConnectionRequestTimeout(timeoutMs)
            .setSocketTimeout(timeoutMs)
            .build();
            
        return HttpClients.custom()
            .setDefaultRequestConfig(config)
            .build();
    }

    private HttpPost createRequest(String recipient, String message) throws Exception {
        JSONObject payload = new JSONObject();
        payload.put("messaging_product", MESSAGING_PRODUCT);
        payload.put("to", recipient);
        payload.put("type", "text");
        
        JSONObject text = new JSONObject();
        text.put("body", message);
        payload.put("text", text);

        HttpPost httpPost = new HttpPost(API_BASE_URL + phoneNumberId + "/messages");
        httpPost.setHeader("Authorization", "Bearer " + accessToken);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(payload.toString()));
        
        return httpPost;
    }

    private String formatNumber(String rawNumber) {
        String digits = rawNumber.replaceAll("[^0-9]", "");
        if (digits.startsWith("92") && digits.length() == 11) {
            return "+" + digits;
        } else if (digits.startsWith("3") && digits.length() == 10) {
            return "+92" + digits;
        }
        return rawNumber;
    }

    private void validateNumber(String number) throws NotificationException {
        if (!number.matches("^\\+[0-9]{10,14}$")) {
            throw new NotificationException("Invalid phone number format");
        }
    }
}