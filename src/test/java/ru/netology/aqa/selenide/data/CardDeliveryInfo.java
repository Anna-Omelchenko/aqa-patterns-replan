package ru.netology.aqa.selenide.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Data
@RequiredArgsConstructor
public class CardDeliveryInfo {
    private final String cityName;
    private final LocalDate deliveryDate;
    private final String personLastFirstName;
    private final String phoneNumber;
}
