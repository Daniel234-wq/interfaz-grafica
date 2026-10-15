package aplicacion;

/**
 * Lanzador auxiliar para entornos donde la clase principal no debe heredar directamente
 * de Application (útil para ejecutar sin configurar argumentos de máquina virtual en IDEs).
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
