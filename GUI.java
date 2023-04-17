import java.awt.*;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/*This class mainly provides formatting for JComponents
        to make sure the format throughout the program is consistent.*/
public class GUI{
    private Font heading = new Font("Calibri", Font.BOLD, 24);

    public JFrame createFrame(String pageName){
        JFrame f = new JFrame(pageName);
        f.setSize(500,375);
        f.setLayout(new BorderLayout());
        //Create JFrame at center of screen (Windows)
        f.setLocationRelativeTo(null); 

        f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        //If user wants to close the frame
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                int ans = JOptionPane.showConfirmDialog(null, "Do you want to exit the program?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(ans == JOptionPane.YES_OPTION) System.exit(0);
            }
        });

        return f; 
    }

    public JLabel BottomMessage(){
        JLabel l = new JLabel("Click on any record to perform action", JLabel.CENTER);
        l.setForeground(Color.BLUE);
        l.setFont(new Font("Calibri", Font.BOLD, 18));
        return l;
    }

    public JPanel createHoriPanel(){
        JPanel p = new JPanel();
        p.removeAll();
        p.validate();
        p.setLayout(new FlowLayout());
        p.setMaximumSize(new Dimension(500,200));
        return p;
    }

    public JPanel createVertPanel(int row, int column, int hgap, int vgap){
        JPanel p = new JPanel();
        p.removeAll();
        p.validate();
        p.setLayout(new GridLayout(row, column, hgap, vgap));
        p.setMaximumSize(new Dimension(250,150));
        //p.setMaximumSize(new Dimension(420,230));
        return p;
    }

    public JPanel createBoxPanel(){
        JPanel p = new JPanel();
        p.removeAll();
        p.validate();
        p.setLayout(new FlowLayout());
        return p;
    }

    public JLabel createHeadingLabel(String text){
        JLabel l = new JLabel(text, JLabel.CENTER);
        l.setFont(heading);
        return l;
    }

    public JFrame createViewPage(String pageName){
        JFrame v = new JFrame(pageName);
        v.setSize(1000, 500);
        v.setLayout(new BorderLayout());
        v.setLocationRelativeTo(null); 

        v.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        v.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                int ans = JOptionPane.showConfirmDialog(null, "Do you want to exit the program?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if(ans == JOptionPane.YES_OPTION) System.exit(0);
            }
        });

        return v;
    }
}