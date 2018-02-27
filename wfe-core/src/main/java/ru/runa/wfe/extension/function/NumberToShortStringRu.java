package ru.runa.wfe.extension.function;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Dmitry Kononov
 * @since 28.02.2018
 *
 */
public class NumberToShortStringRu extends Function<String> {

    private static Integer[] digits = { 2, 0, 1, 1, 1, 2, 2, 2, 2, 0 };

    @Override
    protected String doExecute(Object... parameters) {
        int number = (int) parameters[0];
        List<String> symbols = new ArrayList<>(0);
        for (int i = 2; i < parameters.length; i++) {
            symbols.add((String) parameters[i]);
        }
        String[] wordSymbols = symbols.toArray(new String[symbols.size()]);
        Word word = new Word(wordSymbols);
        String answer = "";
        number %= 100;
        if (number / 10 == 1) {
            if (answer.length() > 0) {
                answer += " ";
            }
            answer += word.s[2];
            return answer;
        }
        number %= 10;
        answer += word.s[digits[(int) number]];
        return answer;
    }

    private static class Word {
        public String[] s;

        public Word(String[] s) {
            this.s = s;
        }
    }

    public static String numberToShortString(long number, Word word) {
        String answer = "";
        number %= 100;
        if (number / 10 == 1) {
            if (answer.length() > 0) {
                answer += " ";
            }
            answer += word.s[2];
            return answer;
        }
        number %= 10;
        answer += word.s[digits[(int) number]];
        return answer;
    }

}
