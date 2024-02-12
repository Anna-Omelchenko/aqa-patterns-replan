package ru.netology.aqa.selenide.data;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;

import java.time.LocalDate;
import java.util.Locale;

public class DataGenerator {

    private static final Faker FAKER = new Faker(new Locale("ru"));

    private DataGenerator() {}

    public static String generateLastFirstName() {
        Name personName = FAKER.name();
        return personName.lastName() + " " + personName.firstName();
    }

    public static String generatePhoneNumber() {
        return FAKER.numerify("+79#########");
    }

    public static CardDeliveryInfo generateCardDeliveryInfo() {
        return new CardDeliveryInfo(
                "Екатеринбург",
                LocalDate.now().plusDays(5),
                generateLastFirstName(),
                generatePhoneNumber());
    }
}
