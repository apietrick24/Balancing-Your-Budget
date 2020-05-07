//Hack Academy (Gilmour Hackathon) Project - University School: Alex, Jay, Nick and Cameron
//Last Updated: 3/1/20

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Main {

    //Default text for Number of Source JLabel
    private static final String inputSourceDefault = "Number of Sources : 0";

    //Default dimensions for textField
    private static final Dimension textField = new Dimension(30, 25);

    //Source Number JLabel
    private static JLabel numberSources = new JLabel(inputSourceDefault);

    //Queue of Sources
    private static Queue<Source> sourceQueue = new LinkedList<>();
    //Number of Source Objects in the Queue
    private static int numberOfSources = 0;

    //Top Most JPanel - Changes Depending on Input Type
    private static JPanel inputPanel = new JPanel();

    //Center JPanel - Changes Depending on status
    private static JPanel center = new JPanel();

    /**
     * This is where the magic happens
     *
     * @param args - We don't use this parameter
     */
    public static void main(String args[]) {
        JFrame frame = creatingJFrame();

        //The dropdown menu
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.addItem("Manual Input");
        comboBox.addItem("File Input");
        comboBox.addActionListener(new ActionListener() {
            @Override
            /**
             * When the user interacts with the combobox
             */
            public void actionPerformed(ActionEvent e) {
                inputPanel.invalidate();
                frame.getContentPane().remove(inputPanel);

                //selecting the correct input type
                if (comboBox.getSelectedItem().equals("Manual Input")) {
                    inputPanel = manualInput();
                } else {
                    inputPanel = fileInput();
                }

                //Setting the input correctly
                frame.getContentPane().add(BorderLayout.NORTH, inputPanel);
                inputPanel.validate();
                frame.validate();
                inputPanel.repaint();
            }
        });

        //Reset Button
        JButton reset = new JButton("Reset");
        reset.addActionListener(new ActionListener() {
            @Override
            /**
             * when the button is pressed - reset values back to default values
             */
            public void actionPerformed(ActionEvent e) {
                resetInputs();
                frame.remove(center);
                center = new JPanel();
                frame.add(BorderLayout.CENTER, center);
            }
        });

        //State Combobox
        JComboBox<String> comboBox1 = new JComboBox<>();
        comboBox1.addItem("Ohio");

        //Start Button
        JButton start = new JButton("Start");
        start.addActionListener(new ActionListener() {

            //When the Button is Pressed
            @Override
            /**
             * Starts the whole the program
             */
            public void actionPerformed(ActionEvent e) {
                System.out.println("Starting Calculations");

                //Removing the center panel - preparing to please table there
                center.invalidate();
                frame.getContentPane().remove(center);

                //Calculate the rest of the value of sources
                calculatedQueues(sourceQueue);

                //Only "+" sources
                double income = 0.0;

                //All sources
                double incomeAfterServices = 0.0;

                //2D String array to input for the JTable
                String[][] sourceInformation = new String[numberOfSources + 2][5];

                //Current Index
                int index = 0;

                //Decimal Format
                DecimalFormat numberFormat = new DecimalFormat("#.00");

                //For each item in the Queue - sets their row values in the 2D array
                for (Source item : sourceQueue) {
                    sourceInformation[index][0] = item.getName();

                    if (item.isAdd()) {
                        sourceInformation[index][1] = "+";
                    } else {
                        sourceInformation[index][1] = "-";
                    }

                    sourceInformation[index][2] = String.valueOf(numberFormat.format(item.getMonthlyRate()));
                    sourceInformation[index][3] = String.valueOf(numberFormat.format(item.getYearlyRate()));
                    sourceInformation[index][4] = String.valueOf(numberFormat.format(item.getPercentOfTotalYear() * 100));

                    if (item.getYearlyRate() > 0) {
                        income += item.getYearlyRate();
                    }

                    incomeAfterServices += item.getYearlyRate();

                    index++;
                }

                //combines federal and state tax
                double taxRate = calculateFederalTax(income) + calculateOHStateTax(income);

                //State Tax Source
                sourceInformation[numberOfSources][0] = "OH Income Tax";
                sourceInformation[numberOfSources][1] = "-";
                sourceInformation[numberOfSources][2] = "NA";
                sourceInformation[numberOfSources][3] = String.valueOf(numberFormat.format(calculateOHStateTax(income) * -1));
                sourceInformation[numberOfSources][4] = String.valueOf(numberFormat.format((calculateOHStateTax(income) / taxRate) * 100));

                //Federal tax Source
                sourceInformation[numberOfSources + 1][0] = "Federal Income Tax";
                sourceInformation[numberOfSources + 1][1] = "-";
                sourceInformation[numberOfSources + 1][2] = "NA";
                sourceInformation[numberOfSources + 1][3] = String.valueOf(numberFormat.format(calculateFederalTax(income) * -1));
                sourceInformation[numberOfSources + 1][4] = String.valueOf(numberFormat.format((calculateFederalTax(income) / taxRate) * 100));

                //Creates a JTable of the 2D Array
                JTable table = new JTable(sourceInformation, new String[]{"Source", "Factor", "Monthly Rate", "Yearly Rate", "Percent of Yearly Income"});
                table.setEditingColumn(2);
                table.setShowGrid(true);

                //Coloring the cell rows
                for (int i = 0; i < 5; i++) {
                    table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                        @Override
                        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                            final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                            LinkedList<Source> tempt = (LinkedList) sourceQueue;

                            Color backgroundColor;

                            try {
                                //if "+", color Green
                                if (tempt.get(row).isAdd()) {
                                    backgroundColor = (Color.GREEN);
                                } else {
                                    backgroundColor = (Color.RED);
                                }
                            } catch (IndexOutOfBoundsException e) {
                                //Tax rates color gray
                                backgroundColor = Color.lightGray;
                            }

                            c.setBackground(backgroundColor);

                            return c;
                        }
                    });
                }

                Action action = new AbstractAction() {
                    /** when a table cell is edited
                     *
                     * @param e
                     */
                    public void actionPerformed(ActionEvent e) {
                        TableCellListener tcl = (TableCellListener) e.getSource();

                        LinkedList<Source> tempt = (LinkedList) sourceQueue;

                        //Which column was edited
                        switch (tcl.getColumn()) {
                            case 0:
                                //Name column - accepts changes
                                //If name is blanks, deletes
                                if (tcl.getRow() < numberOfSources) {
                                    if (tcl.getNewValue().equals("") || tcl.getNewValue().equals(null)) {
                                        if (tempt.remove(tempt.get(tcl.getRow()))) {
                                            numberOfSources--;
                                        }
                                    } else {
                                        if (tcl.getRow() < numberOfSources) {
                                            tempt.get(tcl.getRow()).setName((String) tcl.getNewValue());
                                        }
                                    }
                                }

                                break;
                            case 1:
                                //Factor column
                                //Accepts changes, make sure that work with the monthly rate column as well

                                String input = (String) tcl.getNewValue();

                                if (tcl.getRow() < numberOfSources) {
                                    if (input.equals("+")) {
                                        tempt.get(tcl.getRow()).setAdd(true);

                                        if (tempt.get(tcl.getRow()).getMonthlyRate() < 0) {
                                            tempt.get(tcl.getRow()).setMonthlyRate(Math.abs(tempt.get(tcl.getRow()).getMonthlyRate()));
                                        }
                                    } else if (input.equals("-")) {
                                        tempt.get(tcl.getRow()).setAdd(false);

                                        if (tempt.get(tcl.getRow()).getMonthlyRate() > 0) {
                                            tempt.get(tcl.getRow()).setMonthlyRate(-1 * tempt.get(tcl.getRow()).getMonthlyRate());
                                        }
                                    }
                                }
                                break;
                            case 2:
                                //Monthly rate column
                                //Accepts changes, make sure that they work with the factor column as well
                                //if monthlyRate = 0, deletes source

                                if (tcl.getRow() < numberOfSources) {
                                    tempt.get(tcl.getRow()).setMonthlyRate(Double.parseDouble((String) tcl.getNewValue()));

                                    if (Double.parseDouble((String) tcl.getNewValue()) > 0) {
                                        tempt.get(tcl.getRow()).setAdd(true);
                                    } else {
                                        tempt.get(tcl.getRow()).setAdd(false);
                                    }

                                    if (tcl.getNewValue().equals("0")) {
                                        if (tempt.remove(tempt.get(tcl.getRow()))) {
                                            numberOfSources--;
                                        }
                                    }
                                }
                                break;
                            default:
                                //For all of the others columns
                                //Do nothing
                                break;
                        }

                        //Trigger the Start Button's Action
                        //IE redraw the table
                        start.getActionListeners()[0].actionPerformed(e);

                    }
                };

                TableCellListener tcl = new TableCellListener(table, action);

                //Creates the Output Panel and adds Jlabels with information
                //If the monthlyRate is brings in money it will be displayed as green otherwise it will be displayed as red
                JPanel bottomCenter = new JPanel();

                JLabel yearlyTotal = new JLabel("Yearly Income: " + numberFormat.format(income));
                if (income <= 0) {
                    yearlyTotal.setBackground(Color.RED);
                } else {
                    yearlyTotal.setBackground(Color.GREEN);
                }
                yearlyTotal.setOpaque(true);
                bottomCenter.add(yearlyTotal);

                JLabel expenses = new JLabel("Yearly Expenses: " + (income - incomeAfterServices));

                if ((income - incomeAfterServices) != 0) {
                    expenses.setBackground(Color.RED);
                } else {
                    expenses.setText("Yearly Expenses: 0.0");
                    expenses.setBackground(Color.GREEN);
                }

                expenses.setOpaque(true);
                bottomCenter.add(expenses);

                JLabel tax = new JLabel("Tax Rate: " + numberFormat.format(taxRate) + " (" + numberFormat.format(calculateOHStateTax(income)) + ", " + numberFormat.format(calculateFederalTax(income)) + ")");
                if (taxRate <= 0) {
                    tax.setBackground(Color.GREEN);
                } else {
                    tax.setBackground(Color.RED);
                }
                tax.setOpaque(true);
                bottomCenter.add(tax);

                JLabel freeMoney = new JLabel("Opens Funds: " + numberFormat.format((incomeAfterServices - taxRate)));
                if ((incomeAfterServices - taxRate) > 0) {
                    freeMoney.setBackground(Color.GREEN);
                } else {
                    freeMoney.setBackground(Color.RED);
                }
                freeMoney.setOpaque(true);
                bottomCenter.add(freeMoney);

                JPanel mainPanel = new JPanel();
                mainPanel.add(new JScrollPane(table));
                mainPanel.add(bottomCenter);

                //Adds all elements to the Frame
                center = mainPanel;

                frame.getContentPane().add(BorderLayout.CENTER, center);

                mainPanel.validate();
                frame.validate();
                mainPanel.repaint();
            }
        });

        //Add Item to Panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(comboBox);
        bottomPanel.add(reset);
        bottomPanel.add(numberSources);
        bottomPanel.add(comboBox1);
        bottomPanel.add(start);

        //Set center to Panel
        center = new JPanel();

        //Adding Panels to Frame
        frame.getContentPane().add(BorderLayout.NORTH, inputPanel);
        frame.getContentPane().add(BorderLayout.CENTER, center);
        frame.getContentPane().add(BorderLayout.SOUTH, bottomPanel);
        frame.setVisible(true);
    }

    /**
     * Creates a JPanel with the correct options for the file inputs
     *
     * @return Return the Top JPanel for the program to Display
     */
    private static JPanel fileInput() {
        JPanel inputPanel = new JPanel();

        //File Input
        JLabel fileName = new JLabel("File Name Input");
        JTextField fileNameInput = new JTextField(10);
        JButton findFile = new JButton("Find File");
        findFile.addActionListener(new ActionListener() {
            @Override
            /**
             * when button is pressed
             */
            public void actionPerformed(ActionEvent e) {
                resetInputs();

                try {
                    read(new File(fileNameInput.getText() + ".txt"));
                    fileNameInput.setText("Found File");
                } catch (FileNotFoundException ex) {
                    fileNameInput.setText("File Not Found");
                    ex.printStackTrace();
                }

            }
        });

        //Add Items to Panel
        inputPanel.add(fileName);
        inputPanel.add(fileNameInput);
        inputPanel.add(findFile);

        inputPanel.setVisible(true);

        return inputPanel;
    }

    /**
     * Creates a JPanel with the correct options for the manual inputs
     *
     * @return Return the Top JPanel for the program to Display
     */
    private static JPanel manualInput() {
        JPanel inputPanel = new JPanel();

        //Source Name
        JLabel source = new JLabel("Source Name");
        JTextField sourceInput = new JTextField(5);
        sourceInput.setPreferredSize(textField);

        //Source Monthly Rate
        JLabel monthlyRate = new JLabel("Monthly Rate");
        JTextField monthlyRateInput = new JTextField(5);
        monthlyRateInput.setPreferredSize(textField);

        //Add Source Button
        JButton inputButton = new JButton("Add Source");
        inputButton.addActionListener(new ActionListener() {

            //When the Button is Pressed
            @Override
            /**
             * When the add source is pressed
             */
            public void actionPerformed(ActionEvent e) {
                boolean add = true;

                //if monthly rate is negative, set add boolean to false
                if (Double.valueOf(monthlyRateInput.getText()) < 0) {
                    add = false;
                }
                //Add new source to queue
                addSource(sourceInput.getText(), add, Double.valueOf(monthlyRateInput.getText()), sourceQueue);

                //Reset inputs
                sourceInput.setText("");
                monthlyRateInput.setText("");

                //Update number of Sources
                updateSourceNumber(numberSources);
            }
        });

        //Creating a File
        JLabel fileName = new JLabel("File Name");
        JTextField fileNameInput = new JTextField(5);
        sourceInput.setPreferredSize(textField);
        JButton fileButton = new JButton("Create File");
        fileButton.addActionListener(new ActionListener() {
            @Override
            /**
             * when create file button is pressed
             */
            public void actionPerformed(ActionEvent e) {
                try {
                    write(sourceQueue, new File(fileNameInput.getText() + ".txt"));
                    fileNameInput.setText("File Saved");
                } catch (IOException ex) {
                    fileNameInput.setText("File wasn't Saved");
                    ex.printStackTrace();
                }
            }
        });

        //Adding Items to Panel
        inputPanel.add(source);
        inputPanel.add(sourceInput);
        inputPanel.add(monthlyRate);
        inputPanel.add(monthlyRateInput);
        inputPanel.add(inputButton);
        inputPanel.add(fileName);
        inputPanel.add(fileNameInput);
        inputPanel.add(fileButton);

        inputPanel.setVisible(true);

        return inputPanel;
    }

    /**
     * Creates and returns the center JFrame for all of the graphical components
     *
     * @return SE
     */
    private static JFrame creatingJFrame() {
        JFrame frame = new JFrame("Balancing Your Budget");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(false);

        return frame;
    }

    /**
     * Creates a new Source Object and then it adds it the a Queue of Sources
     *
     * @param name    - Name of the Income Source
     * @param add     - Whether it adds or subtracts from the total
     * @param monthly - Monthly rate
     * @param sources - Queue of the all the Sources
     */
    public static void addSource(String name, boolean add, double monthly, Queue sources) {
        Source newSource = new Source(name, add, monthly);
        sources.add(newSource);

        //Adds to the running total of sources
        numberOfSources++;
    }

    /**
     * Updates the JLabel that holds the number of Sources on the JFrame
     *
     * @param numberOfSources - JFrame component
     */
    private static void updateSourceNumber(JLabel numberOfSources) {
        numberOfSources.setText(numberOfSources.getText().substring(0, numberOfSources.getText().length() - 1) + "" + (Integer.parseInt(String.valueOf(numberOfSources.getText().charAt(numberOfSources.getText().length() - 1))) + 1));
    }

    /**
     * Sets all of the program's inputs to their default values, resetting the application
     */
    private static void resetInputs() {
        numberSources.setText(inputSourceDefault);
        numberOfSources = 0;

        while (!sourceQueue.isEmpty()) {
            sourceQueue.remove();
        }
    }

    /**
     * Calculates the rest of the null values Sources have IE yearly rate, percent of yearly rate.
     * The positive and negative yearly rates are calculated seperately.
     *
     * @param q - Queue of income sources for their person
     */
    private static void calculatedQueues(Queue<Source> q) {
        double totalpYearRate = 0;
        double totalnYearRate = 0;
        Queue<Source> i = new LinkedList<>();
        while (!q.isEmpty()) {
            Source s = q.remove();
            if (s.isAdd() == true) {
                s.setYearlyRate(s.getMonthlyRate() * 12);
                totalpYearRate += s.getMonthlyRate() * 12;
                i.add(s);
            } else {
                s.setYearlyRate((s.getMonthlyRate() * 12));
                totalnYearRate += s.getMonthlyRate() * 12;
                i.add(s);
            }
        }

        for (Source s : i) {
            if (s.getMonthlyRate() >= 0) {
                s.setPercentOfTotalYear(s.getYearlyRate() / totalpYearRate);
            } else {
                s.setPercentOfTotalYear(s.getYearlyRate() / totalnYearRate);
            }
            q.add(s);
        }
    }

    /**
     * Calculates the amount of federal tax the person has to pay
     * based on their income
     *
     * @param income - all positive income sources
     * @return - Amount of Federal Tax the person has to pay
     */
    private static double calculateFederalTax(double income) {
        if (income < 0)
            return -1.0;
        else if (income <= 9875)
            return income * 0.1;
        else if (income > 9875 && income <= 40125)
            return 987.5 + (income * 0.12);
        else if (income > 40125 && income <= 85525)
            return 4617.5 + (income * 0.22);
        else if (income > 85525 && income <= 163300)
            return 14605.5 + (income * 0.24);
        else if (income > 163301 && income <= 207350)
            return 33217.5 + (income * 0.32);
        else if (income > 207351 && (income <= 518400))
            return 47367.5 + (income * 0.35);
        else return 156235 + (income * 0.37);
    }

    /**
     * Calculates the amount of state tax the person has to pay
     * based on their income
     * <p>
     * (Hard coded for Ohio due to time limit)
     *
     * @param income - all positive income sources
     * @return - Amount of Federal Tax the person has to pay
     */
    private static double calculateOHStateTax(double income) {
        if (income < 0)
            return -1.0;
        else if (income <= 5200)
            return income * 0.00495;
        else if (income > 5200 && income <= 10400)
            return income * .0099;
        else if (income > 10400 && income <= 15650)
            return income * .0198;
        else if (income > 15650 && income <= 20900)
            return income * .02476;
        else if (income > 20900 && income <= 41700)
            return income * .02969;
        else if (income > 41700 && income <= 83350)
            return income * .03465;
        else if (income > 83350 && (income <= 104250))
            return income * .03960;
        else if (income > 104250 && (income <= 208500))
            return income * .04597;
        else return income * .04997;
    }

    /**
     * Takes income source queue and writes it into a
     * text file
     *
     * @param q    - SE
     * @param file - Where the method is writing to
     * @throws IOException - If the File isn't found
     */
    private static void write(Queue<Source> q, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        for (Source s : q)
            fileWriter.append(s.getName() + " " + s.isAdd() + " " + s.getMonthlyRate() + "\n");
        fileWriter.close();
    }

    /**
     * Creates a queue of income sources by reading a
     * given file
     *
     * @param file - Where the method is reading from
     * @throws FileNotFoundException - If the File isn't found
     */
    private static void read(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file);
        Queue<Source> r = new LinkedList<>();

        while (scanner.hasNextLine()) {
            String[] s = scanner.nextLine().split(" ");
            String f = "";
            for (int i = 0; i < s.length - 2; i++) {
                if (i == s.length - 3) {
                    f += s[i];
                } else {
                    f += s[i] + " ";
                }
            }
            if (s[s.length - 2].equals("true"))
                r.add(new Source(f, true, Double.parseDouble(s[s.length - 1])));
            else r.add(new Source(f, false, Double.parseDouble(s[s.length - 1])));
            numberOfSources++;
            updateSourceNumber(numberSources);
        }
        sourceQueue = r;
    }
}