package controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import modelo.Usuario;

import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Controlador principal de la aplicación de Gestión de Usuarios.
 * Conecta la vista FXML con el modelo de datos en memoria (ArrayList).
 * Implementa el CRUD completo y todas las validaciones de negocio.
 */
public class UsuarioController implements Initializable {

    // =========================================================================
    // ELEMENTOS INYECTADOS DESDE EL FXML
    // =========================================================================

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCorreo;
    @FXML private PasswordField txtContrasenia;
    @FXML private TextField txtBuscar;

    @FXML private Button btnGuardar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;
    @FXML private Button btnLimpiar;

    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colNombre;
    @FXML private TableColumn<Usuario, String> colCorreo;
    @FXML private TableColumn<Usuario, String> colContrasenia;

    @FXML private HBox bannerMensaje;
    @FXML private Label lblMensaje;
    @FXML private Label lblTotalUsuarios;
    @FXML private Label lblEstadoSeleccion;
    @FXML private Label lblFiltrados;

    // =========================================================================
    // COLECCIONES EN MEMORIA (SIN BASE DE DATOS NI PERSISTENCIA)
    // =========================================================================

    /**
     * Lista principal en memoria donde residen todos los objetos de tipo Usuario.
     */
    private final ArrayList<Usuario> listaUsuarios = new ArrayList<>();

    /**
     * Lista observable conectada con el TableView para reflejar cambios en la interfaz.
     */
    private final ObservableList<Usuario> usuariosObservable = FXCollections.observableArrayList();

    /**
     * Expresión regular para validar formato de correo electrónico.
     */
    private static final Pattern PATRON_CORREO =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    // =========================================================================
    // INICIALIZACIÓN
    // =========================================================================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarColumnasTabla();
        cargarDatosDeEjemplo();
        configurarSeleccionTabla();
        configurarFiltroBusqueda();
        actualizarContadores();
        sugerirSiguienteId();
    }

    /**
     * Configura el mapeo de las columnas del TableView con las propiedades del modelo Usuario.
     */
    private void configurarColumnasTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        
        // Muestra la contraseña enmascarada para privacidad o el texto plano
        colContrasenia.setCellValueFactory(new PropertyValueFactory<>("contrasenia"));
        colContrasenia.setCellFactory(column -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    // Muestra viñetas de contraseña por elegancia y seguridad
                    setText("•".repeat(Math.min(item.length(), 10)));
                }
            }
        });
    }

    /**
     * Carga usuarios iniciales en el ArrayList en memoria para contar con datos visibles al iniciar.
     */
    private void cargarDatosDeEjemplo() {
        listaUsuarios.add(new Usuario(1, "Nancy Ramírez", "nancy.ramirez@ejemplo.com", "clave123"));
        listaUsuarios.add(new Usuario(2, "Carlos Mendoza", "carlos.mendoza@ejemplo.com", "segura2026"));
        listaUsuarios.add(new Usuario(3, "Sofía Castro", "sofia.castro@ejemplo.com", "admin789"));

        usuariosObservable.setAll(listaUsuarios);
        tblUsuarios.setItems(usuariosObservable);
    }

    /**
     * Escucha la selección de filas en la tabla para cargar automáticamente los datos en el formulario.
     */
    private void configurarSeleccionTabla() {
        tblUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) {
                txtId.setText(String.valueOf(seleccionado.getId()));
                txtNombre.setText(seleccionado.getNombre());
                txtCorreo.setText(seleccionado.getCorreo());
                txtContrasenia.setText(seleccionado.getContrasenia());

                lblEstadoSeleccion.setText("Editando al usuario: " + seleccionado.getNombre() + " (ID: " + seleccionado.getId() + ")");
                ocultarBanner();
            } else {
                lblEstadoSeleccion.setText("Selecciona un registro de la tabla para editar o eliminar");
            }
        });
    }

    /**
     * Configura la búsqueda y filtrado dinámico en tiempo real sobre la tabla.
     */
    private void configurarFiltroBusqueda() {
        FilteredList<Usuario> filtro = new FilteredList<>(usuariosObservable, p -> true);

        txtBuscar.textProperty().addListener((observable, valorAnterior, nuevoValor) -> {
            filtro.setPredicate(usuario -> {
                if (nuevoValor == null || nuevoValor.trim().isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = nuevoValor.toLowerCase().trim();
                boolean coincideNombre = usuario.getNombre().toLowerCase().contains(lowerCaseFilter);
                boolean coincideCorreo = usuario.getCorreo().toLowerCase().contains(lowerCaseFilter);
                boolean coincideId = String.valueOf(usuario.getId()).contains(lowerCaseFilter);

                return coincideNombre || coincideCorreo || coincideId;
            });

            tblUsuarios.setItems(filtro);
            int cantidad = filtro.size();
            lblFiltrados.setText("Mostrando " + cantidad + " de " + listaUsuarios.size() + " registros");
        });
    }

    // =========================================================================
    // ACCIONES CRUD (CREAR, EDITAR, ELIMINAR, LIMPIAR)
    // =========================================================================

    /**
     * Acción: CREAR / REGISTRAR NUEVO USUARIO.
     * Valida campos obligatorios, formato de correo, longitud de contraseña y unicidad de ID y correo.
     */
    @FXML
    public void guardarUsuario() {
        String strId = txtId.getText().trim();
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String contrasenia = txtContrasenia.getText();

        // 1. Validación de campos no vacíos
        if (nombre.isEmpty() || correo.isEmpty() || contrasenia.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Incompletos",
                    "Por favor, completa todos los campos del formulario.",
                    TipoBanner.WARNING);
            return;
        }

        // 2. Validación de formato de correo
        if (!validarFormatoCorreo(correo)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Correo Inválido",
                    "El formato del correo electrónico no es válido. Ejemplo: usuario@dominio.com",
                    TipoBanner.WARNING);
            txtCorreo.requestFocus();
            return;
        }

        // 3. Validación de longitud de contraseña
        if (contrasenia.length() < 6) {
            mostrarAlerta(Alert.AlertType.WARNING, "Contraseña Débil",
                    "La contraseña debe tener un mínimo de 6 caracteres.",
                    TipoBanner.WARNING);
            txtContrasenia.requestFocus();
            return;
        }

        // 4. Determinación y validación del ID
        int idCalculado;
        if (strId.isEmpty()) {
            idCalculado = obtenerSiguienteId();
        } else {
            try {
                idCalculado = Integer.parseInt(strId);
                if (idCalculado <= 0) {
                    mostrarAlerta(Alert.AlertType.WARNING, "ID Inválido",
                            "El ID debe ser un número entero positivo mayor a cero.",
                            TipoBanner.WARNING);
                    txtId.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarAlerta(Alert.AlertType.WARNING, "ID Inválido",
                        "El ID ingresado debe ser un número entero válido.",
                        TipoBanner.WARNING);
                txtId.requestFocus();
                return;
            }
        }

        // 5. Validación de ID único en memoria
        if (existeId(idCalculado)) {
            mostrarAlerta(Alert.AlertType.ERROR, "ID Duplicado",
                    "Ya existe un usuario con el ID: " + idCalculado + ". Por favor, utiliza otro ID.",
                    TipoBanner.DANGER);
            txtId.requestFocus();
            return;
        }

        // 6. Validación de correo electrónico único en memoria
        if (existeCorreo(correo, -1)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Correo Duplicado",
                    "Ya existe un usuario registrado con el correo: " + correo,
                    TipoBanner.DANGER);
            txtCorreo.requestFocus();
            return;
        }

        // Creación del nuevo usuario y almacenamiento en ArrayList
        Usuario nuevoUsuario = new Usuario(idCalculado, nombre, correo, contrasenia);
        listaUsuarios.add(nuevoUsuario);

        // Sincronización con la interfaz gráfica
        sincronizarTabla();
        actualizarContadores();
        limpiarCamposFormulario();
        sugerirSiguienteId();

        mostrarNotificacionBanner("✅ Usuario registrado con éxito (ID: " + idCalculado + ")", TipoBanner.SUCCESS);
    }

    /**
     * Acción: MODIFICAR / ACTUALIZAR USUARIO SELECCIONADO.
     * Requiere que el usuario haya seleccionado una fila de la tabla.
     */
    @FXML
    public void modificarUsuario() {
        Usuario usuarioSeleccionado = tblUsuarios.getSelectionModel().getSelectedItem();

        // Validación: debe haber una fila seleccionada
        if (usuarioSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin Selección",
                    "Debes seleccionar un usuario de la tabla para poder modificarlo.",
                    TipoBanner.WARNING);
            return;
        }

        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String contrasenia = txtContrasenia.getText();

        // 1. Validación de campos no vacíos
        if (nombre.isEmpty() || correo.isEmpty() || contrasenia.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos Incompletos",
                    "Todos los campos deben estar llenos para realizar la actualización.",
                    TipoBanner.WARNING);
            return;
        }

        // 2. Validación de formato de correo
        if (!validarFormatoCorreo(correo)) {
            mostrarAlerta(Alert.AlertType.WARNING, "Correo Inválido",
                    "El formato del correo no es correcto. Ejemplo: usuario@dominio.com",
                    TipoBanner.WARNING);
            txtCorreo.requestFocus();
            return;
        }

        // 3. Validación de longitud de contraseña
        if (contrasenia.length() < 6) {
            mostrarAlerta(Alert.AlertType.WARNING, "Contraseña Débil",
                    "La contraseña debe contener al menos 6 caracteres.",
                    TipoBanner.WARNING);
            txtContrasenia.requestFocus();
            return;
        }

        // 4. Validación de correo único (excluyendo el usuario actual)
        if (existeCorreo(correo, usuarioSeleccionado.getId())) {
            mostrarAlerta(Alert.AlertType.ERROR, "Correo en Uso",
                    "El correo '" + correo + "' ya está registrado por otro usuario.",
                    TipoBanner.DANGER);
            txtCorreo.requestFocus();
            return;
        }

        // Actualización de los datos en el objeto del ArrayList
        usuarioSeleccionado.setNombre(nombre);
        usuarioSeleccionado.setCorreo(correo);
        usuarioSeleccionado.setContrasenia(contrasenia);

        // Refresco de la tabla y controles
        tblUsuarios.refresh();
        limpiarCamposFormulario();
        tblUsuarios.getSelectionModel().clearSelection();
        sugerirSiguienteId();

        mostrarNotificacionBanner("✏️ Usuario con ID " + usuarioSeleccionado.getId() + " actualizado correctamente.", TipoBanner.SUCCESS);
    }

    /**
     * Acción: ELIMINAR USUARIO SELECCIONADO.
     * Solicita confirmación explícita mediante un cuadro de diálogo antes de remover del ArrayList.
     */
    @FXML
    public void eliminarUsuario() {
        Usuario usuarioSeleccionado = tblUsuarios.getSelectionModel().getSelectedItem();

        // Validación: debe haber una fila seleccionada
        if (usuarioSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin Selección",
                    "Debes seleccionar un usuario de la tabla para poder eliminarlo.",
                    TipoBanner.WARNING);
            return;
        }

        // Diálogo de confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Estás seguro de eliminar al usuario?");
        confirmacion.setContentText("ID: " + usuarioSeleccionado.getId() + "\nNombre: " +
                usuarioSeleccionado.getNombre() + "\nCorreo: " + usuarioSeleccionado.getCorreo() +
                "\n\nEsta acción no se puede deshacer.");

        // Personalización de botones del diálogo
        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            int idEliminado = usuarioSeleccionado.getId();

            // Remoción de la lista en memoria (ArrayList)
            listaUsuarios.remove(usuarioSeleccionado);

            // Sincronización y actualización
            sincronizarTabla();
            actualizarContadores();
            limpiarCamposFormulario();
            tblUsuarios.getSelectionModel().clearSelection();
            sugerirSiguienteId();

            mostrarNotificacionBanner("🗑️ Usuario con ID " + idEliminado + " eliminado correctamente.", TipoBanner.DANGER);
        }
    }

    /**
     * Acción: LIMPIAR FORMULARIO.
     * Restablece los campos de texto, cancela la selección de la tabla y sugiere el próximo ID disponible.
     */
    @FXML
    public void limpiarFormulario() {
        limpiarCamposFormulario();
        tblUsuarios.getSelectionModel().clearSelection();
        sugerirSiguienteId();
        mostrarNotificacionBanner("🧹 Formulario listo para un nuevo registro.", TipoBanner.INFO);
    }

    // =========================================================================
    // MÉTODOS DE VALIDACIÓN Y APOYO EN MEMORIA
    // =========================================================================

    /**
     * Valida si una cadena cumple con el formato de correo electrónico.
     */
    private boolean validarFormatoCorreo(String correo) {
        return correo != null && PATRON_CORREO.matcher(correo).matches();
    }

    /**
     * Verifica si un ID ya existe en el ArrayList en memoria.
     */
    private boolean existeId(int id) {
        for (Usuario u : listaUsuarios) {
            if (u.getId() == id) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica si un correo ya está registrado en memoria por algún usuario distinto al idExcluir.
     *
     * @param correo    Correo a verificar.
     * @param idExcluir ID que se ignora (útil para modificación). Usar -1 al crear uno nuevo.
     */
    private boolean existeCorreo(String correo, int idExcluir) {
        for (Usuario u : listaUsuarios) {
            if (u.getId() != idExcluir && u.getCorreo().equalsIgnoreCase(correo)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calcula el próximo identificador ID disponible basándose en el máximo ID actual del ArrayList.
     */
    private int obtenerSiguienteId() {
        int maxId = 0;
        for (Usuario u : listaUsuarios) {
            if (u.getId() > maxId) {
                maxId = u.getId();
            }
        }
        return maxId + 1;
    }

    /**
     * Coloca en el campo txtId el próximo ID sugerido.
     */
    private void sugerirSiguienteId() {
        txtId.setText(String.valueOf(obtenerSiguienteId()));
    }

    /**
     * Sincroniza la lista observable de JavaFX con el ArrayList en memoria.
     */
    private void sincronizarTabla() {
        usuariosObservable.setAll(listaUsuarios);
    }

    /**
     * Limpia los campos de texto del formulario.
     */
    private void limpiarCamposFormulario() {
        txtId.clear();
        txtNombre.clear();
        txtCorreo.clear();
        txtContrasenia.clear();
    }

    /**
     * Actualiza la etiqueta del encabezado con el total de usuarios en memoria.
     */
    private void actualizarContadores() {
        int total = listaUsuarios.size();
        lblTotalUsuarios.setText("📊 " + total + (total == 1 ? " Usuario Registrado" : " Usuarios Registrados"));
        lblFiltrados.setText("Mostrando " + total + " registros");
    }

    // =========================================================================
    // GESTIÓN DE NOTIFICACIONES Y ALERTAS
    // =========================================================================

    public enum TipoBanner {
        SUCCESS("alert-success"),
        DANGER("alert-danger"),
        WARNING("alert-warning"),
        INFO("alert-info");

        private final String estiloClase;

        TipoBanner(String estiloClase) {
            this.estiloClase = estiloClase;
        }

        public String getEstiloClase() {
            return estiloClase;
        }
    }

    /**
     * Muestra un banner decorativo y elegante dentro de la propia tarjeta del formulario.
     */
    private void mostrarNotificacionBanner(String mensaje, TipoBanner tipo) {
        bannerMensaje.getStyleClass().removeAll("alert-success", "alert-danger", "alert-warning", "alert-info");
        bannerMensaje.getStyleClass().add(tipo.getEstiloClase());
        lblMensaje.setText(mensaje);
        bannerMensaje.setVisible(true);
        bannerMensaje.setManaged(true);
    }

    private void ocultarBanner() {
        bannerMensaje.setVisible(false);
        bannerMensaje.setManaged(false);
    }

    /**
     * Presenta un cuadro de diálogo emergente estilizado para advertencias y errores.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje, TipoBanner tipoBanner) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        // También mostramos el banner integrado para doble visibilidad y armonía visual
        mostrarNotificacionBanner(mensaje, tipoBanner);

        alerta.showAndWait();
    }
}
