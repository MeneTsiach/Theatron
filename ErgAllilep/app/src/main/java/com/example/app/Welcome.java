package com.example.app;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Welcome extends AppCompatActivity {


    private String selectedSeatId = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_activity);

        MaterialButton startChatButton = findViewById(R.id.startChatButton);
        MaterialButton btnBook = findViewById(R.id.btnBook);
        MaterialButton btnCancel = findViewById(R.id.btnCancel);
        MaterialButton btnView = findViewById(R.id.btnView);
        MaterialButton btnContact = findViewById(R.id.btnContact);
        ImageView toggleViewIcon = findViewById(R.id.toggleViewIcon);
        LinearLayout extraButtonsContainer = findViewById(R.id.extraButtonsContainer);

        toggleViewIcon.setRotation(0f); // αρχική θέση

        toggleViewIcon.setOnClickListener(v -> {
            boolean isChatVisible = startChatButton.getVisibility() == View.VISIBLE;

            // Περιστροφή του εικονιδίου toggle
            float start = isChatVisible ? 0f : 180f;
            float end = isChatVisible ? 180f : 0f;
            ObjectAnimator.ofFloat(toggleViewIcon, "rotation", start, end)
                    .setDuration(300)
                    .start();

            if (isChatVisible) {
                // Απόκρυψη κουμπιού "Shakespeare AI"
                startChatButton.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> startChatButton.setVisibility(View.GONE))
                        .start();

                // Εμφάνιση των υπόλοιπων κουμπιών
                extraButtonsContainer.setAlpha(0f);
                extraButtonsContainer.setVisibility(View.VISIBLE);
                extraButtonsContainer.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start();

            } else {
                // Απόκρυψη των επιπλέον κουμπιών
                extraButtonsContainer.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> extraButtonsContainer.setVisibility(View.GONE))
                        .start();

                // Εμφάνιση του κουμπιού "Shakespeare AI"
                startChatButton.setAlpha(0f);
                startChatButton.setVisibility(View.VISIBLE);
                startChatButton.animate()
                        .alpha(1f)
                        .setDuration(300)
                        .start();

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toggleViewIcon.getLayoutParams();
                params.addRule(RelativeLayout.BELOW, R.id.switchableContainer);
                toggleViewIcon.setLayoutParams(params);
            }
        });



        startChatButton.setOnClickListener(v -> {
            Intent intent = new Intent(Welcome.this, MainActivity.class);
            startActivity(intent);
        });

        btnBook.setOnClickListener(v -> showBookingDialog());
        btnCancel.setOnClickListener(v -> showCancelDialog());
        btnView.setOnClickListener(v -> showAllBookingsDialog());
        btnContact.setOnClickListener(v -> showContactDialog());
    }


    // 💬 Αυτή είναι η μέθοδος που ανοίγει το popup κράτησης
    private void showBookingDialog() {
        // 🔹 Τοπικό πρόγραμμα
        Map<String, Map<String, List<String>>> schedule = new LinkedHashMap<>();

        String play1 = "Άμλετ | Αίθουσα 1";
        String play2 = "Αντιγόνη | Αίθουσα 2";
        String date1 = LocalDate.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        schedule.put(play1, new LinkedHashMap<>() {{
            put(date1, Arrays.asList("18:00", "21:00"));
        }});
        schedule.put(play2, new LinkedHashMap<>() {{
            put(date1, Arrays.asList("17:30", "20:30"));
        }});

        // 🔹 Φόρτωση Layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.book_ticket, null);

        MaterialAutoCompleteTextView inputPlay = dialogView.findViewById(R.id.inputPlay);
        MaterialAutoCompleteTextView inputDate = dialogView.findViewById(R.id.inputDate);
        MaterialAutoCompleteTextView inputTime = dialogView.findViewById(R.id.inputTime);
        EditText inputEmail = dialogView.findViewById(R.id.inputEmail);
        Button btnSelectSeat = dialogView.findViewById(R.id.btnSelectSeat);
        RadioGroup paymentOptions = dialogView.findViewById(R.id.paymentOptions);
        Button buttonCancelSeat = dialogView.findViewById(R.id.buttonCancelSeat);
        Button buttonConfirmSeat = dialogView.findViewById(R.id.buttonConfirmSeat);

        TextInputLayout cardInputLayout = dialogView.findViewById(R.id.cardInputLayout);
        EditText cardInput = dialogView.findViewById(R.id.cardInput);


        paymentOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioOnline) {
                cardInputLayout.setVisibility(View.VISIBLE);
            } else {
                cardInputLayout.setVisibility(View.GONE);
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // 🔹 Dropdown: Παράσταση
        List<String> playTitles = new ArrayList<>(schedule.keySet());
        ArrayAdapter<String> playAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, playTitles);
        inputPlay.setAdapter(playAdapter);

        inputPlay.setOnItemClickListener((parent, view, position, id) -> {
            String selectedPlay = playTitles.get(position);
            Map<String, List<String>> dateMap = schedule.get(selectedPlay);

            List<String> dates = new ArrayList<>(dateMap.keySet());
            ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, dates);
            inputDate.setAdapter(dateAdapter);
            inputDate.setText("");
            inputTime.setText("");
            inputTime.setAdapter(null);

            inputDate.setOnItemClickListener((parent1, view1, pos, id1) -> {
                String selectedDate = dates.get(pos);
                List<String> times = dateMap.get(selectedDate);
                ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, times);
                inputTime.setAdapter(timeAdapter);
            });
        });

        // 🔹 Επιλογή Θέσης
        btnSelectSeat.setOnClickListener(v -> {
            List<String> allSeats = Arrays.asList(
                    "A1", "A2", "A3", "A4", "A5", "A6",
                    "B1", "B2", "B3", "B4", "B5", "B6",
                    "C1", "C2", "C3", "C4", "C5", "C6"
            );

            String play = inputPlay.getText().toString();
            String date = inputDate.getText().toString();
            String time = inputTime.getText().toString();

            SeatSelection.show(Welcome.this, allSeats, play, date, time, selectedSeatId, selected -> {
                selectedSeatId = selected;
                btnSelectSeat.setText("Θέση: " + selected);
            });
        });

        // 🔹 Επιβεβαίωση
        buttonConfirmSeat.setOnClickListener(v -> {
            String play = inputPlay.getText().toString().trim();
            String date = inputDate.getText().toString().trim();
            String time = inputTime.getText().toString().trim();
            String email = inputEmail.getText().toString().trim();
            String seat = selectedSeatId;
            String cardData = cardInput.getText().toString().trim();

            int selectedPaymentId = paymentOptions.getCheckedRadioButtonId();
            String paymentMethod = (selectedPaymentId == R.id.radioOnline) ? "Ηλεκτρονική πληρωμή" :
                    (selectedPaymentId == R.id.radioCash) ? "Πληρωμή στο ταμείο" : "";

            // 🔍 Έλεγχος πεδίων
            if (play.isEmpty() || date.isEmpty() || time.isEmpty() || seat == null ||
                    !email.matches(".+@.+\\..+") || paymentMethod.isEmpty()) {
                Toast.makeText(this, "Συμπλήρωσε όλα τα πεδία και επίλεξε θέση και πληρωμή.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (paymentMethod.equals("Ηλεκτρονική πληρωμή")) {
                if (!cardData.matches("\\d{5}\\s\\d{4}")) {
                    Toast.makeText(this, "Δώσε σωστό αριθμό κάρτας και PIN (π.χ. 12345 4321)", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // ✅ Απόπειρα κράτησης
            boolean success = TicketManager.bookSeat(play, date, time, seat, email);
            if (success) {
                String message = "Η κράτηση ολοκληρώθηκε!\nΘέση: " + seat + "\nΠληρωμή: " + paymentMethod;
                if (paymentMethod.equals("Ηλεκτρονική πληρωμή")) {
                    message += "\n\nΤα στοιχεία πληρωμής καταχωρήθηκαν.";
                }
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                selectedSeatId = null;
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Η θέση ενδέχεται να είναι ήδη δεσμευμένη.", Toast.LENGTH_LONG).show();
            }
        });

        // 🔹 Ακύρωση
        buttonCancelSeat.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showCancelDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.cancel_booking, null);
        EditText inputCancelEmail = dialogView.findViewById(R.id.inputCancelEmail);
        ListView bookingList = dialogView.findViewById(R.id.bookingList);
        Button buttonFind = dialogView.findViewById(R.id.buttonFindBookings);
        Button cancelButton = dialogView.findViewById(R.id.buttonCancel);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Πατάει "Ακύρωση"
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Πατάει "Αναζήτηση"
        buttonFind.setOnClickListener(v -> {
            String email = inputCancelEmail.getText().toString().trim();
            if (!email.matches(".+@.+\\..+")) {
                Toast.makeText(this, "Δώσε έγκυρο email.", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> bookings = TicketManager.getBookingsByEmail(email);
            if (bookings.isEmpty()) {
                Toast.makeText(this, "Δεν βρέθηκαν κρατήσεις.", Toast.LENGTH_SHORT).show();
                bookingList.setVisibility(View.GONE);
                return;
            }

            bookingList.setVisibility(View.VISIBLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.booking_list, bookings);
            bookingList.setAdapter(adapter);

            bookingList.setOnItemClickListener((parent, view, position, id1) -> {
                String selected = bookings.get(position);
                String[] parts = selected.split(",");

                boolean cancelled = TicketManager.cancelSeat(
                        parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), email
                );

                if (cancelled) {
                    Toast.makeText(this, "Η κράτηση ακυρώθηκε επιτυχώς!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(this, "Σφάλμα κατά την ακύρωση.", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showAllBookingsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.booking_view, null);

        ListView allBookingsList = dialogView.findViewById(R.id.allBookingsList);
        Button buttonCloseAll = dialogView.findViewById(R.id.buttonCloseAll);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        Set<String> allBookings = TicketManager.getAllBookings();
        Map<String, List<String>> grouped = new LinkedHashMap<>();

        for (String entry : allBookings) {
            String[] lines = entry.split("\n", 2);
            String email = lines[0];
            String bookingInfo = lines.length > 1 ? lines[1] : "";

            grouped.computeIfAbsent(email, k -> new ArrayList<>()).add(bookingInfo);
        }

        List<String> formatted = new ArrayList<>();
        if(allBookings.isEmpty()) {
            Toast.makeText(this, "Δεν υπάρχουν κρατήσεις.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
                StringBuilder sb = new StringBuilder("• ").append(entry.getKey());
                for (String details : entry.getValue()) {
                    sb.append("\n").append("    ").append(details);
                }
                formatted.add(sb.toString());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.booking_view_item,
                formatted
        );
        allBookingsList.setAdapter(adapter);

        buttonCloseAll.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showContactDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.contact_view, null);

        TextView phone = dialogView.findViewById(R.id.contactPhone);
        TextView email = dialogView.findViewById(R.id.contactEmail);
        TextView address = dialogView.findViewById(R.id.contactAddress);
        Button closeBtn = dialogView.findViewById(R.id.buttonCloseContact);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        phone.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:2101234567"));
            startActivity(intent);
        });

        email.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:info@theatron.gr"));
            startActivity(intent);
        });


        address.setPaintFlags(address.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        address.setOnClickListener(v -> {
            String map = "geo:0,0?q=Βουλής 13, Αθήνα";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        });

        closeBtn.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }


}
