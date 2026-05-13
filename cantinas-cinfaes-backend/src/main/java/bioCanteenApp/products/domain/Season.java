package bioCanteenApp.products.domain;

import java.time.Month;

public enum Season {
    SPRING,
    SUMMER,
    AUTUMN,
    WINTER;

    public static Season fromMonth(Month month) {
        return switch (month) {
            case DECEMBER, JANUARY, FEBRUARY -> WINTER;
            case MARCH, APRIL, MAY -> SPRING;
            case JUNE, JULY, AUGUST -> SUMMER;
            case SEPTEMBER, OCTOBER, NOVEMBER -> AUTUMN;
        };
    }
}
