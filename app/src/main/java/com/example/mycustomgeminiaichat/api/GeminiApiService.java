package com.example.mycustomgeminiaichat.api;

import com.example.mycustomgeminiaichat.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.*;

public class GeminiApiService {

    // TODO: Replace with your actual Gemini API key
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";
    private final OkHttpClient client;
    private final Gson gson;

    public GeminiApiService() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public void generateContent(String model, String prompt, ApiCallback callback) {
        String url = BASE_URL + model + ":generateContent?key=" + API_KEY;

        // Create request body
        GeminiRequest request = new GeminiRequest();
        request.contents = new ArrayList<>();

        Content content = new Content();
        content.parts = new ArrayList<>();

        Part part = new Part();
        part.text = prompt;
        content.parts.add(part);

        request.contents.add(content);

        String jsonBody = gson.toJson(request);

        RequestBody body = RequestBody.create(
            jsonBody,
            MediaType.get("application/json; charset=utf-8")
        );

        Request httpRequest = new Request.Builder()
            .url(url)
            .post(body)
            .build();

        client.newCall(httpRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        GeminiResponse geminiResponse = gson.fromJson(responseBody, GeminiResponse.class);
                        if (geminiResponse.candidates != null &&
                            !geminiResponse.candidates.isEmpty() &&
                            geminiResponse.candidates.get(0).content != null &&
                            geminiResponse.candidates.get(0).content.parts != null &&
                            !geminiResponse.candidates.get(0).content.parts.isEmpty()) {

                            String text = geminiResponse.candidates.get(0).content.parts.get(0).text;
                            callback.onSuccess(text);
                        } else {
                            callback.onError("No response text found");
                        }
                    } catch (Exception e) {
                        callback.onError("Error parsing response: " + e.getMessage());
                    }
                } else {
                    callback.onError("API error: " + response.code() + " " + response.message());
                }
                response.close();
            }
        });
    }

    // Data classes for JSON serialization
    private static class GeminiRequest {
        @SerializedName("contents")
        List<Content> contents;
    }

    private static class Content {
        @SerializedName("parts")
        List<Part> parts;
    }

    private static class Part {
        @SerializedName("text")
        String text;
    }

    private static class GeminiResponse {
        @SerializedName("candidates")
        List<Candidate> candidates;
    }

    private static class Candidate {
        @SerializedName("content")
        Content content;
    }
}
