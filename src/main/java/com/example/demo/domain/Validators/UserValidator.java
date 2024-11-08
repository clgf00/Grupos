package com.example.demo.domain.Validators;

import com.example.demo.config.Constantes;
import com.example.demo.domain.Errores.ErrorApp;
import com.example.demo.domain.Errores.ErrorAppDatosNoValidos;
import com.example.demo.domain.modelo.Usuario;
import io.vavr.control.Either;
import org.springframework.stereotype.Component;
@Component
public class UserValidator {

    public Either<ErrorApp, Usuario> validateUser(Usuario user) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            return Either.left(new ErrorAppDatosNoValidos(Constantes.NOMBRE_NO_VALIDO));
        }
        return Either.right(user);
    }
}