package com.example.demo.domain.service;

import com.example.demo.config.Constantes;
import com.example.demo.dao.implementacion.DaoGrupos;
import com.example.demo.domain.modelo.Grupo;
import com.example.demo.domain.modelo.Usuario;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class GrupoService {
    //EN LOS SERVICES TODA LA LOGICA, VALIDADORES...
    private final DaoGrupos daoGrupos;

    public GrupoService(DaoGrupos daoGrupos) {
        this.daoGrupos = daoGrupos;
    }

    public Either<String, List<Grupo>> getGrupos(){
        return daoGrupos.cargarGrupos();
    }

    public Either<String, Boolean> entrarGrupo(String grupoNombre, String password, Usuario user) {
        return daoGrupos.getGrupoByName(grupoNombre)
                .flatMap(grupo -> {
                    if (!grupo.getPassword().equals(password)) {
                        return Either.left(Constantes.GRUPO_INEXISTENTE_O_CONTRASENYA_INCORRECTA);
                    }

                    if (grupo.isPrivado() && !grupo.getMiembros().contains(user)) {
                        return Either.left(Constantes.NO_PUEDES_ENTRAR_A_GRUPO_PRIVADO);
                    }

                    if (!grupo.getMiembros().contains(user)) {
                        return daoGrupos.addUserToGrupo(grupo, user)
                                .map(ok -> true);
                    } else {
                        return Either.right(true);
                    }
                });
    }



    public Either<String, Boolean> addGrupo(String nombreGrupo, String passwordGrupo, boolean esPrivado, Usuario usuario) {
        if (nombreGrupo.isEmpty() || passwordGrupo.isEmpty()) {
            return Either.left(Constantes.ERROR);
        }

        Grupo nuevoGrupo = new Grupo(new ArrayList<>(), nombreGrupo, passwordGrupo, esPrivado, usuario);
        nuevoGrupo.addMiembro(usuario, usuario);

        return daoGrupos.registerGrupo(nuevoGrupo)
                .flatMap(ok -> daoGrupos.addUserToGrupo(nuevoGrupo, usuario)
                        .map(success -> true))
                .mapLeft(error -> error);
    }
}



