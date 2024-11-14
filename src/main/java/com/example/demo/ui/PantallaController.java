package com.example.demo.ui;

import com.example.demo.config.Constantes;
import com.example.demo.dao.implementacion.DataBase;
import com.example.demo.domain.Errores.ErrorApp;
import com.example.demo.domain.modelo.Grupo;
import com.example.demo.domain.modelo.Mensaje;
import com.example.demo.domain.modelo.Usuario;
import com.example.demo.domain.service.GrupoService;
import com.example.demo.domain.service.MensajeService;
import com.example.demo.domain.service.UsuarioService;
import io.vavr.control.Either;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PantallaController {
    private final UsuarioService usuarioService;
    private final GrupoService grupoService;
    private final MensajeService mensajeService;
    private final PasswordEncoder passwordEncoder;

    @FXML
    private TextField desencriptar;
    @FXML
    private ListView<Usuario> listAllUsers;
    @FXML
    private TextField userSignInput;
    @FXML
    private PasswordField passSignInput;
    @FXML
    private AnchorPane paneInicio;
    @FXML
    private TextField userInput;
    @FXML
    private TextField groupInput;
    @FXML
    private TextField groupInput2;
    @FXML
    private PasswordField passInput;
    @FXML
    private PasswordField groupPassInput;
    @FXML
    private PasswordField groupPassInput2;
    @FXML
    private TextArea messageInput;
    @FXML
    private ListView<String> chatAreaUser;
    @FXML
    private ListView<String> grupoList;
    @FXML
    private Label greenLabelText;
    @FXML
    private Label redLabelText;
    @FXML
    private Label labelText2;
    @FXML
    private ListView<Usuario> listUsers;
    @FXML
    private CheckBox checkboxPrivado;
    private String grupoActivo = "";
    private Usuario usuario;
    private DataBase dataBase;

    public PantallaController(UsuarioService usuarioService, GrupoService grupoService, MensajeService mensajeService, PasswordEncoder passwordEncoder, DataBase dataBase) {
        this.usuarioService = usuarioService;
        this.grupoService = grupoService;
        this.mensajeService = mensajeService;
        this.passwordEncoder = passwordEncoder;
        this.dataBase = dataBase;
    }

    public void logIn() {
        String username = userInput.getText().trim();
        String password = passInput.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            greenLabelText.setText(Constantes.CREDENCIALES_NO_PUEDEN_ESTAR_VACIAS);
            return;
        }

        Either<ErrorApp, Boolean> loginResult = usuarioService.loginUser(new Usuario(username, password));

        loginResult.peek(ok -> {
            Either<ErrorApp, Usuario> usuarioResult = usuarioService.getUserByUsername(username);

            usuarioResult.peek(user -> {
                this.usuario = user;
                labelText2.setText(Constantes.BIENVENID + usuario.getUsername());
                initialize();
                cargarGrupos();
            }).mapLeft(error -> {
                redLabelText.setText(Constantes.CREDENCIALES_INCORRECTOS);
                return null;
            });
        }).mapLeft(error -> {
            redLabelText.setText(Constantes.CREDENCIALES_INCORRECTOS);
            return null;
        });
    }


    public void signUp() {
        String username = userSignInput.getText();
        String password = passSignInput.getText();

        if (username.isEmpty() || password.isEmpty()) {
            redLabelText.setText(Constantes.CREDENCIALES_NO_PUEDEN_ESTAR_VACIAS);
            return;
        }
        Either<String, Boolean> resultado = usuarioService.crearUsuario(username, password);    

        if (resultado.isRight() && Boolean.TRUE.equals(resultado.get())) {
            greenLabelText.setText(Constantes.BIENVENID + username);
            cargarUsuarios();
        } else {
            redLabelText.setText(resultado.getLeft());
        }
    }

    public void cargarGrupos() {
        grupoList.getItems().clear();
        Either<String, List<Grupo>> gruposResult = grupoService.getGrupos();

        gruposResult.peek(grupos -> {
            if (grupos != null && !grupos.isEmpty()) {
                grupos.forEach(grupo -> grupoList.getItems().add(grupo.getNombre()));
            } else {
                redLabelText.setText(Constantes.GRUPO_NO_ENCONTRADO);
            }
        }).peekLeft(error -> redLabelText.setText(Constantes.ERROR));
    }

    public void initialize() {

        dataBase.updatePasswordsWithHash(passwordEncoder);
        greenLabelText.setText("");
        paneInicio.setVisible(true);
        cargarGrupos();
        cargarUsuarios();
        messageInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                enviarMensaje(messageInput.getText().trim(), desencriptar.getText());
                messageInput.clear();
                e.consume();
            }
        });
    }


    private void enviarMensaje(String textoMensaje, String key) {
        redLabelText.setText("");

        if (grupoActivo == null || grupoActivo.isEmpty()) {
            redLabelText.setText(Constantes.DEBES_ENTRAR_A_UN_GRUPO_PARA_ENVIAR_MENSAJES);
            return;
        }

        grupoService.getGrupos()
                .peek(grupos -> {
                    Optional<Grupo> grupoOpt = grupos.stream()
                            .filter(grupo -> grupo.getNombre().equals(grupoActivo))
                            .findFirst();

                    if (grupoOpt.isEmpty()) {
                        redLabelText.setText(Constantes.GRUPO_NO_ENCONTRADO);
                        return;
                    }

                    Grupo grupoActual = grupoOpt.get();
                    if (!grupoActual.getMiembros().contains(usuario)) {
                        redLabelText.setText(Constantes.NO_PUEDES_ENVIAR_MENSAJES_EN_EL_GRUPO + grupoActivo + Constantes.PORQUE_NO_ERES_MIEMBRO);
                        return;
                    }

                    Mensaje mensaje = new Mensaje(textoMensaje, usuario, new ArrayList<>(), grupoActual);
                    mensajeService.addMensajeToGrupo(mensaje, key)
                            .peek(msg -> chatAreaUser.getItems().add(msg))
                            .peekLeft(error -> redLabelText.setText(error));
                })
                .peekLeft(error -> redLabelText.setText(error));
    }


    private void cargarMensajesDelGrupo(String grupoName) {
        grupoService.getGrupos()
                .peek(grupos -> {
                    Optional<Grupo> grupoOpt = grupos.stream()
                            .filter(grupo -> grupo.getNombre().equals(grupoName))
                            .findFirst();

                    Either<String, Grupo> grupoEither = grupoOpt
                            .map(Either::<String, Grupo>right)
                            .orElseGet(() -> Either.left(Constantes.ERROR));

                    grupoEither
                            .flatMap(grupo -> {
                                if (grupo.getMiembros().contains(usuario)) {
                                    return mensajeService.getMensajesOfGrupo(grupoName, desencriptar.getText());
                                } else {
                                    return Either.left(Constantes.ERROR);
                                }
                            })
                            .peek(mensajes -> {
                                chatAreaUser.getItems().clear();
                                chatAreaUser.getItems().addAll(mensajes);
                            })
                            .peekLeft(error -> {
                                chatAreaUser.getItems().clear();
                                chatAreaUser.getItems().add(error);
                            });
                })
                .peekLeft(error -> {
                    chatAreaUser.getItems().clear();
                    chatAreaUser.getItems().add(error);
                });
    }


    public void addGrupo() {
        greenLabelText.setText("");
        redLabelText.setText("");

        String nombreGrupo = groupInput.getText().trim();
        String passwordGrupo = groupPassInput.getText().trim();
        boolean esPrivado = checkboxPrivado.isSelected();

        if (nombreGrupo.isEmpty() || passwordGrupo.isEmpty()) {
            redLabelText.setText(Constantes.NO_PUEDEN_ESTAR_VACIOS);
            return;
        }

        Either<String, Boolean> resultado = grupoService.addGrupo(nombreGrupo, passwordGrupo, esPrivado, usuario);

        resultado.peek(ok -> {
            grupoList.getItems().add(nombreGrupo);
            grupoActivo = nombreGrupo;
            greenLabelText.setText(Constantes.GRUPO + nombreGrupo + Constantes.REGISTRADO);
            cargarMiembrosGrupo(new Grupo(new ArrayList<>(), nombreGrupo, passwordGrupo, esPrivado, usuario));
            cargarMensajesDelGrupo(grupoActivo);
            greenLabelText.setText(Constantes.HAS_ENTRADO_AL_GRUPO + nombreGrupo);
        }).mapLeft(error -> {
            redLabelText.setText(error);
            return null;
        });
    }


    public void anyadirUsuarioAGrupo() {
        redLabelText.setText("");
        greenLabelText.setText("");
        Usuario usuarioSeleccionado = listAllUsers.getSelectionModel().getSelectedItem();
        String grupoSeleccionado = grupoList.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado == null || grupoSeleccionado == null) {
            redLabelText.setText(Constantes.DEBES_SELECCIONAR_UN_USUARIO_Y_UN_GRUPO);
            return;
        }

        grupoService.getGrupos()
                .peek(grupos -> {
                    Grupo grupoActual = grupos.stream()
                            .filter(g -> g.getNombre().equals(grupoSeleccionado))
                            .findFirst().get();
                    if (grupoActual.isPrivado() && !grupoActual.esAdmin(usuario)) {
                        redLabelText.setText(Constantes.SOLO_EL_ADMIN_PUEDE_ANYADIR_USUARIOS_A_GRUPOS_PRIVADOS);
                        return;
                    }
                    grupoActual.addMiembro(usuario, usuarioSeleccionado);
                    greenLabelText.setText(Constantes.USUARIO_ANYADIDO_AL_GRUPO);
                })
                .peekLeft(error -> redLabelText.setText(error));


                }


    public void entrarGrupo() {
        redLabelText.setText("");

        String grupoText = groupInput2.getText().trim();
        String pass = groupPassInput2.getText().trim();
        Usuario usuarioActual = usuario;

        if (grupoText.isEmpty() || pass.isEmpty()) {
            redLabelText.setText(Constantes.NO_PUEDEN_ESTAR_VACIOS);
            return;
        }
        Either<String, Boolean> result = grupoService.entrarGrupo(grupoText, pass, usuarioActual);

        result.peek(ok -> {
            grupoActivo = grupoText;
            greenLabelText.setText(Constantes.HAS_ENTRADO_AL_GRUPO + grupoText);

            Either<String, List<Grupo>> grupos = grupoService.getGrupos();

            grupos.peek(gruposList -> {
                Optional<Grupo> grupoOpt = gruposList.stream()
                        .filter(grupo -> grupo.getNombre().equals(grupoActivo))
                        .findFirst();

                if (grupoOpt.isPresent()) {
                    cargarMiembrosGrupo(grupoOpt.get());
                    cargarMensajesDelGrupo(grupoActivo);
                } else {
                    redLabelText.setText(Constantes.GRUPO_NO_ENCONTRADO);
                }
            }).peekLeft(error -> redLabelText.setText(error));

        }).peekLeft(errorMessage -> redLabelText.setText(errorMessage));
    }


    public void seleccionar() {
        redLabelText.setText("");
        String grupoSeleccionado = grupoList.getSelectionModel().getSelectedItem();

        if (grupoSeleccionado != null) {
            Either<String, List<Grupo>> gruposEither = grupoService.getGrupos();

            gruposEither.peek(grupos -> {
                Optional<Grupo> grupoOpt = grupos.stream()
                        .filter(grupo -> grupo.getNombre().equals(grupoSeleccionado))
                        .findFirst();

                if (grupoOpt.isPresent()) {
                    grupoActivo = grupoOpt.get().getNombre();
                    cargarMiembrosGrupo(grupoOpt.get());
                    cargarMensajesDelGrupo(grupoActivo);
                } else {
                    redLabelText.setText(Constantes.GRUPO_NO_ENCONTRADO);
                }
            }).peekLeft(error -> redLabelText.setText(Constantes.ERROR));
        } else {
            redLabelText.setText(Constantes.SELECCIONA_UN_GRUPO);
        }
    }

    private void cargarMiembrosGrupo(Grupo grupo) {
        listUsers.getItems().clear();

        grupo.getMiembros().forEach(user -> {
            if (!listUsers.getItems().contains(user)) {
                listUsers.getItems().add(user);
            }
        });
    }
    public void cargarUsuarios() {
        listAllUsers.getItems().clear();

        Either<String, List<Usuario>> usuariosEither = usuarioService.getUsuarios();

        usuariosEither.peek(usuarios -> listAllUsers.getItems().addAll(usuarios))
                .peekLeft(error -> redLabelText.setText(Constantes.ERROR));
    }
}
