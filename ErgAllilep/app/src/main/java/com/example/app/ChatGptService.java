package com.example.app;

import okhttp3.*;
import com.google.gson.*;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class ChatGptService {

    private static final String API_KEY = "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"; // <-- Βεβαιώσου ότι το API key είναι έγκυρο
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_MODEL = "meta-llama/llama-4-maverick:free";


    public interface ChatGptCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public static void sendMessageToGpt(List<JsonObject> history, ChatGptCallback callback) {
        OkHttpClient client = new OkHttpClient();

        JsonArray messages = new JsonArray();
        messages.add(createSystemMessage());

        // Add user and assistant messages from the history
        history.forEach(messages::add);

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("model", API_MODEL);
        jsonBody.add("messages", messages);
        jsonBody.addProperty("max_tokens", 500);

        RequestBody body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
        Request request = createRequest(body);

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                handleResponse(response, callback);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Connection failure: " + e.getMessage());
            }
        });
    }

    private static JsonObject createSystemMessage() {
        JsonObject systemMsg = new JsonObject();
        LocalDate today = LocalDate.now();
        LocalDate playDate = today.plusDays(2);  // 2 μέρες μετά από σήμερα
        String displayDate = playDate.format(DateTimeFormatter.ofPattern("dd/MM"));
        String isoDate = playDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // για JSON

        String play1 = "Άμλετ";
        String play2 = "Αντιγόνη";

        String schedule = "Πρόγραμμα παραστάσεων – " + displayDate + ":\n\n" +
                "• " + play1 + " — Αίθουσα 1: 18:00, 21:00\n" +
                "• " + play2 + " — Αίθουσα 2: 17:30, 20:30";


        String prompt =
                "Απαντάς στα ελληνικά ως ένας φιλικός AI υπάλληλος ταμείου του θεάτρου Theatron. Απαντάς στον Α' Ενικό." +
                "Όταν απαντάς, δίνεις πάντα μία ολοκληρωμένη και σαφή απάντηση σε ένα και ΜΌΝΟ ενα μήνυμα." +
                "Το θέατρο διαθέτει δύο αίθουσες. Σε κάθε αίθουσα παίζεται ένα συγκεκριμένο έργο, σε δύο παραστάσεις: απογευματινή και βραδινή.\n" +

                "Αν ο χρήστης ζητήσει το πρόγραμμα για τις παραστάσεις παρουσιάζεις ακριβώς το εξής πρόγραμμα και μετά προσθέτεις την τιμή των εισιτηρίων (15€):\n\n" +
                schedule + "\n\n" +
                "Τιμή εισιτηρίου: 15€\n\n" +

                "Αν ζητήσει κράτηση, απαντάς ΜΟΝΟ με JSON: " +
                "{\"intent\":\"book_ticket\", \"play\":\"Άμλετ\", \"date\":\"" + isoDate + "\", \"time\":\"18:00\"}. " +

                "Αν ζητήσει ακύρωση κράτησης: " +
                "{\"intent\":\"cancel_ticket\", \"play\":\"Άμλετ\", \"date\":\"" + isoDate + "\", \"time\":\"18:00\"}. " +

                "Για προβολή κρατήσεων: {\"intent\":\"view_bookings\"}. " +
                "Αν λείπουν στοιχεία, άφησέ τα κενά: \"date\":\"\". " +

                "Αν ζητήσει τηλέφωνο επικοινωνίας: {\"intent\":\"phone\"}. " +

                "Αν ζητήσει email επικοινωνίας: {\"intent\":\"email\"}. " +

                "Αν ζητήσει τοποθεσία θεάτρου: {\"intent\":\"location\"}. " +

                "Αν ζητήσει τρόπους επικοινωνίας γενικά: {\"intent\":\"contact\"}. " +

                "Για οτιδήποτε άλλο, ενημερώνεις πως δεν κατάλαβες τι σου ζητάει και επισημαίνεις πως μπορείς να τον βοηθήσεις ΜΌΝΟ με τις προαναφερόμενες λειτουργίες";

        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", prompt);
        return systemMsg;
    }

    private static Request createRequest(RequestBody body) {
        return new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
    }

    private static void handleResponse(Response response, ChatGptCallback callback) {
        try {
            String responseBody = response.body() != null ? response.body().string() : "{}";
            if (!response.isSuccessful()) {
                callback.onError("API Error: " + response.code() + "\n" + responseBody);
                return;
            }

            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            if (!json.has("choices")) {
                callback.onError("Invalid response: " + responseBody);
                return;
            }

            String reply = json.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            callback.onResponse(reply);
        } catch (Exception e) {
            callback.onError("Response Processing Error: " + e.getMessage());
        }
    }

    public static String extractIntentFromContent(String content) {
        try {
            int start = content.indexOf("{");
            int end = content.lastIndexOf("}") + 1;
            if (start == -1 || end == -1 || end <= start) return null;

            String jsonPart = content.substring(start, end).trim();
            JsonObject obj = JsonParser.parseString(jsonPart).getAsJsonObject();

            if (obj.has("intent")) {
                return obj.get("intent").getAsString();
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }

    public static String extractJsonBlock(String content) {
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}") + 1;
        if (start != -1 && end != -1 && end > start) {
            return content.substring(start, end).trim();
        }
        return "{}";
    }
}