package aplicacion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * Clase principal de arranque de la aplicación JavaFX.
 * Inicializa la ventana principal (Stage), carga la vista FXML y aplica los estilos CSS avanzados.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Carga de la vista FXML
            URL fxmlLocation = getClass().getResource("/vista/MainView.fxml");
            if (fxmlLocation == null) {
                // Alternativa por si se ejecuta desde el directorio de trabajo
                File fallbackFxml = new File("src/vista/MainView.fxml");
                if (fallbackFxml.exists()) {
                    fxmlLocation = fallbackFxml.toURI().toURL();
                } else {
                    throw new IllegalStateException("No se pudo localizar el archivo vista/MainView.fxml");
                }
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // 2. Creación de la escena con dimensiones cómodas
            Scene scene = new Scene(root, 1080, 720);

            // 3. Carga y vinculación de la hoja de estilos CSS
            vincularEstilosCSS(scene);

            // 4. Configuración de la ventana principal
            primaryStage.setTitle("Sistema de Gestión de Usuarios — JavaFX & ArrayList");
            primaryStage.setMinWidth(960);
            primaryStage.setMinHeight(640);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();

            // 5. Presentación de la interfaz
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Error crítico al iniciar la aplicación JavaFX:");
            e.printStackTrace();
        }
    }

    /**
     * Vincula la hoja de estilos CSS buscando en el classpath o en la carpeta resources local.
     *
     * @param scene Escena de JavaFX donde se aplicarán los estilos.
     */
    private void vincularEstilosCSS(Scene scene) {
        // Opción A: Recursos dentro del classpath
        URL cssClasspath = getClass().getResource("/estilos.css");
        if (cssClasspath != null) {
            scene.getStylesheets().add(cssClasspath.toExternalForm());
            return;
        }

        // Opción B: Carpeta resources relativa al proyecto
        File archivoLocal = new File("resources/estilos.css");
        if (archivoLocal.exists()) {
            scene.getStylesheets().add(archivoLocal.toURI().toString());
            return;
        }

        // Opción C: Carpeta src/resources alternativa
        File archivoSrc = new File("src/resources/estilos.css");
        if (archivoSrc.exists()) {
            scene.getStylesheets().add(archivoSrc.toURI().toString());
        }
    }

    /**
     * Método main estándar de Java para iniciar el ciclo de vida de la aplicación.
     *
     * @param args Argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
