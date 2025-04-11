package com.ibaisologuestoa.acciones500;

import android.util.Patterns;

public class InputUtils {

    public static boolean validarEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean validarPassword(String password) {
        return password.length() >= 6;
    }

    public static boolean camposVacios(String... campos) {
        for (String campo : campos) {
            if (campo == null || campo.trim().isEmpty()) return true;
        }
        return false;
    }
}