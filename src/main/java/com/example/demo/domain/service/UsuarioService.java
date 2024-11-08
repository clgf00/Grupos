package com.example.demo.domain.service;
import com.example.demo.config.Constantes;
import com.example.demo.dao.implementacion.DaoUsuarios;
import com.example.demo.dao.implementacion.DataBase;
import com.example.demo.domain.Errores.ErrorApp;
import com.example.demo.domain.Errores.ErrorAppDatosNoValidos;
import com.example.demo.domain.Validators.UserValidator;
import com.example.demo.domain.modelo.Grupo;
import com.example.demo.domain.modelo.Usuario;
import io.vavr.control.Either;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final PasswordEncoder passwordEncoder;
    private final DaoUsuarios daoUsuarios;
    private final UserValidator userValidator;

    @Autowired
    public UsuarioService(PasswordEncoder passwordEncoder, DaoUsuarios daoUsuarios, UserValidator userValidator) {
        this.passwordEncoder = passwordEncoder;
        this.daoUsuarios = daoUsuarios;
        this.userValidator = userValidator;
    }


    public Either<String, List<Usuario>> getUsuarios(){
        return daoUsuarios.cargarUsuarios();
    }

    public Either<String, Boolean> crearUsuario(String username, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        Usuario nuevoUsuario = new Usuario(username, encodedPassword);
        return daoUsuarios.addUsuario(nuevoUsuario);
    }


    public Either<ErrorApp,Boolean> loginUser(Usuario user) {
        return userValidator.validateUser(user)
                .flatMap(ok -> daoUsuarios.findByUsername(user.getUsername()))
                .flatMap(userDB -> {
                    if (passwordEncoder.matches(user.getPassword(), userDB.getPassword()))
                    {
                        return Either.right(true);
                    } else {
                        return Either.left(new ErrorAppDatosNoValidos(Constantes.ERROR));
                    }
                });
    }


    public Either<ErrorApp, Usuario> getUserByUsername(String username) {
        return daoUsuarios.findByUsername(username)
                .toEither(new ErrorAppDatosNoValidos(Constantes.ERROR));
    }

}
