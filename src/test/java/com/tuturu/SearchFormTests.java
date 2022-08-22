package com.tuturu;

import com.codeborne.selenide.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.*;

public class SearchFormTests {
    @BeforeAll
    static void configuration() {
        Configuration.baseUrl = "https://www.tutu.ru";
    }

    @CsvSource(value = {
            "Авиабилеты, Найти билеты",
            "Ж/д билеты,  Найти ж/д билеты",
            "Автобусы,  Найти билеты",
            "Электрички,  Показать расписание"
    })
    @ParameterizedTest(name = "Кнопка поиска в табе \"{0}\" имеет название: \"{1}\"")
    void checkSearchButtonNameIsCorrect(String tabName, String buttonName) {
        open("");
        $(".l-search_forms_tabs").$(byText(tabName)).click();
        $(".j-search_form.tab_active").$$("button").last().shouldHave(text(buttonName));
    }

    @CsvSource(value = {
            "Авиабилеты, https://avia.tutu.ru/",
            "Ж/д билеты,  https://www.tutu.ru/poezda/",
            "Автобусы,  https://bus.tutu.ru/",
            "Отели,  https://hotel.tutu.ru/",
            "Электрички,  https://www.tutu.ru/prigorod/",
            "Туры,  https://tours.tutu.ru/",
            "Приключения,  https://go.tutu.ru/",
            "Справочная,  https://www.tutu.ru/2read/"
    })
    @ParameterizedTest(name = "Элемент меню \"{0}\" открывает страницу: \"{1}\"")
    void checkMenuLinkOpenCorrespondingPage(String menuItemName, String url) {
        open("");
        $$("a").filter(Condition.exactText(menuItemName)).first().click();
        Assertions.assertTrue(Selenide.webdriver().driver().url().contains(url));
    }

    @ValueSource(strings = {"Новосибирск", "Санкт-Петербург"})
    @ParameterizedTest(name = "Результаты поиска авиабилетов из Москвы в {0} на завтра не пустой")
    void checkSearchResultsForTomorrowFlightFromMoscowIsNotEmpty(String city) {
        open("");
        SelenideElement currentTab = $(".j-search_form.tab_active");
        currentTab.$("input[name=city_from]").setValue("Москва");
        currentTab.$$(".name_city").filter(visible).first().click();
        currentTab.$("input[name=city_to]").setValue(city);
        currentTab.$$(".name_city").filter(visible).first().click();

        String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        currentTab.$("input[name=date_from]").setValue(tomorrow).pressEnter();
        $(".j-search_form.tab_active").$(withText("Найти билеты")).parent().click();
        $$("[data-ti=offersList]").shouldBe(CollectionCondition.sizeGreaterThan(0));
    }

    static Stream<Arguments> dataProviderForSelenideSiteMenuTest() {
        return Stream.of(
                Arguments.of("https://www.tutu.travel/poezda/", List.of("Flights", "Hotels", "Local", "Tours", "Travel information (RU)")),
                Arguments.of("https://www.tutu.ru/poezda/", List.of("Авиабилеты", "Автобусы", "Отели", "Электрички", "Туры", "Справочная", "Сюжеты"))
        );
    }
    @MethodSource("dataProviderForSelenideSiteMenuTest")
    @ParameterizedTest(name = "Для версии сайта {0} отображаются кнопки меню {1}")
    void selenideSiteMenuTest(String url, List<String> expectedButtons) {
        open(url);
        $$(".main_menu_stable_links .link a").shouldHave(CollectionCondition.textsInAnyOrder(expectedButtons));
    }
}
