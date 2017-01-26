package pkgB;

import java.awt.EventQueue;
import javax.swing.JFrame;

public class ClassB1 extends JFrame {

    public ClassB1() {

        initUI();
    }

    private void initUI() {
        
        setTitle("Simple example");
        setSize(300, 200);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {
        	ClassB1 b1 = new ClassB1();
            b1.setVisible(true);
        });
    }
}