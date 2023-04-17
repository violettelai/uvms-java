import java.awt.*;
import java.util.*; //java.util.Scanner = take input
import org.omg.CORBA.Request;
import java.io.*;
import java.nio.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.awt.event.*;
import javax.lang.model.util.ElementScanner6;
import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.event.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

/*This class allows admin to manage and manipulate all available venue information */
public class Venue{
    protected String[] temp;
    protected Vector<String> allRecord;
    private boolean recStatus;
    private Font fieldFont = new Font("Ariel", Font.BOLD, 14);
    private Font recFont = new Font("Calibri", Font.PLAIN, 14);
    private String[] fieldOption = {"Venue ID", "Venue Name", "Venue Type", "Description"};
    private String[] venueType = {"Tutorial Room", "Lecture Room", "Lecture Hall", "Laboratory"};
    private GUI gui = new GUI();
    private UVMS uvms = new UVMS();

    Vector<String> getVector(){
        return allRecord;
    }

    Vector<String> getAvailableVenue(){
        /*This function loads all available Venue ID and returns it as a vector */
        Vector<String> venueList = new Vector<String>();

        //Read each record and store the Venue ID in venue List
        Vector<String> temp2 = uvms.loadRecord("venue.txt");

        //Indicate there is no available venue if size is 0
        if(temp2.size()==0)
            venueList.addElement("null");
        else{
            //Push only venue ID into the vector
            for(int i=0; i<temp2.size(); i++){
                String[] strTemp = new String[0];
                strTemp = temp2.get(i).split("\t");
                venueList.addElement(strTemp[1]);
            }
        }
    
        return venueList;
    }

    void venue_menu(){
        /*
         *  It is the main menu page for venue
         */
        final String fname = new String("venue.txt");
        final Venue v1 = new Venue();
        
        /*Create a display frame for the function */
        final JFrame f = gui.createViewPage("Admin Dashboard");
        JLabel l1 = gui.createHeadingLabel("Manage Venue");
        f.add(l1, BorderLayout.NORTH);

        //Create JButtons for user to click
        final JButton addVen = new JButton("Add new venue");
        final JButton exitPage = new JButton("Return to main menu");

        //Create a panel to place buttons horizontally
        JPanel buttonPanel = gui.createHoriPanel();
        buttonPanel.add(addVen);
        buttonPanel.add(exitPage);

        //A Panel to hold items on north
        JPanel NorthPanel = gui.createVertPanel(2,1,0,0);
        NorthPanel.add(l1);
        NorthPanel.add(buttonPanel);

        Vector<String> records = uvms.loadRecord(fname);
        String data[][] = viewRecord(records);

        //Create a table to place the data
        String column[] = {"Venue ID", "Name", "Venue Type", "Description"};
        final JTable table = new JTable(data, column);
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
                if(e.getSource() == addVen){
                    f.dispose();
                    addRecord(uvms.loadRecord(fname));
                }
                else if(e.getSource() == exitPage){
                    //Terminates frame and returns to login page (main)
                    f.dispose(); 
                    uvms.staff_menu();
                }
            }
        };
        addVen.addActionListener(buttonAction);
        exitPage.addActionListener(buttonAction);


        table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e){
                if(e.getValueIsAdjusting()){
                    String id = table.getValueAt(table.getSelectedRow(), 0).toString();
                    f.dispose();
                    editRecord(uvms.loadRecord(fname), id);
                }
            }
        });

    }
    
    public void addRecord(final Vector<String> records){
        /*This function adds a new venue record in the system. */
        int cnt = uvms.countRecord("venue.txt");
        temp = new String[5];
        temp[0] = Integer.toString(cnt+1);

        /*Create JComponents for Columns*/
        JComponent[] ColumnData = new JComponent[4];
        //Right column - data
        final JTextField blockID = new JTextField();
        final JTextField venueName = new JTextField();
        final JComboBox cbox = new JComboBox(venueType);
        cbox.setFont(recFont);
        final JTextField descrp = new JTextField();

        //Create Jlabels for left column - fields
        for(int j=0; j<4; j++){
            ColumnData[j] = new JLabel(fieldOption[j], JLabel.RIGHT);
        }
        
        //Display New Frame
        final JFrame f = gui.createFrame("New Venue Record");
        
        //Add Component into Columns
        JPanel p = gui.createVertPanel(4,2,5,10);
        p.setPreferredSize(new Dimension(275,170));
        p.add(ColumnData[0]); p.add(blockID);
        p.add(ColumnData[1]); p.add(venueName);
        p.add(ColumnData[2]); p.add(cbox);
        p.add(ColumnData[3]); p.add(descrp);

        //Add buttons for users
        final JButton saveRec = new JButton("Add");
        final JButton cancelAdd = new JButton("Cancel");
        JPanel buttonPanel = gui.createHoriPanel();
        buttonPanel.add(saveRec);
        buttonPanel.add(cancelAdd);

        JPanel boxPanel = gui.createBoxPanel();
        boxPanel.add(p);

        JPanel formatSpaceVert = new JPanel();
        formatSpaceVert.setLayout(new BoxLayout(formatSpaceVert, BoxLayout.X_AXIS));
        formatSpaceVert.add(Box.createVerticalStrut(50));

        JPanel formatSpaceHori = new JPanel();
        formatSpaceHori.setLayout(new BoxLayout(formatSpaceHori, BoxLayout.X_AXIS));
        formatSpaceHori.add(Box.createHorizontalStrut(40));

        f.add(formatSpaceVert, BorderLayout.NORTH);
        f.add(formatSpaceHori, BorderLayout.EAST);
        f.add(boxPanel, BorderLayout.CENTER);
        f.add(buttonPanel, BorderLayout.SOUTH);
        f.setVisible(true);

        final int tempindex = cnt+1;
        ActionListener buttonAction = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(e.getSource() == saveRec){
                    /*User wants to save a new record. Retrieve entered strings in JTextfield and save it in temp[] */
                    temp[1] = blockID.getText();
                    //check if venue ID already exists
                    boolean checkExist = false;
                    for(int i=0; i<tempindex-1; i++){
                        String[] temp2 = new String[5];
                        temp2 = records.get(i).split("\t");
                        if(temp[1].equals(temp2[1])){
                            checkExist = true;
                            break;
                        }
                    }
                    if(!checkExist){ //if Venue ID does not exist, retrieve other fields and save
                        temp[2] = venueName.getText();
                        //Venue Type is a dropdown box (JComboBox)
                        if(cbox.getSelectedIndex()==0)
                            temp[3] = "Tutorial Room";
                        else if(cbox.getSelectedIndex()==1)
                            temp[3] = "Lecture Room";
                        else if(cbox.getSelectedIndex()==2)
                            temp[3] = "Lecture Hall";
                        else
                            temp[3] = "Laboratory";
                        temp[4] = descrp.getText();
                        //Update file
                        recStatus = true;
                        updateRec(records, tempindex);
                        f.dispose();
                        venue_menu();
                    }else
                        JOptionPane.showMessageDialog(null, "ID already exists!", "Warning", JOptionPane.WARNING_MESSAGE);
                }else if(e.getSource() == cancelAdd){
                    //Add Process is cancelled
                    recStatus = false;
                    f.dispose();
                    venue_menu();
                }
            }
        };
        saveRec.addActionListener(buttonAction);
        cancelAdd.addActionListener(buttonAction);

        allRecord = records;
        return;
    }

    public void editRecord(final Vector<String> records, final String id){
        /*This function allows admin to edit venue information.
         * Editable fields are venue name, venue type, description
         */
        int index = -1;

        //Check if record exists and saves the index of record within the vector
        for(int i=0; i<records.size(); i++){
            temp = new String[0];
            temp = records.get(i).split("\t");
            if(temp[1].equals(id)){
                index = i; //save index to replace the record (in vector) after editing
                break;
            }
        }
        
        JComponent[] ColumnData = new JComponent[4];

        //Create JComponents for right column (data)
        final JLabel blockID = new JLabel(temp[1], JLabel.CENTER);
        blockID.setFont(recFont);
        final JTextField venueName = new JTextField(temp[2]);
        final JComboBox cbox = new JComboBox(venueType);
        cbox.setFont(recFont);
        cbox.setSelectedItem(temp[3]);
        final JTextField descrp = new JTextField(temp[4]);
        //Create JComponents for left column (field)
        for(int j=0; j<4; j++){
            ColumnData[j] = new JLabel(fieldOption[j], JLabel.RIGHT);
        }
                        
        //Display Edit Frame
        final JFrame editf = gui.createFrame("Edit Record");
        
        JPanel p = gui.createVertPanel(4,2,5,10);
        p.setPreferredSize(new Dimension(275,170));
        //Add Component into Columns
        p.add(ColumnData[0]);
        p.add(blockID);
        p.add(ColumnData[1]);
        p.add(venueName);
        p.add(ColumnData[2]);
        p.add(cbox);
        p.add(ColumnData[3]);
        p.add(descrp);

        //Create Buttons
        final JButton saveEdit = new JButton("Save");
        final JButton delVen = new JButton("Delete");
        final JButton cancelEdit = new JButton("Cancel");
        JPanel buttonPanel = gui.createHoriPanel();
        buttonPanel.add(saveEdit);
        buttonPanel.add(delVen);
        buttonPanel.add(cancelEdit);

        JPanel boxPanel = gui.createBoxPanel();
        boxPanel.add(p);

        JPanel formatSpaceVert = new JPanel();
        formatSpaceVert.setLayout(new BoxLayout(formatSpaceVert, BoxLayout.X_AXIS));
        formatSpaceVert.add(Box.createVerticalStrut(50));

        JPanel formatSpaceHori = new JPanel();
        formatSpaceHori.setLayout(new BoxLayout(formatSpaceHori, BoxLayout.X_AXIS));
        formatSpaceHori.add(Box.createHorizontalStrut(40));

        editf.add(formatSpaceVert, BorderLayout.NORTH);
        editf.add(formatSpaceHori, BorderLayout.EAST);
        editf.add(boxPanel, BorderLayout.CENTER);
        editf.add(buttonPanel, BorderLayout.SOUTH);
        editf.setVisible(true);
                        
        final int tempindex = index;
        ActionListener buttonAction = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(e.getSource() == saveEdit){
                    /*User wants to save edit. Retrieve strings in the JTextfield and transfer into temp[] */
                    temp[2] = venueName.getText();
                    //Venue Type is a dropdown list (JComboBox)
                    if(cbox.getSelectedIndex()==0)
                        temp[3] = "Tutorial Room";
                    else if(cbox.getSelectedIndex()==1)
                        temp[3] = "Lecture Room";
                    else if(cbox.getSelectedIndex()==2)
                        temp[3] = "Lecture Hall";
                    else
                        temp[3] = "Laboratory";
                    temp[4] = descrp.getText();

                    //Update file
                    recStatus = true;
                    updateRec(records, tempindex);
                    editf.dispose();
                    venue_menu();
                }else if(e.getSource() == delVen){
                    deleteRecord(records, id);
                    editf.dispose();
                    venue_menu();
                }else if(e.getSource() == cancelEdit){
                    recStatus = false;
                    editf.dispose();
                    venue_menu();
                }
            }
        };
        saveEdit.addActionListener(buttonAction);
        delVen.addActionListener(buttonAction);
        cancelEdit.addActionListener(buttonAction);
    }

    public void updateRec(Vector<String> records, int index){
        /*This function saves all changes made to the records (new record and edited record) */
        String str = new String();
        String msg;
        if(recStatus){
            for(int i=0; i<temp.length; i++){
                if(temp[i].length()==0) temp[i]="None";
                str += temp[i] + "\t";
            }
            try{ //Update vector
                records.set(index, str);
                msg = new String("Record has been updated successfully!");
            }catch(ArrayIndexOutOfBoundsException arrE){
                //OutOfBounds: index is a new record
                records.addElement(str);
                msg = new String("Record has been added successfully!");
            }
            
            //Write vector into file
            try{
                writeFile("venue.txt", records);
                JOptionPane.showMessageDialog(null, msg, "Success", JOptionPane.PLAIN_MESSAGE);
            }catch(IOException ioe){
                JOptionPane.showMessageDialog(null, "Operation failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            allRecord = records;
        }
    }

    public String[][] viewRecord(Vector<String> records){
        /*This function lets admin view all available venue information in the system. */
        //Load each record to 1 row, each field to 1 column
        String data[][] = new String[records.size()][4];
        for(int i=0; i<records.size(); i++){
            temp = new String[0];
            temp = records.get(i).split("\t");
            for(int j=0; j<4; j++){
                data[i][j] = temp[j+1];
            }
        }
        return data;
    }

    public void deleteRecord(Vector<String> records, String id){
        /*This function completely removes a venue information from the file. */
        int index = -1, ans;
            
        //Check if the entered venue ID exists
        for(int i=0; i<records.size(); i++){
            temp = new String[0];
            temp = records.get(i).split("\t");
            if(temp[1].equals(id)){//if the record exists, save the index of the record within the vector
                index = i;
                break;
            }        
        }

        ans = JOptionPane.showConfirmDialog(null, "Do you want to delete " + temp[1] +"?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        
        if(ans == JOptionPane.NO_OPTION || ans == JOptionPane.CLOSED_OPTION){
            //User does not want to remove the displayed venue
            return;
        }
        else{
            //Remove the venue record from the file
            //.remove() returns the value removed from vector
            records.remove(index); 
                            
            //Update file after removal
            try{
                writeFile("venue.txt", records);
                JOptionPane.showMessageDialog(null, "Record is deleted successfully!", "Success", JOptionPane.PLAIN_MESSAGE);
            }catch(IOException ioe){
                JOptionPane.showMessageDialog(null, "Error updating file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            allRecord = records;
        }
    }
    
    //searchRecord must search by file id
    public boolean searchRecord(int input, Vector<String> records){
        /*This function checks if the given input (file ID) exists */
        boolean found = false;
        for(int i=0; i<records.size(); i++){
            temp = new String[0];
            temp = records.get(i).split("\t");
            if(temp[0].equals(Integer.toString(input))){
                found = true;
                break;
            }
        }
        return found;
    }

    public void writeFile(String fname, Vector<String> allRecord) throws IOException{
        /*This function updates the vector to the given file name (venue.txt) */
        FileWriter writeFile = new FileWriter(fname, false);
        String[] temp = new String[0];
        for(int i=0; i<allRecord.size(); i++){
            temp = allRecord.get(i).split("\t");
            for(int j=0; j<temp.length; j++)
                writeFile.write(temp[j] + "\t");
            writeFile.write("\r\n"); 
            //write in new line, prepare for next record
        }
        writeFile.close();
    }
}
