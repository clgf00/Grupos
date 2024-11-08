package com.example.demo.dao.implementacion;

import com.example.demo.config.Configuracion;
import com.example.demo.config.Constantes;
import com.example.demo.domain.modelo.Grupo;
import com.example.demo.domain.modelo.Mensaje;
import com.example.demo.domain.modelo.Usuario;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import io.vavr.control.Either;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Component
public class DataBase {

    private final Gson gson;
    private final Configuracion configuracion;

    public DataBase(Gson gson, Configuracion configuracion) {
        this.gson = gson;
        this.configuracion = configuracion;
    }


    public Either<String, List<Usuario>> loadUsuarios() {
        Type userListType = new TypeToken<ArrayList<Usuario>>(){}.getType();
        List<Usuario> usuarios = null;

        try {
            usuarios = gson.fromJson(
                    new FileReader(configuracion.getPathJsonUsuarios()),
                    userListType);

            if (usuarios == null) {
                usuarios = new ArrayList<>();
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            return Either.left(Constantes.ERROR);
        }
        return Either.right(usuarios);
    }


    public Either<String, Boolean> saveUsuarios(List<Usuario> usuarios) {
        try (FileWriter w = new FileWriter(configuracion.getPathJsonUsuarios())) {
            gson.toJson(usuarios, w);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Either.left(Constantes.ERROR);
        }
        return Either.right(true);
    }


    public void updatePasswordsWithHash(PasswordEncoder passwordEncoder) {
        Either<String, List<Usuario>> usuariosEither = loadUsuarios();

        if (usuariosEither.isLeft()) {
            usuariosEither.getLeft();
            return;
        }

        List<Usuario> usuarios = usuariosEither.get();

        for (Usuario usuario : usuarios) {
            Either<String, Boolean> isEncoded = isPasswordEncoded(usuario.getPassword());

            if (isEncoded.isRight() && Boolean.TRUE.equals(!isEncoded.get())) {
                String hashedPassword = passwordEncoder.encode(usuario.getPassword());
                usuario.setPassword(hashedPassword);
            }
        }
        Either<String, Boolean> saved = saveUsuarios(usuarios);
        if (saved.isLeft()) {
            saved.getLeft();
        }

    }



    public Either<String, List<Mensaje>> loadMensajes() {
        Type mensajeListType = new TypeToken<ArrayList<Mensaje>>() {
        }.getType();

        try {
            List<Mensaje> mensajes = gson.fromJson(
                    new FileReader(configuracion.getPathJsonMensajes()),
                    mensajeListType);

            if (mensajes == null) {
                mensajes = new ArrayList<>();
            }

            return Either.right(mensajes);

        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            return Either.left(Constantes.ERROR);

        } catch (JsonSyntaxException e) {
            log.error(Constantes.ERROR);
            return Either.left(Constantes.ERROR);

        }
    }


    public Either<String, List<Grupo>> loadGrupos() {
        Type grupoListType = new TypeToken<ArrayList<Grupo>>() {
        }.getType();

        List<Grupo> grupos = null;
        try {
            grupos = gson.fromJson(
                    new FileReader(configuracion.getPathJsonGrupos()),
                    grupoListType);
            if (grupos == null) {
                grupos = new ArrayList<>();
            }
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            return Either.left(Constantes.ERROR);
        }
        return Either.right(grupos);
    }
    public Either<String, Boolean> saveMensajes(List<Mensaje> mensajes) {
        try (FileWriter w = new FileWriter(configuracion.getPathJsonMensajes())) {
            gson.toJson(mensajes, w);
            return Either.right(true);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Either.left(Constantes.ERROR);
        }
    }

    public Either<String, Boolean> saveGrupos(List<Grupo> grupos) {

        try (FileWriter w = new FileWriter(configuracion.getPathJsonGrupos())) {
            gson.toJson(grupos, w);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return Either.left(Constantes.ERROR);
        }

        return Either.right(true);
    }
    private Either<String, Boolean> isPasswordEncoded(String password) {
        if (password == null) {
            return Either.left(Constantes.ERROR);
        }
        boolean isEncoded = password.startsWith("$2a$");

        if (isEncoded) {
            return Either.right(true);
        } else {
            return Either.left(Constantes.ERROR);
        }
    }

}
