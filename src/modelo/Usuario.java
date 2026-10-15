package modelo;

import java.util.Objects;

/**
 * Clase que representa a un Usuario en el sistema.
 * Contiene los atributos básicos de identificación, nombre, correo y contraseña.
 * Se utiliza para manipular la información en memoria dentro del ArrayList.
 */
public class Usuario {

    private int id;
    private String nombre;
    private String correo;
    private String contrasenia;

    /**
     * Constructor por defecto.
     */
    public Usuario() {
    }

    /**
     * Constructor con todos los atributos.
     *
     * @param id          Identificador único del usuario.
     * @param nombre      Nombre completo del usuario.
     * @param correo      Correo electrónico válido y único.
     * @param contrasenia Contraseña del usuario (mínimo 6 caracteres).
     */
    public Usuario(int id, String nombre, String correo, String contrasenia) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenia = contrasenia;
    }

    // --- GETTERS Y SETTERS ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    // --- MÉTODOS AUXILIARES ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id == usuario.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", correo='" + correo + '\'' +
                '}';
    }
}
