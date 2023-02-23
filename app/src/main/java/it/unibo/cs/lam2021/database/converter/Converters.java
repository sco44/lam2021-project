package it.unibo.cs.lam2021.database.converter;

import androidx.room.TypeConverter;

import java.time.LocalDate;

public class Converters {

    @TypeConverter
    public static LocalDate fromTimestamp(Long value) {
        return value == LocalDate.MAX.toEpochDay() ? null : LocalDate.ofEpochDay(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(LocalDate date) {
        return date == null ? LocalDate.MAX.toEpochDay() : date.toEpochDay();
    }
}
