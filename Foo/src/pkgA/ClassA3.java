package pkgA;

import java.awt.*;
import javax.swing.*;
 
/* FrameDemo.java requires no other files. */
public class ClassA3 {
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
	
	public ClassA3(){
		JRootPane rootPane = new JRootPane();
		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		JButton exitButton = new JButton("Exit");
		JOptionPane.showMessageDialog(null, "A basic JOptionPane message dialog");
	}
	
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("FrameDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JButton button = new JButton("OK");
        JLabel statusLabel = new JLabel("", SwingConstants.CENTER);;
 
        JLabel emptyLabel = new JLabel("");
        emptyLabel.setPreferredSize(new Dimension(175, 100));
        frame.getContentPane().add(emptyLabel, BorderLayout.CENTER);
 
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
 
}
