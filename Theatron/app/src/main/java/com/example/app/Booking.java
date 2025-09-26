package com.example.app;

import java.util.Objects;

public class Booking {
    private final String play;
    private final String date;
    private final String time;
    private final String seat;
    private final String email;

    public Booking(String play, String date, String time, String seat, String email) {
        this.play = play;
        this.date = date;
        this.time = time;
        this.seat = seat;
        this.email = email;
    }

    public String getPlay() { return play; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getSeat() { return seat; }
    public String getEmail() { return email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        return play.equals(booking.play) &&
                date.equals(booking.date) &&
                time.equals(booking.time) &&
                seat.equals(booking.seat);
    }

    @Override
    public int hashCode() {
        return Objects.hash(play, date, time, seat);
    }

    @Override
    public String toString() {
        return email + "\n" + play + ", " + date + ", " + time + ", " + seat ;
    }
}
