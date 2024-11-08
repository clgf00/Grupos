package com.example.demo.domain.service;

import com.example.demo.config.Constantes;
import com.example.demo.config.MainAesTestIVRandomMAC;
import com.example.demo.dao.implementacion.DaoGrupos;
import com.example.demo.dao.implementacion.DaoMensajes;
import com.example.demo.domain.Errores.ExcepcionEncriptar;
import com.example.demo.domain.modelo.Grupo;
import com.example.demo.domain.modelo.Mensaje;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MensajeService {

    private final MainAesTestIVRandomMAC encryptionService;
    private final DaoMensajes daoMensajes;
    private final DaoGrupos daoGrupos;

    public MensajeService(MainAesTestIVRandomMAC encryptionService, DaoMensajes daoMensajes, DaoGrupos daoGrupos) {
        this.encryptionService = encryptionService;
        this.daoMensajes = daoMensajes;
        this.daoGrupos = daoGrupos;
    }

    public Either<String, String> addMensajeToGrupo(Mensaje mensajeNuevo, String clave) {
        if (mensajeNuevo.getGrupo().isPrivado()) {
            return daoMensajes.registerMensaje(mensajeNuevo);
        } else {
            if(clave.isEmpty()){
                return Either.left(Constantes.ERROR);
            }
            else {
                try {
                    String textoEncriptado = encryptionService.encrypt(mensajeNuevo.getTexto(), clave);
                    Mensaje mensajeEncriptado = new Mensaje(textoEncriptado, mensajeNuevo.getEmisor(), mensajeNuevo.getRemitentes(), mensajeNuevo.getGrupo());
                    return daoMensajes.registerMensaje(mensajeEncriptado)
                            .map(ok -> mensajeNuevo.getTexto())
                            .mapLeft(error -> Constantes.ERROR);
                } catch (ExcepcionEncriptar e) {
                    return Either.left(Constantes.ERROR);
                }
            }
        }
    }

    public Either<String, List<String>> getMensajesOfGrupo(String grupoName, String privateKey) {
        return daoMensajes.getMensajesOfGrupo(grupoName).flatMap(mensajesLista -> {
            Either<String, Grupo> grupo = daoGrupos.getGrupoByName(grupoName);
            if (grupo.isRight()) {
                if (grupo.get().isPrivado()) {
                    List<String> mensajeList = new ArrayList<>();
                    return daoMensajes.getMensajesOfGrupo(grupoName)
                            .flatMap(mensajes -> {
                                mensajes.forEach(mensaje -> mensajeList.add(mensaje.toString()));
                                return Either.right(mensajeList);
                            });
                } else {
                    if (privateKey.isEmpty()) {
                        return Either.left(Constantes.ERROR);

                    } else {
                        try {
                            List<String> mensajesDescifrados = new ArrayList<>();
                            mensajesLista.forEach(mensaje -> {
                                String mensajeDescifrado = encryptionService.decrypt(mensaje.getTexto(), privateKey);
                                mensajesDescifrados.add(mensajeDescifrado);
                            });
                            return Either.right(mensajesDescifrados);
                        } catch (ExcepcionEncriptar e) {
                            return Either.left(Constantes.ERROR);
                        }

                    }


                }
            } else {
                return Either.left("Error al registrar el mensaje: " + grupoName);
            }
        });
    }
}


