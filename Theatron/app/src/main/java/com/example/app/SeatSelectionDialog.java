package com.example.app;
import android.app.AlertDialog;
import android.content.Context;
import android.view.*;
import android.widget.*;
import androidx.core.content.ContextCompat;

import java.util.List;

public class SeatSelectionDialog {
    public interface OnSeatSelectedListener {
        void onSeatSelected(String seatNumber);
    }

    public static void show(Context context, List<String> seats, String play, String date, String time, OnSeatSelectedListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_seat_selection, null);
        GridLayout seatGrid = dialogView.findViewById(R.id.seatGrid);

        for (String seat : seats) {
            Button seatButton = new Button(context);
            seatButton.setText(seat);
            seatButton.setPadding(0, 0, 0, 0);

            boolean isBooked = !TicketManager.isSeatAvailable(play, date, time, seat);
            seatButton.setBackgroundColor(ContextCompat.getColor(context, isBooked ? android.R.color.holo_red_light : android.R.color.holo_green_light));
            seatButton.setEnabled(!isBooked);

            seatButton.setOnClickListener(v -> {
                listener.onSeatSelected(seat);
            });

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 120;
            params.height = 120;
            params.setMargins(10, 10, 10, 10);
            seatButton.setLayoutParams(params);

            seatGrid.addView(seatButton);
        }

        new AlertDialog.Builder(context)
                .setTitle("🪑 Διάλεξε θέση")
                .setView(dialogView)
                .setNegativeButton("Επιστροφή", null)
                .show();
    }
}

