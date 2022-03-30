package com.github.houseorganizer.houseorganizer;

import static com.github.houseorganizer.houseorganizer.login.LoginHelpers.inputsEmpty;
import static com.github.houseorganizer.houseorganizer.login.LoginHelpers.isValidEmail;
import static com.github.houseorganizer.houseorganizer.login.LoginHelpers.isValidPassword;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LoginHelpersUnitTest {


    @Test
    public void isValidEmailReturnsCorrectResponse() {
        assertTrue(isValidEmail("ex@example.com"));
        assertTrue(isValidEmail("ex.user@example.ch"));
        assertTrue(isValidEmail("ex_user@example.com"));
        assertTrue(isValidEmail("ex.user_user@example.com"));
        assertFalse(isValidEmail("@example.com"));
        assertFalse(isValidEmail("ex@.com"));
        assertFalse(isValidEmail("ex@com"));
        assertFalse(isValidEmail("ex.example.com"));
        assertTrue(isValidEmail("ex2000@example.com"));
        assertTrue(isValidEmail("ex.20@example.com"));
        assertTrue(isValidEmail("ex*@example.com"));
        assertTrue(isValidEmail("ex.*us@example.com"));
    }

    @Test
    public void isValidPasswordReturnsCorrectResponse() {
        assertTrue(isValidPassword("123A@hme", "123A@hme"));
        assertTrue(isValidPassword("123/*-+=?:;A@hme", "123/*-+=?:;A@hme"));
        assertFalse(isValidPassword("123A@hme0", "123A@hme1"));
        assertFalse(isValidPassword("abcdefgh", "abcdefgh"));
        assertFalse(isValidPassword("/*-*%&()", "/*-*%&()"));
        assertFalse(isValidPassword("12345678", "12345678"));
        assertFalse(isValidPassword("1234a678", "1234a678"));
        assertFalse(isValidPassword("1234aA78", "1234aA78"));
        assertFalse(isValidPassword("1234a@78", "1234a@78"));
        assertFalse(isValidPassword("1234a@78", "1234A@78"));
        assertTrue(isValidPassword("1234aA@78", "1234aA@78"));
    }

    @Test
    public void inputsEmptyReturnsCorrectResult() {
        assertTrue(inputsEmpty("", ""));
        assertTrue(inputsEmpty("", "notempty"));
        assertTrue(inputsEmpty("notempty", ""));
        assertFalse(inputsEmpty("notempty", "notempty"));
    }
}
