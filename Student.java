import java.awt.*;
import java.util.*;
import javax.lang.model.util.ElementScanner6;
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

/*This class refers to any user that is non-admin (Student, Lecturers)
 * In the explanation, non-admin will be referred as a student for better clarification.
 * Student Class inherits User class: Student is a User.
 */
public class Student extends User{
    private String pwd;
    private String uid;

    Student(){} //constructor

    boolean login(){
        /*Student login function
         * This function returns true indicating successful login. (uid and password matches)
         */
        boolean status = false;
        int ans;
        Vector<String> userRecord = uvms.loadRecord("login.txt");
        
        while(true){
            while(true){
                try{
                    //Prompt student to enter a user ID and validates the input. UID cannot be null or admin.
                    uid = JOptionPane.showInputDialog(null, "Enter Username", "");
                    if(uid == null)
                        return status;
                    else if(uid.equals(""))
                        throw new NullPointerException();
                    else if(uid.toLowerCase().equals("admin"))
                        JOptionPane.showMessageDialog(null, "Cannot login as admin!", "Warning", JOptionPane.WARNING_MESSAGE);
                    else
                        break;
                }catch(NullPointerException npe){
                    JOptionPane.showMessageDialog(null, "You must enter an input!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }    

            //Checks if user exists within login.txt file.
            for(int i=0; i<userRecord.size(); i++){
                temp = new String[0];
                temp = userRecord.get(i).split("\t");
                if(temp[0].equals(uid)){
                    while(true){
                        //If UID exists, keep looping until there is a proper input or dialog is cancelled/closed
                        try{
                            pwd = JOptionPane.showInputDialog(null, "Enter Password for user " + uid, ""); //Display message and initial selection value
                            if(pwd == null) //Input dialog cancelled/closed
                                return status;
                            else if(pwd.equals("")) //input is empty
                                throw new NullPointerException();
                            else if(pwd.equals(temp[1])){ //only set login status as true if password matches
                                status = true;
                                break;
                            }
                            else
                                JOptionPane.showMessageDialog(null, "Wrong login credentials!", "Information", JOptionPane.INFORMATION_MESSAGE);
                        }catch(NullPointerException npe){
                            JOptionPane.showMessageDialog(null, "You must enter an input!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    break;
                }   
            }
            if(temp[0].equals(uid)) //if temp[0] = uid, means there is a record that matches the password. (because multiple accounts with same password may occur)
                break;

            //if there is no matching credentials, ask if the student wants to create a new account
            ans = JOptionPane.showConfirmDialog(null, "User does not exist! Do you want to create new user?", "Confirm Registration", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if(ans == JOptionPane.YES_OPTION){ //student chooses yes option. invoke register user function
                registerUser(userRecord);
                break;
            }
        }
        return status;
    }

    void registerUser(Vector<String> userRecord){
        /*This function is invoked from student login function. Creates a new user and write it into login.txt file */
        boolean exist = false;
        while(true){
            try{
                //Ask for new username input
                uid = JOptionPane.showInputDialog(null, "Register new username", "");
                if(uid == null) //Input Dialog is cancelled/closed
                    return;
                else if(uid.toLowerCase().equals("admin")) //New username cannot be admin
                    JOptionPane.showMessageDialog(null, "Username cannot be admin!", "Warning", JOptionPane.WARNING_MESSAGE);
                else if(uid.equals("")) //input is empty
                    throw new NullPointerException();
                else{
                    //check if user exists
                    for(int i=0; i<userRecord.size(); i++){
                        temp = new String[0];
                        temp = userRecord.get(i).split("\t");
                        if(uid.equals(temp[0])){
                            exist = true;
                            break;
                        }
                    }
                    
                    if(exist){ //continues to loop for new username input if current username is already taken
                        JOptionPane.showMessageDialog(null, "User already exists!", "Alert", JOptionPane.WARNING_MESSAGE);
                    }else{//if username is not taken, ask for new password
                        try{
                            pwd = JOptionPane.showInputDialog(null, "Enter password for new user " + uid + ": ", "Register New User", JOptionPane.INFORMATION_MESSAGE);
                            if(pwd == null) //Input dialog is cancelled/closed
                                return;
                            //Save uid and password into file
                            String str = new String(uid + "\t" + pwd); 
                            userRecord.addElement(str);
                            writeFile("login.txt", userRecord);
                            JOptionPane.showMessageDialog(null, "User created successfully!", "Success", JOptionPane.PLAIN_MESSAGE);
                            break;
                        }catch(IOException e){
                            JOptionPane.showMessageDialog(null, "New user creation failed.", "Warning", JOptionPane.WARNING_MESSAGE);
                        }                
                    }
                }
            }catch(NullPointerException npe){
                JOptionPane.showMessageDialog(null, "You must enter an input!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    String getUsername(){
        return uid;
    }

    void user_menu(){
        /*This function is invoked after student login is successful.
         *  It is the main menu page for students
         */
        final String fname = new String("record.txt");
        
        /*Create a display frame for the function */
        final JFrame f = gui.createViewPage("Student Dashboard");
        JLabel l1 = gui.createHeadingLabel("Welcome " + getUsername());

        //Create JButtons for user to click
        final JButton addReq = new JButton("New reservation request");
        final JButton logOut = new JButton("Log out");

        //Create a panel to place buttons horizontally
        JPanel buttonPanel = gui.createHoriPanel();
        buttonPanel.add(addReq);
        buttonPanel.add(logOut);

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
                if(e.getSource() == addReq){
                    f.dispose();
                    allRecord = uvms.loadRecord(fname);
                    addRecord();
                }
                else if(e.getSource() == logOut){
                    //Terminates frame and returns to login page (main)
                    f.dispose(); 
                    uvms.main(new String[0]);
                }
            }
        };
        addReq.addActionListener(buttonAction);
        logOut.addActionListener(buttonAction);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e){
                if(e.getValueIsAdjusting()){
                    //System.out.println(table.getValueAt(table.getSelectedRow(), 0).toString());
                    int id = Integer.parseInt(table.getValueAt(table.getSelectedRow(), 0).toString());
                    f.dispose();
                    allRecord = uvms.loadRecord(fname);
                    editRecord(id);
                }
            }
        });

    }

    void addRecord(){
        /*This function adds a new reservation REQUEST in the system.
         * Status of all new requests added by users will be set as "Pending"
         * Students are required to wait until admin approves the request, then the request will be considered as a valid record.
        */
        //Initialize temp[] and set uid for display later
        super.temp = new String[8];
        super.temp[7] = new String(uid);
        //Invoke super class method
        super.addRecord();
        return;
    }

    void editRecord(int id){
        /*This function allows the student to edit any reservation request that belongs to their user ID
         * Status of all edited request will be set to "Pending", considered as a new request
         * Editable fields are Venue ID, Start/End Date&Time, Description.
         */
        
        //Initialize temp[] and retrieve record
        super.temp = new String[0];
        super.temp = allRecord.get(id-1).split("\t");
        super.index = id; //Index of the record within the vector is saved
        super.editRecord(id);
        super.temp[5] = "Pending";
    }

    String[][] viewRecord(Vector<String> records){
        /*This function lets student to view all reservation requests available UNDER THEIR UID 
        */
        Vector<String> tempVector = new Vector<String>(); //initialize before use
        for(int i=0; i<records.size(); i++){
            temp = new String[0]; //clear content of array
            temp = records.get(i).split("\t");
            //Filter out records that belongs to the student to a vector
            if(temp[7].equals(uid)){
                tempVector.addElement(records.get(i));
            }
        }
        //Pass the filtered results to view
        return super.viewRecord(tempVector);
    }

    void deleteRecord(int input){
        /*This function allows students to cancel a reservation record (Approved/Pending requests) 
         * The function does not delete the record straightaway as the students can refer/edit their request in the future
        */
        
        temp = new String[0];
        temp = allRecord.get(input-1).split("\t");
        if(temp[5].equals("Approved") || temp[5].equals("Pending")) //Can only cancel a request if record is approved or pending
            super.deleteRecord(input);
        else
            JOptionPane.showMessageDialog(null, "Reservation is already in cancelled or denied state!", "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    boolean searchRecord(int input, Vector<String> records){
        /*This function checks if the input record ID exists and returns as boolean
         * Students may access to records under their UID only
         */
        boolean found = false;
        
        //Scan if the record id matches input (superclass method)
        for(int i=0; i<records.size(); i++){
            super.temp = new String[0];
            super.temp = records.get(i).split("\t");
            if(super.temp[7].equals(uid) && super.searchRecord(input, records)){ //return true if record id matches input & record belongs to user
                    found = true;
                    break;
            }
        }
        return found;
    }
}
