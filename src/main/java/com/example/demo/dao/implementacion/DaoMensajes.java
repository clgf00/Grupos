package com.example.demo.dao.implementacion;


import com.example.demo.config.Constantes;
import com.example.demo.domain.modelo.Mensaje;
import io.vavr.control.Either;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DaoMensajes {

    private final DataBase dataBase;

    public DaoMensajes(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    public Either<String, String> registerMensaje(Mensaje mensajeNuevo) {
        Either<String, List<Mensaje>> mensajesEither = dataBase.loadMensajes();

        if (mensajesEither.isLeft()) {
            return Either.left(Constantes.ERROR);
        }

        List<Mensaje> mensajes = mensajesEither.get();
        mensajes.add(mensajeNuevo);
        Either<String, Boolean> saveResult = dataBase.saveMensajes(mensajes);

        if (saveResult.isLeft()) {
            return Either.left(Constantes.ERROR);
        }

        return Either.right(mensajeNuevo.getTexto());
    }

    public Either<String, List<Mensaje>> getMensajesOfGrupo(String grupoName) {
        Either<String, List<Mensaje>> mensajesEither = dataBase.loadMensajes();

        if (mensajesEither.isLeft()) {
            return Either.left(Constantes.ERROR);
        }

        List<Mensaje> mensajes = mensajesEither.get();

        List<Mensaje> mensajesFiltrados = mensajes.stream()
                .filter(mensaje -> mensaje.getGrupo().getNombre().equals(grupoName))
                .toList();

        if (mensajesFiltrados.isEmpty()) {
            return Either.left(Constantes.ERROR);
        }

        return Either.right(mensajesFiltrados);
    }
}
