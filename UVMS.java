import java.awt.*;
import java.util.*;
import org.omg.CORBA.Request;
import java.io.*;
import java.nio.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

/*This is the main file of the system.
 * Compile and run to start the GUI program.
 */
public class UVMS{
    static GUI gui = new GUI();
    public static void main(String args[]){
        //Title of the Program
        final JFrame f = gui.createFrame("University Venue Management System");
        JLabel l1 = gui.createHeadingLabel("University Venue Management System");
        f.add(l1, BorderLayout.NORTH);

        final JButton stuLogin = new JButton("Student Login");
        final JButton adminLogin = new JButton("Admin Login");
        final JButton exitProg = new JButton("Exit Program");

        //Create a panel to hold the buttons
        JPanel buttonPanel = gui.createVertPanel(3, 1, 0, 25);
        buttonPanel.add(stuLogin);
        buttonPanel.add(adminLogin);
        buttonPanel.add(exitProg);

        //Vertical boxlayout add from top to bottom
        JPanel boxPanel = gui.createBoxPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
        boxPanel.add(Box.createVerticalStrut(35));
        boxPanel.add(buttonPanel);
        f.add(boxPanel, BorderLayout.CENTER);
        f.setVisible(true);

        /*Invokes login page.
         * If login is successful, goes to respective menu
         */
        ActionListener buttonAction = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(e.getSource() == stuLogin){
                    Student u1 = new Student();
                    if(u1.login()){
                        f.dispose();
                        u1.user_menu();
                    }
                }
                else if(e.getSource() == adminLogin){
                    Staff s1 = new Staff();
                    if(s1.login()){
                        f.dispose();
                        staff_menu();
                    }
                }
                else if(e.getSource() == exitProg){
                    f.dispose(); //Terminates frame and program
                    System.exit(0);
                }
            }
        };

        stuLogin.addActionListener(buttonAction);
        adminLogin.addActionListener(buttonAction);
        exitProg.addActionListener(buttonAction);
    }

    static void staff_menu(){
        /*This function is invoked after admin login is successful.
         *  It is the main menu page for admin.
         *  This menu page contains 3 buttons: Process pending request, direct to record menu and venue menu.
         */
        final Staff s1 = new Staff();
        final JFrame f = gui.createFrame("Admin Main Menu");
        JLabel l1 = gui.createHeadingLabel("Welcome " + s1.getUsername());
        f.add(l1, BorderLayout.NORTH);

        //Create JButtons for user to click
        final JButton reqProcess = new JButton("Manage Pending Requests");
        final JButton recMenu = new JButton("Manage Records");
        final JButton venMenu = new JButton("Manage Venue");
        final JButton logOut= new JButton("Log out");

        //Create a panel to place buttons vertically
        JPanel buttonPanel = gui.createVertPanel(4, 1, 0, 30);
        buttonPanel.setMaximumSize(new Dimension(300,230));
        buttonPanel.add(reqProcess);
        buttonPanel.add(recMenu);
        buttonPanel.add(venMenu);
        buttonPanel.add(logOut);

        //Vertical boxlayout add from top to bottom
        JPanel boxPanel = gui.createBoxPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
        boxPanel.add(Box.createVerticalStrut(20));
        boxPanel.add(buttonPanel);
        f.add(boxPanel, BorderLayout.CENTER);
        f.setVisible(true);

        //When button is clicked, invoke class functions accordingly
        ActionListener buttonAction = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(e.getSource() == reqProcess){
                    f.dispose();
                    s1.processRequest();
                }else if(e.getSource() == recMenu){
                    f.dispose();
                    s1.user_menu();
                }
                else if(e.getSource() == venMenu){
                    final Venue v1 = new Venue();
                    f.dispose();
                    v1.venue_menu();
                }
                else if(e.getSource() == logOut){
                    f.dispose(); //Terminates frame and goes back to login page (main)
                    main(new String[0]);
                }
            }
        };
        reqProcess.addActionListener(buttonAction);
        recMenu.addActionListener(buttonAction);
        venMenu.addActionListener(buttonAction);
        logOut.addActionListener(buttonAction);
    }

    static int countRecord(String fname){
        /*This function calculates the total number of records in a text file 
         * This is unrelated to the record ID.
        */
        int cnt=0;
        try{
            BufferedReader reader = new BufferedReader(new FileReader(fname));
            while(reader.readLine() != null)
                cnt++;
            reader.close();
        }catch(IOException e){
            //This exception is thrown when error occurs during file reading
            JOptionPane.showMessageDialog(null, "Failed to count record.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return cnt;
    }
    
    static Vector<String> loadRecord(String fname){
        /*This function loads all records in a text file and return it as a vector of string */
        Vector<String> allRecord = new Vector<String>();
        try{
            File checkFile = new File(fname);
            /*If the given file name exists, reads the content of the file
             * if !exist, create an empty txt file with the given file name.
            */
            if(checkFile.exists()){
                FileReader r = new FileReader(fname);
                Scanner s = new Scanner(r);
                while(s.hasNextLine()){
                    allRecord.addElement(s.nextLine());
                }
                r.close();
            }
            else
                checkFile.createNewFile();
        }catch(IOException e){
            //This exception is thrown when error occurs during file reading or writing
            JOptionPane.showMessageDialog(null, "Failed to load record.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return allRecord;
    }
}
