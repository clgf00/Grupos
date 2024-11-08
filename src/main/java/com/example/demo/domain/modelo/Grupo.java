package com.example.demo.domain.modelo;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
@Data
public class Grupo {

    private final List<Usuario> miembros;
    private final String nombre;
    private final String password;
    private final boolean privado;
    private Usuario admin;

    public Grupo(List<Usuario> miembros, String nombre, String password, boolean privado, Usuario admin) {
        this.miembros = miembros;
        this.nombre = nombre;
        this.password = password;
        this.privado = privado;
        this.admin = admin;
    }

    public void addMiembro(Usuario adminSolicitante, Usuario nuevoMiembro) {
        if (this.admin.equals(adminSolicitante)) {
            miembros.add(nuevoMiembro);
        }
    }
    public boolean esAdmin(Usuario usuario) {
        return this.admin.equals(usuario);
    }
}
