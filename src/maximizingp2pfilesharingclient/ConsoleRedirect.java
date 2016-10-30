/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package maximizingp2pfilesharingclient;

import java.io.PrintStream;
import javax.swing.JTextArea;

/**
 *
 * @author Admin
 */
public class ConsoleRedirect extends PrintStream {
      private PrintStream out;

      private JTextArea console;
      
      public ConsoleRedirect(JTextArea console,PrintStream out) {
          super(out);
          
          this.console = console;
      }

    @Override
    public PrintStream printf(String format, Object... args) {
        this.console.append(String.format(format, args));
        return this;//To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void println(Object x) {
        this.console.append(x.toString()+"\r\n");
        //super.println(x); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void println(String x) {
        this.console.append(x.toString()+"\r\n");
    }

    @Override
    public void print(Object obj) {
        this.console.append(obj.toString());
        //super.print(obj); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void print(String s) {
        this.console.append(s);
        //super.print(s); //To change body of generated methods, choose Tools | Templates.
    }
}
