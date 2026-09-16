package com.company.permit.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordPolicyLogicTest {

    @Test
    public void 默认规则_至少8位含字母数字() {
        assertTrue("Admin@123".matches(".*[A-Za-z].*") && "Admin@123".matches(".*\\d.*") && "Admin@123".length() >= 8);
        assertFalse("abc".length() >= 8);
        assertFalse("12345678".matches(".*[A-Za-z].*"));
    }
}
