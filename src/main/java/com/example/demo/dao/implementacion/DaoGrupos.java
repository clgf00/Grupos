package com.example.demo.dao.implementacion;

import com.example.demo.config.Configuracion;
import com.example.demo.config.Constantes;
import com.example.demo.domain.modelo.Grupo;
import com.example.demo.domain.modelo.Usuario;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vavr.control.Either;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

@Data
@Component
public class DaoGrupos {
    private final DataBase dataBase;
    private final Configuracion config;

    public DaoGrupos(DataBase dataBase, Configuracion config) {
        this.dataBase = dataBase;
        this.config = config;
    }

    public Either<String, List<Grupo>> cargarGrupos() {
        List<Grupo> gruposList;
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(config.getPathJsonGrupos())) {
            Type tipoListaGrupos = new TypeToken<List<Grupo>>() {
            }.getType();

            gruposList = gson.fromJson(reader, tipoListaGrupos);
            return Either.right(gruposList);
        } catch (IOException e) {
            return Either.left(e.getMessage());
        }
    }
    public Either<String, Boolean> registerGrupo(Grupo grupoNuevo) {

        return dataBase.loadGrupos()
                .flatMap(grupos -> {
                    if (grupos.stream().anyMatch(grupo -> grupo.getNombre().equals(grupoNuevo.getNombre()))) {
                        return Either.left(Constantes.ERROR);
                    } else {
                        return Either.right(grupos);
                    }
                })
                .flatMap(grupos -> {
                    grupos.add(grupoNuevo);
                    return dataBase.saveGrupos(grupos);
                });
    }
    public Either<String, Boolean> addUserToGrupo(Grupo grupo, Usuario nuevoMiembro) {
        try {
            if (grupo.getMiembros().contains(nuevoMiembro)) {
                return Either.left(Constantes.ERROR);
            }
            grupo.getMiembros().add(nuevoMiembro);

            return Either.right(true);

        } catch (Exception e) {
            return Either.left(Constantes.ERROR);
        }
    }
    public Either<String,Grupo> getGrupoByName(String grupoName) {
        return dataBase.loadGrupos()
                .flatMap(grupos->grupos.stream()
                        .filter(grupo -> grupo.getNombre().equals(grupoName))
                        .findFirst()
                        .map(Either::<String, Grupo>right)
                        .orElseGet(() -> Either.left("Group not found: " + grupoName)));

    }
}
