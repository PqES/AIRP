package airptool.jung;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

public class NewWindow {
  Display d = PlatformUI.getWorkbench().getDisplay();

  public NewWindow() {
    Shell s = new Shell(d);
    s.setSize(500, 500);
    s.open();
    //ChildShell cs = new ChildShell(s);
    while (!s.isDisposed()) {
      if (!d.readAndDispatch())
        d.sleep();
    }
    d.dispose();
  }
    /*public static void main(String[] args){
      new NewWindow();
    } */ 
}
class ChildShell {

  ChildShell(Shell parent) {
    Shell child = new Shell(parent);
    child.setSize(200, 200);
    child.open();
  }
}