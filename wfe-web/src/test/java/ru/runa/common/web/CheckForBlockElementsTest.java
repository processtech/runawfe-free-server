package ru.runa.common.web;

public class CheckForBlockElementsTest {

    public static void main(String[] args) {
        System.out.println(HTMLUtils.checkForBlockElements("abc <dD>abc</dd>"));
        System.out.println(HTMLUtils.checkForBlockElements("abc <div>abc</div>"));
        System.out.println(HTMLUtils.checkForBlockElements("abc <span>abc</span>"));
    }
}
