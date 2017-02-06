package pkgA;

import java.awt.AWTException;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.sql.SQLData;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

public class ClassA1 {
	
	private static Calendar calendar = Calendar.getInstance(Locale.US) ;
	public static String fullPattern = "yyyy/MMM/dd HH:mm:ss.SSS";

	public ClassA1(){
		
	}
	
	public int mA1(Date date) throws AWTException{
		calendar.clear() ;
        calendar.setTime(date) ;
        
        JOptionPane.showMessageDialog(null, "A basic JOptionPane message dialog");
        JFrame frame = new JFrame("JOptionPane showMessageDialog example");
        Robot robot = new Robot();
        robot.delay(40);
        java.util.ArrayList<SQLData> Ar = new java.util.ArrayList<SQLData>();
        
        return calendar.get(Calendar.DAY_OF_MONTH);
        
	}
	
	public void mA1_2(String pattern){
		DateFormat df = new SimpleDateFormat(pattern);
		boolean verifica = true;
		int i = 0;
		
		if(verifica){
			JLabel statusLabel = new JLabel("", SwingConstants.CENTER);;
			JButton okButton = new JButton("OK");
			JButton submitButton = new JButton("Submit");
			ArrayList<JPanel> arrayControlPanel = new ArrayList<JPanel>();
		}
		else{
			JRootPane rootPane = new JRootPane();
			InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
			JButton exitButton = new JButton("Exit"); 
		}
		
		if(i==0){
			ArrayList<Long> array = new ArrayList<Long>();
			long x =3;
			array.add(x);
		}
	}
	
	public void mA1_3(){
		for(int i=0; i<10; i++){
			JLabel statusLabel = new JLabel("", SwingConstants.CENTER);;
			JPanel controlPanel = new JPanel();
			JButton okButton = new JButton("OK");
			
			for(int j=0;j<5;j++){
				JRootPane rootPane = new JRootPane();
				InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
				JButton exitButton = new JButton("Exit");
				
				if(j==0){
					Point p = new Point(400, 400);
					KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
					
					if(j==1){
						Action action = new AbstractAction(){
	
							@Override
							public void actionPerformed(ActionEvent e) {
								int x=0;
								
								if(true){
									double d= 0;
									char c = 'a';
									byte b = 1;
									short s = 'a';
									float f= 0;
								}
								
								
								while(x<5){
									java.lang.Boolean Bo=false;
									java.lang.Character C= 'a';
									java.lang.Byte By=0;
									java.lang.Short Sh='a';
									java.lang.Integer I=3;
									java.lang.Long L = 123456789L;
									java.lang.Float F= 12345667F;
									java.lang.Double d = 5.0;
									java.lang.String St= "z";
									java.lang.Object o = new java.lang.Object();
									x++;
								}
								
							}};
					}
				}
				
				if(j==2){
					JTextField jtfInput = new JTextField(20);
					JTextArea jtAreaOutput;
					GridBagLayout gridBag = new GridBagLayout();
				}
			}
			
			if(i==1){
				JTextArea jtAreaOutput = new JTextArea(5, 20);
				JScrollPane scrollPane = new JScrollPane(jtAreaOutput,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
				JPopupMenu popup = new JPopupMenu();
			}		
		}	
	}
		
	
	public void mA1_4(){
		JMenuItem item = new JMenuItem();
		JFrame frame = new JFrame("Example");
		java.lang.Boolean[] Bo = new java.lang.Boolean[5];
		java.lang.Boolean[][] BBo = new java.lang.Boolean[5][5];
		java.lang.Character[] Ch = new java.lang.Character[5];
		java.lang.Character[][] CCh = new java.lang.Character[5][5];
		java.lang.Byte[] By = new java.lang.Byte[5];
		java.lang.Byte[][] BBy = new java.lang.Byte[5][5];
		java.lang.Short[] Sh = new java.lang.Short[5];
		java.lang.Short[][] SSh = new java.lang.Short[5][5];
		java.lang.Integer[] In= new java.lang.Integer[5];
		java.lang.Integer[][] IIn = new java.lang.Integer[5][5];
		java.lang.Long[] Lo = new java.lang.Long[5];
		java.lang.Long[][] LLo = new java.lang.Long[5][5];
		java.lang.Float[] Fl = new java.lang.Float[5]; 
		java.lang.Float[][] FFl = new java.lang.Float[5][5];
		java.lang.Double[] Do = new java.lang.Double[5];
		java.lang.Double[][] DDo = new java.lang.Double[5][5];
		java.lang.String[] St= new java.lang.String[5];
		java.lang.String[][] SSt = new java.lang.String[5][5];
		java.lang.Object[] Ob = new java.lang.Object[5];
		java.lang.Object[][] OOb = new java.lang.Object[5][5];
		java.util.ArrayList<Double> Ar1 = new java.util.ArrayList<Double>(); 
		java.util.ArrayList<Integer>[] Ar2 = new java.util.ArrayList[5]; 
		java.util.ArrayList<String>[][] AAr= new java.util.ArrayList[5][5];
	}
	
	public void mA1_5(){
		for(int i=0; i<5; i++){
			
		}
	}
	
}
