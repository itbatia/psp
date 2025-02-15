package com.itbatia.psp.Utils;

import com.itbatia.psp.dto.CardDto;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Batsian_SV
 */
@Component
public class MaskingUtil {

    private final static Pattern pattern = Pattern.compile("(\\d{4})(\\d{8})(\\d{4})");

    public static String masking(String cardNumber) {
        Matcher matcher = pattern.matcher(cardNumber);
        return matcher.replaceAll("$1***$3");
    }

    public static void masking(CardDto cardDto) {
        Matcher matcher = pattern.matcher(cardDto.getCardNumber());
        String maskedNumber = matcher.replaceAll("$1***$3");
        cardDto.setCardNumber(maskedNumber);
    }
}
