package com.example.app;

import java.util.*;
import java.util.stream.Collectors;

public class TicketManager {

    private static final Set<Booking> bookings = new HashSet<>();

    // Κράτηση θέσης
    public static boolean bookSeat(String play, String date, String time, String seat, String email) {
        Booking booking = new Booking(play, date, time, seat, email);
        if (bookings.contains(booking)) return false;
        bookings.add(booking);
        return true;
    }

    // Έλεγχος διαθεσιμότητας
    public static boolean isSeatAvailable(String play, String date, String time, String seat) {
        return !bookings.contains(new Booking(play, date, time, seat, ""));
    }

    // Ακύρωση θέσης
    public static boolean cancelSeat(String play, String date, String time, String seat, String email) {
        return bookings.removeIf(b ->
                b.getPlay().equals(play) &&
                        b.getDate().equals(date) &&
                        b.getTime().equals(time) &&
                        b.getSeat().equals(seat) &&
                        b.getEmail().equals(email));
    }

    // Όλες οι κρατήσεις
    public static Set<String> getAllBookings() {
        return bookings.stream()
                .map(Booking::toString)
                .collect(Collectors.toSet());
    }

    public static List<String> getBookingsByEmail(String email) {
        return bookings.stream()
                .filter(b -> b.getEmail().equalsIgnoreCase(email))
                .map(b -> b.getPlay() + ", " + b.getDate() + ", " + b.getTime() + ", " + b.getSeat())
                .collect(Collectors.toList());
    }


}
