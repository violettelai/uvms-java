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

/*This class refers to Admin.
 * Staff class inherits User class: Admin is a User.
 */
public class Staff extends User{
    private String pwd; //username = user id
    private String uid = new String("Admin");
    
    Staff(){} //constructor

    boolean login(){
        /*Staff login function
         * This function returns true indicating successfully login (password is correct)
         */
        boolean status = false;
        Vector<String> userRecord = uvms.loadRecord("login.txt");

        while(true){ //loop until password is correct
            try{
                pwd = JOptionPane.showInputDialog(null, "Enter Admin Password", "");
                if(pwd == null) //Input dialog cancelled/closed
                    return status;
                else if(pwd.equals("")) //input is empty
                    throw new NullPointerException();
                else{
                    temp = new String[0];
                    temp = userRecord.get(0).split("\t"); //admin will always be 1st record (index 0)
                    if(pwd.equals(temp[1])){
                        status = true;
                        break; //return true and break from loop if password is correct
                    }
                }
                JOptionPane.showMessageDialog(null, "Wrong password!", "Information", JOptionPane.INFORMATION_MESSAGE);
            }catch(NullPointerException npe){
                JOptionPane.showMessageDialog(null, "You must enter an input!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return status;
    }

    String getUsername(){
        return uid;
    }

    void user_menu(){
        /*This function is invoked after staff login is successful.
         *  It is the main menu page for admin
         */
        final String fname = new String("record.txt");
        
        /*Create a display frame for the function */
        final JFrame f = gui.createViewPage("Admin Dashboard");
        JLabel l1 = gui.createHeadingLabel("Welcome " + getUsername());

        //Create JButtons for user to click
        final JButton addRec = new JButton("Add new record");
        final JButton exitPage = new JButton("Return to main menu");

        //Create a panel to place buttons horizontally
        JPanel buttonPanel = gui.createHoriPanel();
        buttonPanel.add(addRec);
        buttonPanel.add(exitPage);

        //A Panel to hold items on north
        JPanel NorthPanel = gui.createVertPanel(2,1,0,0);
        NorthPanel.add(l1);
        NorthPanel.add(buttonPanel);

        Vector<String> records = uvms.loadRecord(fname);
        String data[][] = viewRecord(records);

        //Create a table to place the data.
        final JTable table = new JTable(data, fieldColumn);
        JScrollPane sp = new JScrollPane(table);
        table.setDefaultEditor(Object.class, null);
        table.setAutoCreateRowSorter(true);

        //Set table alignment to center
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        TableModel tableModel = table.getModel();
        for (int i=0; i<tableModel.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(centerRender);
    
        f.add(sp, BorderLayout.CENTER);
        f.add(NorthPanel, BorderLayout.NORTH);

        //Display alert message at the bottom
        JPanel msgPanel = gui.createHoriPanel();
        JLabel instruction = gui.BottomMessage();
        msgPanel.add(instruction);
        f.add(msgPanel, BorderLayout.SOUTH);
        f.setVisible(true);
        
        //When button is clicked, invoke class functions accordingly
        ActionListener buttonAction = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(e.getSource() == addRec){
                    f.dispose();
                    allRecord = uvms.loadRecord(fname);
                    addRecord();
                }
                else if(e.getSource() == exitPage){
                    //Terminates frame and returns to login page (main)
                    f.dispose(); 
                    uvms.staff_menu();
                }
            }
        };
        addRec.addActionListener(buttonAction);
        exitPage.addActionListener(buttonAction);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e){
                if(e.getValueIsAdjusting()){
                    int id = Integer.parseInt(table.getValueAt(table.getSelectedRow(), 0).toString());
                    f.dispose();
                    allRecord = uvms.loadRecord(fname);
                    editRecord(id);
                }
            }
        });

    }

    void addRecord(){
        /*This function adds a new reservation RECORD in the system.
         * Status of all new records added by admin will be set as "Approved"
         */
        //Initialize temp[] and set uid for display later
        super.temp = new String[8];
        super.temp[7] = new String(uid);
        //Invoke superclass method
        super.addRecord();
        super.temp[5] = new String("Approved");
        return;
    }

    void editRecord(int id){
        /*This function allows the admin to edit a reservation record.
         * Editable fields are Venue ID, Start/End Date&Time, Description.
         * Status will be automatically set as "Approved" for editted records
        */
        
        //Initialize temp[] and retrieve records.
        super.temp = new String[0];
        super.temp = allRecord.get(id-1).split("\t");
        super.index = id; //Index of the record within the vector is saved 
        super.editRecord(id);
        super.temp[5] = "Approved";
    }

    String[][] viewRecord(Vector<String> records){
        /*This function lets admin view all reservation records available in the system.
         * No condition needs to be met for admin. (Default superclass function)
         */
        return super.viewRecord(records);
    }

    void deleteRecord(int input){
        /*This function allows admin to switch a record status to cancel.
         * The function does not delete the record straightaway as the students need to see that the record is cancelled (not disappear)
         */
        
        temp = new String[0];
        temp = allRecord.get(input-1).split("\t");
        if(temp[5].equals("Approved") || temp[5].equals("Pending")) //Can only cancel a record if status is approved or pending
            super.deleteRecord(input);
        else
            JOptionPane.showMessageDialog(null, "Reservation is already in cancelled or denied state!", "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    boolean searchRecord(int input, Vector<String> records){
        /*This function checks if the input record ID exists and returns it as a boolean 
         * Admin may search/access all record without limitations
        */
        boolean found = false;
        //Thus, admin uses default search function (superclass method)
        for(int i=0; i<records.size(); i++){
            super.temp = new String[0];
            super.temp = records.get(i).split("\t");
            if(super.searchRecord(input, records)){
                    found = true;
                    break;
            }
        }
        return found;
    }

    void processRequest(){
        /*This function allows admin to approve/deny pending requests made by students */
        allRecord = uvms.loadRecord("record.txt");
        Vector<String> tempVector = new Vector<String>();

        //Filter & save all pending request in a temporary vector
        for(int i=0; i<allRecord.size(); i++){ 
            temp = new String[0];
            temp = allRecord.get(i).split("\t");
            if(temp[5].equals("Pending"))
                tempVector.addElement(allRecord.get(i));
        }

        //Lets admin view all filtered records (pending requests)
        if(tempVector.size()==0){
            JOptionPane.showMessageDialog(null, "No pending requests.", "Information", JOptionPane.INFORMATION_MESSAGE);
            uvms.staff_menu();
            return;
        }

        final JFrame f = gui.createViewPage("Manage Pending Requests");
        JLabel l1 = gui.createHeadingLabel("Welcome " + getUsername());

        //Create JButtons for user to click
        final JButton exitPage = new JButton("Return to main menu");
        JPanel buttonPanel = gui.createHoriPanel();
        buttonPanel.add(exitPage);

        //Vertical boxlayout add from top to bottom
        JPanel NorthPanel = gui.createVertPanel(2,1,0,0);
        NorthPanel.add(l1);
        NorthPanel.add(buttonPanel);

        String data[][] = viewRecord(tempVector);
        final JTable table = new JTable(data, fieldColumn);
        JScrollPane sp = new JScrollPane(table);
        table.setDefaultEditor(Object.class, null);

        //Set table alignment to center
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        TableModel tableModel = table.getModel();
        for (int i=0; i<tableModel.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(centerRender);
        f.add(sp, BorderLayout.CENTER);
        f.add(NorthPanel, BorderLayout.NORTH);

        //Display alert message at the bottom
        JPanel msgPanel = gui.createHoriPanel();
        JLabel instruction = gui.BottomMessage();
        msgPanel.add(instruction);
        f.add(msgPanel, BorderLayout.SOUTH);
        f.setVisible(true);
        
        //When button is clicked, invoke class functions accordingly
        ActionListener buttonAction = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(e.getSource() == exitPage){
                    //Terminates frame and returns to login page (main)
                    f.dispose(); 
                    uvms.staff_menu();
                }
            }
        };
        exitPage.addActionListener(buttonAction);

        //Invoke approveReq() if user clicks on a record
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e){
                if(e.getValueIsAdjusting()){
                    int id = Integer.parseInt(table.getValueAt(table.getSelectedRow(), 0).toString());
                    approveReq(id);
                    f.dispose();
                    processRequest();
                }
            }
        });
    }

    void approveReq(int id){
        int ans;
        String str = new String();

        ans = JOptionPane.showConfirmDialog(null, "Do you approve this request?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        temp = new String[0];
        temp = allRecord.get(id-1).split("\t");

        if(ans == JOptionPane.CLOSED_OPTION)
            return; //Dialog is closed
        else if(ans == JOptionPane.YES_OPTION){
            //Run check clash if wants to approve record. No changes if date clashed
            ArrayList<String[]> checkList = new ArrayList<String[]>();
            checkList = loadCheckList(temp[1], 0);
            if(dateValid(temp[2]) && dateValid(temp[3])){
                if(startClash(checkList, temp[2]) || endClash(checkList, temp[2], temp[3]) || overlapClash(checkList, temp[2], temp[3])){
                    return;
                } else temp[5] = "Approved";
            }
        }    
        else //NO option
            temp[5] = "Denied";
    
        //Update Vector
        for(int i=0; i<temp.length; i++)
            str += temp[i] + "\t";
        allRecord.set(id-1, str);
        //Update File
        try{
            writeFile("record.txt", allRecord);
            JOptionPane.showMessageDialog(null, "Record has been updated successfully!", "Success", JOptionPane.PLAIN_MESSAGE);
        }catch(IOException ioe){
            JOptionPane.showMessageDialog(null, "Record update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
