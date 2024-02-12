package ru.netology.aqa.selenide;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import ru.netology.aqa.selenide.data.CardDeliveryInfo;
import ru.netology.aqa.selenide.data.DataGenerator;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.*;

public class CardDeliveryTest {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            .withZone(ZoneId.systemDefault());

    @Test
    void testPlanWithDirectInput() {
        open("http://localhost:9999");
        SelenideElement form = $(".form");
        String cityName = "Тверь";
        form.$("[data-test-id=city] input").setValue(cityName);
        $(".menu-item").find(byText(cityName)).click();
        String deliveryDate = DATE_FORMATTER.format(LocalDate.now().plusDays(5));
        SelenideElement dateInput = form.$("[data-test-id=date] input");
        dateInput.sendKeys(Keys.CONTROL + "a");
        dateInput.sendKeys(Keys.DELETE);
        dateInput.sendKeys(deliveryDate);
        form.$("[data-test-id=name] input").setValue("Орлова-Тупова Марина");
        form.$("[data-test-id=phone] input").setValue("+79031234567");
        form.$("[data-test-id=agreement]").click();
        form.$(".button").click();
        $("[data-test-id=success-notification]").shouldBe(visible, Duration.ofSeconds(15));
        SelenideElement notification = $("[data-test-id=success-notification]");
        notification.$(byClassName("notification__title")).shouldHave(exactText("Успешно!"));
        notification.$(byClassName("notification__content"))
                .shouldHave(text("Встреча успешно запланирована на " + deliveryDate));
    }

    @Test
    void testPlanWithWidgets() {
        open("http://localhost:9999");
        SelenideElement form = $(".form");
        // Select city
        form.$("[data-test-id=city] input").sendKeys("Вл");
        $$(".menu-item").find(exactText("Владимир")).click();
        // Select delivery date
        form.$("[data-test-id=date] button").click();
        LocalDate deliveryDate = LocalDate.now().plusDays(7);
        if (deliveryDate.getMonthValue() != LocalDate.now().getMonthValue()) {
            $$(".calendar__arrow_direction_right")
                    .find(Condition.attribute("data-step", "1")).click();
        }
        String deliveryDay = String.valueOf(deliveryDate.getDayOfMonth());
        $$(".calendar__day").find(exactText(deliveryDay)).click();
        // Input other fields
        form.$("[data-test-id=name] input").setValue("Орлова-Тупова Марина");
        form.$("[data-test-id=phone] input").setValue("+79031234567");
        form.$("[data-test-id=agreement]").click();
        form.$(".button").click();
        $("[data-test-id=success-notification]").shouldBe(visible, Duration.ofSeconds(15));
        SelenideElement notification = $("[data-test-id=success-notification]");
        notification.$(byClassName("notification__title")).shouldHave(exactText("Успешно!"));
        notification.$(byClassName("notification__content"))
                .shouldHave(text("Встреча успешно запланирована на " + DATE_FORMATTER.format(deliveryDate)));
    }

    @Test
    void testReplanWithoutPageReload() {
        open("http://localhost:9999");
        // Generate test data
        CardDeliveryInfo info = DataGenerator.generateCardDeliveryInfo();
        // Fill in the form
        SelenideElement form = $(".form");
        fillInCardDeliveryForm(form, info);
        // Click submit button
        form.$(".button").click();
        SelenideElement successNotification = $("[data-test-id=success-notification]");
        successNotification.shouldBe(visible, Duration.ofSeconds(15));
        // Close success-notification
        successNotification.$(byClassName("notification__closer")).click();
        successNotification.shouldBe(not(visible));
        // Set another delivery date
        String anotherDeliveryDate = DATE_FORMATTER.format(info.getDeliveryDate().plusDays(3));
        SelenideElement dateInput = form.$("[data-test-id=date] input");
        dateInput.sendKeys(Keys.CONTROL + "a");
        dateInput.sendKeys(Keys.DELETE);
        dateInput.sendKeys(anotherDeliveryDate);
        // Click the Submit button
        form.$(".button").click();
        // Check replan-notification
        SelenideElement replanNotification = $("[data-test-id=replan-notification]");
        replanNotification.shouldBe(visible, Duration.ofSeconds(15));
        replanNotification.$(byClassName("notification__title")).shouldHave(exactText("Необходимо подтверждение"));
        SelenideElement replanContent = replanNotification.$(byClassName("notification__content"));
        replanContent.shouldHave(exactOwnText("У вас уже запланирована встреча на другую дату. Перепланировать?"));
        // Click the Replan button
        SelenideElement replanButton = replanContent.$(byTagName("button"));
        replanButton.$(byClassName("button__text")).shouldHave(exactText("Перепланировать"));
        replanButton.click();
        replanNotification.shouldBe(not(visible));
        // Check success-notification
        successNotification.shouldBe(visible, Duration.ofSeconds(15));
        successNotification.$(byClassName("notification__title")).shouldHave(exactText("Успешно!"));
        successNotification.$(byClassName("notification__content"))
                .shouldHave(text("Встреча успешно запланирована на " + anotherDeliveryDate));
        // Close success-notification
        successNotification.$(byClassName("notification__closer")).click();
        successNotification.shouldBe(not(visible));
    }

    @Test
    void testReplanWithPageReload() {
        open("http://localhost:9999");
        // Generate test data
        CardDeliveryInfo info = DataGenerator.generateCardDeliveryInfo();
        // Fill in the form
        SelenideElement form = $(".form");
        fillInCardDeliveryForm(form, info);
        // Click submit button
        form.$(".button").click();
        SelenideElement successNotification = $("[data-test-id=success-notification]");
        successNotification.shouldBe(visible, Duration.ofSeconds(15));
        // Close success-notification
        successNotification.$(byClassName("notification__closer")).click();
        successNotification.shouldBe(not(visible));
        // Reload page
        open("http://localhost:9999");
        // Fill in the form
        int daysToAdd = 2;
        fillInCardDeliveryForm(form, info, daysToAdd);
        // Click the Submit button
        form.$(".button").click();
        // Check replan-notification
        SelenideElement replanNotification = $("[data-test-id=replan-notification]");
        replanNotification.shouldBe(visible, Duration.ofSeconds(15));
        replanNotification.$(byClassName("notification__title")).shouldHave(exactText("Необходимо подтверждение"));
        SelenideElement replanContent = replanNotification.$(byClassName("notification__content"));
        replanContent.shouldHave(exactOwnText("У вас уже запланирована встреча на другую дату. Перепланировать?"));
        // Click the Replan button
        SelenideElement replanButton = replanContent.$(byTagName("button"));
        replanButton.$(byClassName("button__text")).shouldHave(exactText("Перепланировать"));
        replanButton.click();
        replanNotification.shouldBe(not(visible));
        // Check success-notification
        successNotification.shouldBe(visible, Duration.ofSeconds(15));
        successNotification.$(byClassName("notification__title")).shouldHave(exactText("Успешно!"));
        successNotification.$(byClassName("notification__content"))
                .shouldHave(text("Встреча успешно запланирована на " +
                        DATE_FORMATTER.format(info.getDeliveryDate().plusDays(daysToAdd))));
        // Close success-notification
        successNotification.$(byClassName("notification__closer")).click();
        successNotification.shouldBe(not(visible));
    }

    private static void fillInCardDeliveryForm(SelenideElement form, CardDeliveryInfo info, int daysToAdd) {
        form.$("[data-test-id=city] input").setValue(info.getCityName());
        $(".menu-item").find(byText(info.getCityName())).click();
        SelenideElement dateInput = form.$("[data-test-id=date] input");
        dateInput.sendKeys(Keys.CONTROL + "a");
        dateInput.sendKeys(Keys.DELETE);
        dateInput.sendKeys(DATE_FORMATTER.format(info.getDeliveryDate().plusDays(daysToAdd)));
        form.$("[data-test-id=name] input").setValue(info.getPersonLastFirstName());
        form.$("[data-test-id=phone] input").setValue(info.getPhoneNumber());
        form.$("[data-test-id=agreement]").click();
    }

    private static void fillInCardDeliveryForm(SelenideElement form, CardDeliveryInfo info) {
        fillInCardDeliveryForm(form, info, 0);
    }
}
