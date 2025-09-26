package com.example.app;

import android.app.AlertDialog;
import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.core.content.ContextCompat;
import java.util.List;

public class SeatSelection {

    public interface OnSeatConfirmedListener {
        void onSeatConfirmed(String seatNumber);
    }

    public static void show(Context context,
                            List<String> allSeats,
                            String play,
                            String date,
                            String time,
                            String preselectedSeat,
                            OnSeatConfirmedListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.seat_selection, null);
        GridLayout seatGrid = dialogView.findViewById(R.id.seatGrid);
        Button confirmButton = dialogView.findViewById(R.id.buttonConfirmSeat);
        Button cancelButton = dialogView.findViewById(R.id.buttonCancelSeat);

        final Button[] lastSelected = {null};
        final String[] selectedSeat = {preselectedSeat}; // ✅ προεπιλεγμένη θέση

        for (String seat : allSeats) {
            Button seatButton = new Button(context);
            seatButton.setText(seat);
            seatButton.setPadding(0, 0, 0, 0);
            seatButton.setTextColor(ContextCompat.getColor(context, android.R.color.white));

            boolean isBooked = !TicketManager.isSeatAvailable(play, date, time, seat);
            seatButton.setEnabled(!isBooked);

            if (seat.equals(preselectedSeat)) {
                seatButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_light));
                lastSelected[0] = seatButton;
            } else {
                seatButton.setBackgroundColor(ContextCompat.getColor(context,
                        isBooked ? android.R.color.holo_red_light : android.R.color.holo_green_dark));
            }

            seatButton.setOnClickListener(v -> {
                if (lastSelected[0] != null && lastSelected[0] != seatButton) {
                    lastSelected[0].setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                }
                seatButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_light));
                lastSelected[0] = seatButton;
                selectedSeat[0] = seat;
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 140;
            params.height = 140;
            params.setMargins(20, 20, 20, 20);
            seatButton.setLayoutParams(params);
            seatGrid.addView(seatButton);
        }

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        confirmButton.setOnClickListener(v -> {
            if (selectedSeat[0] != null) {
                listener.onSeatConfirmed(selectedSeat[0]);
                dialog.dismiss();
            } else {
                Toast.makeText(context, "Παρακαλώ διάλεξε θέση", Toast.LENGTH_SHORT).show();
            }
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public static void show(Context context,
                            List<String> allSeats,
                            String play,
                            String date,
                            String time,
                            OnSeatConfirmedListener listener) {
        show(context, allSeats, play, date, time, null, listener); // null = no preselection
    }
}
