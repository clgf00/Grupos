package com.example.demo.domain.Errores;

public record ErrorAppDatosNoValidos
        (
                String message
        ) implements ErrorApp {
}
