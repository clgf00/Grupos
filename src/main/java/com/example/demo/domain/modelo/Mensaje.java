package com.example.demo.domain.modelo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;


@Data
public class Mensaje {

    private final String texto;
    private final Usuario emisor;
    private final ArrayList<Usuario> remitentes;
    private final Grupo grupo;


}
