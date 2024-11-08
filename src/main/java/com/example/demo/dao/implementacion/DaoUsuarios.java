package com.example.demo.dao.implementacion;
import com.example.demo.config.Constantes;
import com.example.demo.domain.Errores.ErrorApp;
import com.example.demo.domain.Errores.ErrorAppDatosNoValidos;
import com.example.demo.domain.modelo.Usuario;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vavr.control.Either;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Data
@Component
public class DaoUsuarios {

    private final DataBase dataBase;
    private List<Usuario> usuarios;

    @Autowired
    public DaoUsuarios(DataBase dataBase) {
        this.dataBase = dataBase;
        cargarUsuarios();
    }

    public Either<String, List<Usuario>> cargarUsuarios() {
        Either<String, List<Usuario>> usuariosEither = dataBase.loadUsuarios();

        if (usuariosEither.isLeft()) {
            return usuariosEither;
        }

        usuarios = usuariosEither.get();
        return Either.right(usuarios);
    }


    public Either<String, Boolean> addUsuario(Usuario usuarioNuevo) {
        Either<ErrorApp, Usuario> usuarioExistente = findByUsername(usuarioNuevo.getUsername());

        if (usuarioExistente.isRight()) {
            return Either.left(Constantes.ERROR);
        }

        usuarios.add(usuarioNuevo);
        Either<String, Boolean> saved = dataBase.saveUsuarios(usuarios);

        if (saved.isLeft()) {
            return Either.left(saved.getLeft());
        }

        return Either.right(true);
        }

    public Either<ErrorApp, Usuario> findByUsername(String username) {
        return usuarios.stream()
                .filter(usuario -> usuario.getUsername().equals(username))
                .findFirst()
                .map(Either::<ErrorApp, Usuario>right)
                .orElseGet(() -> Either.left(new ErrorAppDatosNoValidos(Constantes.USER_NOT_FOUND + username))); // Si no se encuentra, devuelve un error
    }
}
