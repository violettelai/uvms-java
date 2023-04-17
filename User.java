import java.awt.*;
import java.util.*;
import org.omg.CORBA.Request;
import java.io.*;
import java.nio.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

/*This class is the abstract superclass of Student and Staff.
 * Refer to its child class for general explanation of the functions
 */
abstract class User{
    protected String[] temp;
    protected Vector<String> allRecord;
    protected int index;
    protected String fieldColumn[] = {"Record ID", "Venue ID", "Start Date&Time", "End Date&Time", "Description", "Status", "Request Date", "User ID"};
    protected UVMS uvms = new UVMS();
    protected GUI gui = new GUI(); //Composition: User Class uses/has-a GUI

    private boolean recStatus;
    private Font fieldFont = new Font("Calibri", Font.BOLD, 14);
    private Font recFont = new Font("Calibri", Font.PLAIN, 14);
    private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    private JFrame viewTable;

    Vector<String> getVector(){
        return allRecord;
    }

    void user_menu(){}

    void addRecord(){
        //Increment the total number of records by 1 as new ID.
        index = uvms.countRecord("record.txt")+1;
        temp[0] = new String(Integer.toString(index));

        //Format and initialize required fields
        temp[5] = "Pending";
        Date date = new Date();
        temp[6] = new String(sdf.format(date));

        Venue v1 = new Venue();
        //Create JComponents for Columns
        JComponent[] ColumnData = new JComponent[8];
        JLabel rid = new JLabel(temp[0], JLabel.CENTER);                            rid.setFont(recFont);
        final JComboBox vid = new JComboBox(v1.getAvailableVenue());                vid.setFont(recFont);
        final JTextField startDate = new JTextField("dd-MM-yyyy hh:mm");        startDate.setFont(recFont);
        final JTextField endDate = new JTextField("dd-MM-yyyy hh:mm");          endDate.setFont(recFont);
        final JTextField desc = new JTextField();                                   desc.setFont(recFont);
        JLabel status = new JLabel(temp[5], JLabel.CENTER);                         status.setFont(recFont);
        JLabel reqDate = new JLabel(temp[6], JLabel.CENTER);                        reqDate.setFont(recFont);
        JLabel uid = new JLabel(temp[7], JLabel.CENTER);                            uid.setFont(recFont);
        final JButton displayVenTT = new JButton("Timetable");

        //Create fields as JLabels (left column)
        for(int i=0; i<8; i++){
            ColumnData[i] = new JLabel(fieldColumn[i], JLabel.RIGHT);
            ColumnData[i].setFont(fieldFont);
        }

        //Display New Frame
        final JFrame f = gui.createFrame("Add Record");

        //Add data to their respective cells
        JPanel p = gui.createVertPanel(8, 3, 5, 5);
        p.setMaximumSize(new Dimension(420,230));
        p.add(ColumnData[0]); p.add(rid); p.add(new JLabel());
        p.add(ColumnData[1]); p.add(vid);  p.add(displayVenTT);
        p.add(ColumnData[2]); p.add(startDate); p.add(new JLabel());
        p.add(ColumnData[3]); p.add(endDate); p.add(new JLabel());
        p.add(ColumnData[4]); p.add(desc); p.add(new JLabel());
        p.add(ColumnData[5]); p.add(status); p.add(new JLabel());
        p.add(ColumnData[6]); p.add(reqDate); p.add(new JLabel());
        p.add(ColumnData[7]); p.add(uid); p.add(new JLabel());

        //Create buttons for user to click
        final JButton saveRec = new JButton("Add");
        final JButton cancelAdd = new JButton("Cancel");
        
        //Create panel to hold buttons
        JPanel buttonPanel = gui.createHoriPanel();
        buttonPanel.add(saveRec);
        buttonPanel.add(cancelAdd);

        //Add panel to box panel and add it to the center of frame
        JPanel boxPanel = gui.createBoxPanel();
        boxPanel.add(p, BorderLayout.CENTER);

        JPanel formatSpaceTop = new JPanel();
        formatSpaceTop.setLayout(new BoxLayout(formatSpaceTop, BoxLayout.X_AXIS));
        formatSpaceTop.add(Box.createVerticalStrut(20));

        f.add(formatSpaceTop, BorderLayout.NORTH);
        f.add(boxPanel, BorderLayout.CENTER);
        f.add(buttonPanel, BorderLayout.SOUTH);
        f.setVisible(true);

        ActionListener buttonAction = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                //Validate&check clash for dates before saving
                if(e.getSource() == saveRec){
                    //Retrieve new data for editable field (2,3,4)
                    temp[1] = vid.getSelectedItem().toString();
                    temp[2] = startDate.getText();
                    temp[3] = endDate.getText();
                    temp[4] = desc.getText();

                    //Check Clash for Start/End date&time
                    ArrayList<String[]> checkList = new ArrayList<String[]>();
                    checkList = loadCheckList(temp[1], 0);
                    /* If venue ID is not null, proceed to check dates
                     * If both dates are valid & no clash in time, save changes; else, display error message
                    */
                    if(!vid.getSelectedItem().toString().equals("null")){
                        if(dateValid(temp[2]) && dateValid(temp[3])){
                            if(!startClash(checkList, temp[2]) && !endClash(checkList, temp[2], temp[3]) && !overlapClash(checkList, temp[2], temp[3])){
                                //If all conditions are met, save new record in the file (updateRec function)
                                recStatus = true;
                                updateRec(allRecord, index);
                                f.dispose();
                                if(viewTable!=null) viewTable.dispose();
                                user_menu();
                            }
                        }
                    }else{ //Venue ID is null (no venue records available for users to choose)
                        recStatus = false;
                        JOptionPane.showMessageDialog(null, "No available venue! Contact admin to add venue.", "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                }else if(e.getSource() == cancelAdd){ //User cancels the add process
                    recStatus = false;
                    f.dispose();
                    if(viewTable!=null) viewTable.dispose();
                    user_menu();
                }else if(e.getSource() == displayVenTT){
                    //User wants to display the timetable of a venue
                    if(vid.getSelectedItem().toString().equals("null")){
                        JOptionPane.showMessageDialog(null, "No available venue.", "Warning", JOptionPane.WARNING_MESSAGE);
                    }else{
                        ArrayList<String[]> checkList = new ArrayList<String[]>();
                        checkList = loadCheckList(vid.getSelectedItem().toString(), 0);
                        if(checkList.size()==0)
                            JOptionPane.showMessageDialog(null, vid.getSelectedItem().toString() + " has no reservation record. All time slots free.", "Information", JOptionPane.INFORMATION_MESSAGE);
                        else{ //Found reservation records under the venue chosen. Sort and display venue timetable
                            sortDate(checkList);
                            venueTT(checkList, vid.getSelectedItem().toString());
                        }
                    }
                }
            }
        };
        saveRec.addActionListener(buttonAction);
        cancelAdd.addActionListener(buttonAction);
        displayVenTT.addActionListener(buttonAction);

        return;
    }

    void editRecord(final int id){
        JComponent[] ColumnData = new JComponent[8];
        //Create JLabels for left column (field)
        for(int i=0; i<8; i++){
            ColumnData[i] = new JLabel(fieldColumn[i], JLabel.RIGHT);
            ColumnData[i].setFont(fieldFont);
        }
                
        Venue v1 = new Venue();

        /**Create JTextFields for editable information & format the fields*/
        JLabel rid = new JLabel(temp[0], JLabel.CENTER);                rid.setFont(recFont);
        final JComboBox vid = new JComboBox(v1.getAvailableVenue());    vid.setFont(recFont);
        vid.setSelectedItem(temp[1]); //Initial selected item
        final JTextField startDate = new JTextField(temp[2]);           startDate.setFont(recFont);
        final JTextField endDate = new JTextField(temp[3]);             endDate.setFont(recFont);
        final JTextField desc = new JTextField(temp[4]);                desc.setFont(recFont);
        JLabel status = new JLabel(temp[5], JLabel.CENTER);             status.setFont(recFont);
        JLabel reqDate = new JLabel(temp[6], JLabel.CENTER);            reqDate.setFont(recFont);
        JLabel uid = new JLabel(temp[7], JLabel.CENTER);                uid.setFont(recFont);
        final JButton displayVenTT = new JButton("Timetable");

        //Create display frame
        final JFrame f = gui.createFrame("Edit Record");

        JPanel p = gui.createVertPanel(8, 3, 5, 5);

        //Add Components into Columns
        p.add(ColumnData[0]); p.add(rid); p.add(new JLabel());       
        p.add(ColumnData[1]); p.add(vid);  p.add(displayVenTT);      
        p.add(ColumnData[2]); p.add(startDate); p.add(new JLabel());
        p.add(ColumnData[3]); p.add(endDate); p.add(new JLabel());
        p.add(ColumnData[4]); p.add(desc); p.add(new JLabel());
        p.add(ColumnData[5]); p.add(status); p.add(new JLabel());
        p.add(ColumnData[6]); p.add(reqDate); p.add(new JLabel());
        p.add(ColumnData[7]); p.add(uid); p.add(new JLabel());

        //Create buttons for user to click
        final JButton saveEdit = new JButton("Save");
        final JButton cancelR = new JButton("Cancel Request");
        final JButton goBack = new JButton("Go Back");

        //Create panel to hold buttons
        JPanel buttonPanel = gui.createHoriPanel();
        buttonPanel.add(saveEdit);
        buttonPanel.add(cancelR);
        buttonPanel.add(goBack);

        JPanel boxPanel = gui.createBoxPanel();
        boxPanel.add(p, BorderLayout.CENTER);

        JPanel formatSpaceTop = new JPanel();
        formatSpaceTop.setLayout(new BoxLayout(formatSpaceTop, BoxLayout.X_AXIS));
        formatSpaceTop.add(Box.createVerticalStrut(20));

        f.add(formatSpaceTop, BorderLayout.NORTH);
        f.add(boxPanel, BorderLayout.CENTER); 
        f.add(buttonPanel, BorderLayout.SOUTH);
        f.setVisible(true);

        ActionListener buttonAction = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(e.getSource() == saveEdit){
                    //Retrieve new data for editable field
                    temp[1] = vid.getSelectedItem().toString();
                    temp[2] = startDate.getText();
                    temp[3] = endDate.getText();
                    temp[4] = desc.getText();
                    temp[6] = sdf.format(new Date()); //Update request date

                    //Check Clash for Start/End date&time
                    ArrayList<String[]> checkList = new ArrayList<String[]>();
                    checkList = loadCheckList(temp[1], id);
                    /* If venue ID is not null, proceed to check dates
                      * If both dates are valid & no clash in time, save changes; else, display error message
                    */
                    if(!vid.getSelectedItem().toString().equals("null")){
                        if(dateValid(temp[2]) && dateValid(temp[3])){
                            if(!startClash(checkList, temp[2]) && !endClash(checkList, temp[2], temp[3]) && !overlapClash(checkList, temp[2], temp[3])){
                                //If all conditions are met, save the update in the file (updateRec function)
                                recStatus = true;
                                updateRec(allRecord, index);
                                f.dispose();
                                if(viewTable!=null) viewTable.dispose();
                                editRecord(id);
                            }
                        }
                    }else{
                        recStatus = false;
                        JOptionPane.showMessageDialog(null, "No available venue! Contact admin to add venue.", "Warning", JOptionPane.WARNING_MESSAGE);
                    }
                }else if(e.getSource() == cancelR){
                    deleteRecord(id);
                    f.dispose();
                    if(viewTable!=null) viewTable.dispose();
                    user_menu();           
                }else if(e.getSource() == goBack){
                    f.dispose();
                    if(viewTable!=null) viewTable.dispose();
                    user_menu();
                }else if(e.getSource() == displayVenTT){
                    //User wants to display timetable for the selected venue
                    if(vid.getSelectedItem().toString().equals("null")){
                        //Venue ID is null. No available venue for user to choose.
                        JOptionPane.showMessageDialog(null, "No available venue.", "Warning", JOptionPane.WARNING_MESSAGE);
                    }else{
                        ArrayList<String[]> checkList = new ArrayList<String[]>();
                        checkList = loadCheckList(vid.getSelectedItem().toString(), 0);
                        if(checkList.size()==0) //Venue ID is valid, Venue exists and all time slots are available (no reservation under the venue ID)
                            JOptionPane.showMessageDialog(null, vid.getSelectedItem().toString() + " has no reservation record. All time slots free.", "Information", JOptionPane.INFORMATION_MESSAGE);
                        else{ //Found reservation records under the venue chosen. Sort and display venue timetable
                            sortDate(checkList);
                            venueTT(checkList, vid.getSelectedItem().toString());
                        }
                    }
                }
            }
        };
        saveEdit.addActionListener(buttonAction);
        cancelR.addActionListener(buttonAction);
        goBack.addActionListener(buttonAction);
        displayVenTT.addActionListener(buttonAction);
    }

    String[][] viewRecord(Vector<String> records){
        //Each record uses 1 row.
        //Each row has 8 columns/fields
        String[][] data = new String[records.size()][8];
        for(int i=0; i<records.size(); i++){
            temp = new String[0];
            temp = records.get(i).split("\t");
            for(int j=0; j<8; j++){
                data[i][j] = temp[j]; //Insert data into respective row and column
            }
        }
        return data;
    }

    void deleteRecord(int input){
        int ans;
        String str = new String();

        //Ask user for delete confirmation
        ans = JOptionPane.showConfirmDialog(null, "Do you want to cancel this reservation?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        
        //User selects NO option or closes the dialog
        if(ans == JOptionPane.NO_OPTION || ans == JOptionPane.CLOSED_OPTION){   
        }else{
            temp = new String[0];
            temp = allRecord.get(input-1).split("\t");
            temp[5] = "Cancelled"; //switch record status to cancelled

            //Update vector
            for(int i=0; i<temp.length; i++){
                str += temp[i] + "\t";
            }
            allRecord.set(input-1, str);

            //Write vector into file
            try{
                writeFile("record.txt", allRecord);
                JOptionPane.showMessageDialog(null, "Reservation is cancelled sucessfully!", "Success", JOptionPane.PLAIN_MESSAGE);
            }catch(IOException ioe){
                JOptionPane.showMessageDialog(null, "Error updating file.", "Error", JOptionPane.ERROR_MESSAGE);
        
            }
        }
    }

    boolean searchRecord(int input, Vector<String> records){
        //Returns true if input equals to the record id
        boolean found = false;
        if(temp[0].equals(Integer.toString(input)))
            found = true;
        return found;
    }

    public void updateRec(Vector<String> records, int index){
        /*This function saves all changes made to the records (new record and edited record) */
        String str = new String();
        String msg;
        if(recStatus){ //make sure changes need to be made
            for(int i=0; i<temp.length; i++){
                if(temp[i].length()==0) temp[i]="None";
                str += temp[i] + "\t";
            }
            try{ //Update vector
                records.set(index-1, str);
                msg = new String("Record has been updated successfully!");
            }catch(ArrayIndexOutOfBoundsException arrE){
                //OutOfBounds: index is a new record
                records.addElement(str);
                msg = new String("Record has been added successfully!");
            }
            
            try{ //write vector into file
                writeFile("record.txt", records);
                JOptionPane.showMessageDialog(null, msg, "Success", JOptionPane.PLAIN_MESSAGE);
            }catch(IOException ioe){
                JOptionPane.showMessageDialog(null, "Operation failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            allRecord = records;
        }
    }

    void writeFile(String fname, Vector<String> allRecord)throws IOException{
        /*This function updates the Vector into the given file name*/
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

    boolean dateValid(String d){
        /*This function checks the given date is in correct format and is larger than the current date&time */
        try {
            Date currDate = new Date();
            if(sdf.parse(d).compareTo(currDate)<0){
                JOptionPane.showMessageDialog(null, "Input date & time has already passed!", "Warning", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "Invalid date input! ", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    Date convertDate(String s){
        Date date = new Date();
        try {
            date = sdf.parse(s);
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(null, "Invalid date input! ", "Error", JOptionPane.ERROR_MESSAGE);
            
        }
        return date;
    }

    void sortDate(ArrayList<String[]> list){
        /*This function sorts the list of dates given in ascending order */
        Collections.sort(list, new Comparator<String[]>() {    
            @Override
            public int compare(String[] a, String[] b) {
                return a[2].compareTo(b[2]);
            }               
        });
    }

    ArrayList<String[]> loadCheckList(String venueId, int id){
        /*This function loads all approved records under the given venue ID into a check list
         * Records before current date&time & in pending/denied/cancelled state will not be loaded
         * The list is then returned as a list of entries that needs to be checked for time clash
         */

        allRecord = uvms.loadRecord("record.txt");
        String[] temp2;
        ArrayList<String[]> list = new ArrayList<String[]>();

        for(int i=0; i<allRecord.size(); i++){
            temp2 = new String[0]; //clear content of array
            temp2 = allRecord.get(i).split("\t");
            try{
                Date checkEndDate = sdf.parse(temp2[3]);
                Date currDate = new Date();
                //only check for approved request that havent complete/finish, past request/before curr date no need check
                if(temp2[1].equals(venueId) && temp2[5].equals("Approved") && currDate.compareTo(checkEndDate) < 0){
                    if(!temp2[0].equals(Integer.toString(id)))
                        list.add(temp2); //record that need to check clashing
                }
            }catch(ParseException e){
                JOptionPane.showMessageDialog(null, "Invalid data! ", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return list;
    }

    void venueTT(ArrayList<String[]> list, String venueId){
        /*This function displays a series of valid records under the same venue ID in ascending order
         */
        //Load the list of date&time and put them in their respective array element
        String data[][] = new String[list.size()][3];
        for(int i=0; i<list.size(); i++){
            for(int j=0; j<3; j++){
                data[i][j] = list.get(i)[j+1];
            }
        }

        //Create a table using the loaded data
        String[] venueColumn = {"Venue ID", "Start Date & Time", "End Date & Time"};
        JTable table = new JTable(data, venueColumn);
        JScrollPane sp = new JScrollPane(table);
        table.setDefaultEditor(Object.class, null);
        table.setAutoCreateRowSorter(true);

        //Align all field at center
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        TableModel tableModel = table.getModel();
        for (int i=0; i<tableModel.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(centerRender);

        //Create display frame for the page
        if(viewTable != null) viewTable.dispose();
        viewTable = new JFrame("Reservation Timetable of " + venueId);
        viewTable.setSize(1000, 500);
        viewTable.setLayout(new BorderLayout());
        viewTable.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        viewTable.setLocationRelativeTo(null); 
        viewTable.add(sp, BorderLayout.CENTER);
        viewTable.setVisible(true);
    }

    boolean startClash(ArrayList<String[]> list, String s){
        /*This function checks if clashing occurs between 2 given start date&time*/
        Date start = convertDate(s);
        for(int i=0; i<list.size(); ++i){ //sdf.parse(list.get(i)[2]).compareTo(currDate)<0
            if(start.compareTo(convertDate(list.get(i)[2]))>=0 && start.compareTo(convertDate(list.get(i)[3]))<0){
                JOptionPane.showMessageDialog(null, "Clashed with " + list.get(i)[2] + " - " + list.get(i)[3], "Alert", JOptionPane.WARNING_MESSAGE);
                return true;
            }
        }
        return false;
    }

    boolean endClash(ArrayList<String[]> list, String s, String e){
        /*This function checks if clashing occurs between 2 given end date&time*/
        Date start = convertDate(s);
        Date end = convertDate(e);
        if(end.compareTo(start)<0){
            JOptionPane.showMessageDialog(null, "End Date & Time is before Start Date & Time!", "Alert", JOptionPane.WARNING_MESSAGE);
            return true;
        } else if(end.compareTo(start)==0){
            JOptionPane.showMessageDialog(null, "End Date & Time is same as Start Date & Time!", "Alert", JOptionPane.WARNING_MESSAGE);
            return true;
        } else{
            for(int i=0; i<list.size(); ++i){
                if(end.compareTo(convertDate(list.get(i)[2]))>0 && end.compareTo(convertDate(list.get(i)[3]))<=0){
                    JOptionPane.showMessageDialog(null, "Clashed with " + list.get(i)[2] + " - " + list.get(i)[3], "Alert", JOptionPane.WARNING_MESSAGE);
                    return true;
                }
            }
        }
        return false;
    }

    boolean overlapClash(ArrayList<String[]> list, String s, String e){
        /*This function checks if 2 given date&time overlaps each other */
        Date start = convertDate(s);
        Date end = convertDate(e);
        for(int i=0; i<list.size(); ++i){
            if(start.compareTo(convertDate(list.get(i)[2]))<0 && start.compareTo(convertDate(list.get(i)[3]))<0 &&
               end.compareTo(convertDate(list.get(i)[2]))>0 && end.compareTo(convertDate(list.get(i)[3]))>0){
                    JOptionPane.showMessageDialog(null, "Clash with " + list.get(i)[2] + " - " + list.get(i)[3], "Alert", JOptionPane.WARNING_MESSAGE);
                    return true;
            }
        }
        return false;
    }
}
