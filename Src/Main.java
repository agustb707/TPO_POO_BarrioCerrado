import Controlador.ControladorBarrio;
import Vista.VentanaPrincipal;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new VentanaPrincipal(new ControladorBarrio()).setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo abrir el sistema: " + e.getMessage() +
                                "\nSe conservaron los datos existentes.",
                        "Los Aromos - Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
