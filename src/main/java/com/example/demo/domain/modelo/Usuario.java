package com.example.demo.domain.modelo;

import lombok.Data;
import lombok.Getter;

import java.util.Objects;

@Getter
@Data
public class Usuario {
    private final String username;
    private String password;
    private boolean admin;

    public Usuario(String username, String password) {

        this.username = username;
        this.password = password;
        this.admin = isAdmin();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Usuario usuario = (Usuario) obj;
        return username.equals(usuario.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

}
