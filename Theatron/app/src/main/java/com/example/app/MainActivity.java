package com.example.app;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private ListView chatListView;
    private List<JsonObject> conversationHistory = new ArrayList<>();

    private EditText userInputEditText;
    private Button sendButton;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private Map<String, List<String>> availableSeatsMap;
    private JsonObject pendingBooking;

    private String pendingEmail = null;
    private String pendingSeat = null;
    private String tempCardNumber;
    private boolean showCancelIcon = false;


    private boolean awaitingCardNumber = false;
    private boolean awaitingPaymentMethod = false;

    private boolean awaitingCancelEmail = false;
    private JsonObject pendingCancelData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatListView = findViewById(R.id.chatListView);
        userInputEditText = findViewById(R.id.editTextMessage);
        sendButton = findViewById(R.id.buttonSend);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messageList);
        chatListView.setAdapter(chatAdapter);

        availableSeatsMap = new HashMap<>();
        addMessage("Καλωσόρισες στο Theatron! Είμαι ο Shakespeare, το AI Chatbot του θεάτρου. " +
                "Μπορώ να σε βοηθήσω με τα εξής: " + "\n\nΠρόγραμμα, Κράτηση Θέσης, Ακύρωση Κράτησης, Προβολή Κρατήσεων, Επικοινωνία", false);


        sendButton.setOnClickListener(v -> {
            String userMessage = userInputEditText.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                addMessage(userMessage, true);
                handleUserInput(userMessage);
                userInputEditText.setText("");
            }
        });
    }

    private void addMessage(String text, boolean isUser) {
        ChatMessage message = new ChatMessage(text, isUser);
        if (!isUser) {
            message.setShowCancelIcon(showCancelIcon);
        }
        messageList.add(message);
        chatAdapter.notifyDataSetChanged();
        chatListView.setSelection(messageList.size() - 1);
    }

    private void handleUserInput(String userInput) {
        userInput = removeAccents(userInput.trim().toLowerCase());

        //Εmail gia krathsh
        if (pendingBooking != null && pendingEmail == null) {
            if (userInput.matches(".+@.+\\..+")) {
                pendingEmail = userInput;
                showCancelIcon = true;
                confirmPendingBooking();
            } else {
                addMessage("Παρακαλώ δώσε ένα έγκυρο email για να συνεχίσουμε την κράτηση.", false);
            }
            return;
        }

        //synexeia h akyrwsh krathshs


        //methodos plhrwmhs
        if (awaitingPaymentMethod) {
            if (userInput.equals("1") || userInput.contains("ηλεκτρονικα")) {
                awaitingCardNumber = true;
                showCancelIcon = true;
                awaitingPaymentMethod = false;
                addMessage("Παρακαλώ δώσε τον αριθμό κάρτας και το PIN (π.χ. 12345 4321):", false);

            } else if (userInput.equals("2") || userInput.contains("ταμειο") || userInput.contains("θεατρο")) {
                TicketManager.bookSeat(
                        pendingBooking.get("play").getAsString(),
                        pendingBooking.get("date").getAsString(),
                        pendingBooking.get("time").getAsString(),
                        pendingSeat,
                        pendingEmail
                );
                showCancelIcon = false;
                addMessage("Η πληρωμή θα γίνει στο θέατρο. Παρουσιάστε στην είσοδο τον κωδικό κράτησης που σας ήρθε στο email.", false);

                // reset
                awaitingPaymentMethod = false;
                awaitingCardNumber = false;
                pendingBooking = null;
                pendingEmail = null;
                tempCardNumber = null;
            } else {
                addMessage("Παρακαλώ επιλέξτε έγκυρο τρόπο πληρωμής.", false);
            }
            return;
        }

        //hlektronikh plhrwmh
        if (awaitingCardNumber) {
            String[] parts = userInput.replace(",", " ").replace("/", " ").split("\\s+");

            if (parts.length == 2 && parts[0].matches("\\d{5}") && parts[1].matches("\\d{4}")) {
                tempCardNumber = parts[0];
                String pin = parts[1];
                TicketManager.bookSeat(
                        pendingBooking.get("play").getAsString(),
                        pendingBooking.get("date").getAsString(),
                        pendingBooking.get("time").getAsString(),
                        pendingSeat,
                        pendingEmail
                );
                addMessage("Η πληρωμή ολοκληρώθηκε επιτυχώς!" +
                        "\n\nΤα εισιτήρια θα σταλούν στο email:" + pendingEmail, false);

                // reset
                showCancelIcon = false;
                awaitingCardNumber = false;
                pendingBooking = null;
                pendingEmail = null;
                tempCardNumber = null;
            } else {
                addMessage("Μη έγκυρη μορφή. Παρακαλώ δώσε τον αριθμό και το PIN στη μορφή '12345 4321':", false);
            }
            return;
        }

        //email gia akyrwsh theshs
        if (awaitingCancelEmail) {
            showCancelIcon = true;
            if (!pendingCancelData.has("email")) {
                if (userInput.matches(".+@.+\\..+")) {
                    String email = userInput.trim();
                    pendingCancelData.addProperty("email", email);

                    List<String> bookings = TicketManager.getBookingsByEmail(email);
                    if (bookings == null || bookings.isEmpty()) {
                        addMessage("Δεν βρέθηκαν κρατήσεις για το email: " + email, false);
                        awaitingCancelEmail = false;
                        pendingCancelData = null;
                    } else {
                        StringBuilder sb = new StringBuilder("Οι κρατήσεις σου:\n");
                        for (int i = 0; i < bookings.size(); i++) {
                            sb.append((i + 1)).append(". ").append(bookings.get(i)).append("\n");
                        }
                        addMessage(sb.toString() + "\nΓράψε τον αριθμό της κράτησης που θέλεις να ακυρώσεις:", false);
                        pendingCancelData.add("bookings", new Gson().toJsonTree(bookings));
                    }
                } else {
                    addMessage("Δώσε έγκυρο email για την ακύρωση:", false);
                }
                return;
            }

            // Δεύτερο βήμα: ο χρήστης επιλέγει ποια κράτηση να ακυρώσει (βάσει αριθμού)
            JsonArray bookingsArray = pendingCancelData.getAsJsonArray("bookings");
            try {
                int choice = Integer.parseInt(userInput.trim()) - 1;
                if (choice >= 0 && choice < bookingsArray.size()) {
                    String selected = bookingsArray.get(choice).getAsString();
                    String[] parts = selected.split(","); // Αν είναι structured ως play,date,time,seat

                    String play = parts[0].trim();
                    String date = parts[1].trim();
                    String time = parts[2].trim();
                    String seat = parts[3].trim();
                    String email = pendingCancelData.get("email").getAsString();

                    boolean cancelled = TicketManager.cancelSeat(play, date, time, seat, email);
                    if (cancelled) {
                        showCancelIcon = false;
                        addMessage("Η κράτηση ακυρώθηκε επιτυχώς. Η θέση " + seat + " είναι ξανά διαθέσιμη.", false);
                    } else {
                        addMessage("Παρουσιάστηκε πρόβλημα με την ακύρωση. Προσπάθησε ξανά.", false);
                    }

                    // Reset state
                    awaitingCancelEmail = false;
                    pendingCancelData = null;
                } else {
                    addMessage("Παρακαλώ επίλεξε έναν έγκυρο αριθμό κράτησης.", false);
                }
            } catch (NumberFormatException e) {
                addMessage("Πληκτρολόγησε τον αριθμό της κράτησης (π.χ. 1).", false);
            }
            showCancelIcon = false;
            return;
        }
        sendToChatGpt(userInput);
    }

    private void confirmPendingBooking() {
        String play = pendingBooking.get("play").getAsString();
        String date = pendingBooking.get("date").getAsString();
        String time = pendingBooking.get("time").getAsString();

        List<String> allSeats = Arrays.asList(
                "A1", "A2", "A3", "A4", "A5", "A6",
                "B1", "B2", "B3", "B4", "B5", "B6",
                "C1", "C2", "C3", "C4", "C5", "C6",
                "D1", "D2", "D3", "D4", "D5", "D6",
                "E1", "E2", "E3", "E4", "E5", "E6"
        );

        SeatSelection.show(this, allSeats, play, date, time, selectedSeat -> {
            pendingSeat = selectedSeat;
            addMessage("Έχεις επιλέξει:" +
                    "\n\n" + play +
                    "\n" + date +
                    "\n" + time +
                    "\nΘέση " + selectedSeat +
                    "\nEmail κράτησησς: " + pendingEmail +
                    "\n\nΕπιλογή πληρωμής:" + "\n1. Ηλεκτρονικά " + "\n2. Στο ταμείο του Θεάτρου",false);
            awaitingPaymentMethod = true;
            showCancelIcon = true;
        });
    }




    private void sendToChatGpt(String userInput) {
        JsonObject userMsg = createMessage("user", userInput);
        conversationHistory.add(userMsg);

        ChatGptService.sendMessageToGpt(conversationHistory, new ChatGptService.ChatGptCallback() {
            @Override
            public void onResponse(String response) {
                runOnUiThread(() -> handleChatGptResponse(response));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> addMessage("❗ Σφάλμα: " + error, false));
            }
        });
    }

    private JsonObject createMessage(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        return message;
    }

    private void handleChatGptResponse(String response) {
        try {
            String jsonString = ChatGptService.extractJsonBlock(response);
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();

            JsonObject assistantMsg = createMessage("assistant", response);
            conversationHistory.add(assistantMsg);

            if (json.has("intent")) {
                processIntent(json.toString());
            } else {
                addMessage(response, false);  // <-- Χρησιμοποίησε την απάντηση ακόμα και χωρίς intent
            }

        } catch (Exception e) {
            addMessage(response, false);  // fallback αν αποτύχει η ανάλυση JSON
        }
    }


    private void processIntent(String response) {
        JsonObject json = JsonParser.parseString(response).getAsJsonObject();
        if (json.has("intent")) {
            String intent = json.get("intent").getAsString();
            switch (intent) {
                case "book_ticket":
                    handleBookTicketIntent(json);
                    break;
                case "view_bookings":
                    handleViewBookingsIntent();
                    break;
                case "cancel_ticket":
                    handleCancelTicketIntent(json);
                    break;
                case "phone":
                    addMessage("Επικοινωνήστε μαζί μας στο 210 1234 567.\n\n10:00 - 18:00 καθημερινά.", false);
                    break;
                case "email":
                    addMessage("Επικοινωνήστε μαζί μας στο support@theatron.gr.", false);
                    break;
                case "location":
                    addMessage("Το Theatron βρίσκεται στην οδό Βουλής 13, Αθήνα.", false);
                    break;
                case "contact":
                    addMessage("Διεύθυνση: Βουλής 13, Αθήνα\n\nEmail: info@theatron.gr\n\nΤηλέφωνο: 210 1234 567, 10:00–18:00", false);
                    break;
            }
        }
    }

    private void handleBookTicketIntent(JsonObject json) {
        if (json.has("play") && json.has("date") && json.has("time")) {
            String play = json.get("play").getAsString();
            String date = json.get("date").getAsString();
            String time = json.get("time").getAsString();

            if (!play.isEmpty() && !date.isEmpty() && !time.isEmpty()) {
                pendingBooking = json;
                pendingEmail = null;
                showCancelIcon = true;
                addMessage("Πριν συνεχίσουμε, παρακαλώ γράψε το email σου." +
                        "\n\nTip: Μπορείς να διακόψεις την διαδικασία συνναλαγής οποιαδήποτε στιγμή " +
                        "πατώντας το Χ κάτω δεξιά από το μήνυμά μου.", false);
            } else {
                addMessage("Για να σε βοηθήσω με την κράτηση, χρειάζομαι πληροφορίες όπως " +
                        "τίτλο παράστασης, ημερομηνία και ώρα." +
                        "\n\nΑν δεν γνωρίζεις το πρόγραμμα, γράψε «Πρόγραμμα» και θα σου το εμφανίσω.", false);
            }
        }
    }

    private void handleViewBookingsIntent() {
        Set<String> bookings = TicketManager.getAllBookings();
        addMessage(bookings.isEmpty() ? "Δεν υπάρχουν κρατήσεις." : "Οι κρατήσεις ανά email:\n" + String.join("\n", bookings), false);
    }

    private void handleCancelTicketIntent(JsonObject json) {
        // Δημιουργούμε κενό αντικείμενο εάν δεν υπάρχουν όλα τα στοιχεία
        pendingCancelData = new JsonObject();

        // Αν έχουν δοθεί, τα αποθηκεύουμε προσωρινά
        if (json.has("play")) {
            pendingCancelData.addProperty("play", json.get("play").getAsString());
        }
        if (json.has("date")) {
            pendingCancelData.addProperty("date", json.get("date").getAsString());
        }
        if (json.has("time")) {
            pendingCancelData.addProperty("time", json.get("time").getAsString());
        }
        if (json.has("seat")) {
            pendingCancelData.addProperty("seat", json.get("seat").getAsString());
        }

        // Ορίζουμε ότι είμαστε σε ακύρωση
        awaitingCancelEmail = true;
        showCancelIcon = true;

        // Ζητάμε email
        addMessage("Παρακαλώ γράψε το email που χρησιμοποίησες για την κράτηση:", false);
    }

    void cancelBookingFlow() {
        boolean isBookingFlow = pendingBooking != null || awaitingPaymentMethod || awaitingCardNumber;
        boolean isCancelFlow = awaitingCancelEmail || (pendingCancelData != null && pendingCancelData.has("email"));
        pendingBooking = null;
        pendingEmail = null;
        pendingSeat = null;
        tempCardNumber = null;
        pendingCancelData = null;

        awaitingPaymentMethod = false;
        awaitingCardNumber = false;
        awaitingCancelEmail = false;

        showCancelIcon = false;

        if (isBookingFlow) {
            addMessage("Η διαδικασία κράτησης διακόπηκε επιτυχώς.", false);
        } else if (isCancelFlow) {
            addMessage("Η διαδικασία ακύρωσης κράτησης διακόπηκε επιτυχώς.", false);
        }
    }

    public void requestCancelFromAdapter() {
        cancelBookingFlow();
    }

    private String removeAccents(String input) {
        return java.text.Normalizer
                .normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
