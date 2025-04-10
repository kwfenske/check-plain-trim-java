/*
  Check Plain Trim #2 - Check Files for Plain Trimmed Text
  Written by: Keith Fenske, http://kwfenske.github.io/
  Monday, 25 September 2017
  Java class name: CheckPlainTrim2
  Copyright (c) 2017 by Keith Fenske.  Apache License or GNU GPL.

  This is a Java 1.4 application to check if files are in plain text and do not
  have trailing spaces or tabs (white space) at the end of lines.  How clean
  are your source, text files, and XML documents?  The following are accepted:

    - horizontal tab (0x09)
    - line feed (0x0A)
    - carriage return (0x0D)
    - blank space (0x20)
    - printable US-ASCII text (0x21 to 0x7E)

  By default, files are assumed to be in the local character set (text
  encoding).  You may select another encoding or raw 8-bit data bytes.  See the
  TrimFile Java application to detect and remove trailing white space, in
  various character sets.

  Apache License or GNU General Public License
  --------------------------------------------
  CheckPlainTrim2 is free software and has been released under the terms and
  conditions of the Apache License (version 2.0 or later) and/or the GNU
  General Public License (GPL, version 2 or later).  This program is
  distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY,
  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE.  See the license(s) for more details.  You should have
  received a copy of the licenses along with this program.  If not, see the
  http://www.apache.org/licenses/ and http://www.gnu.org/licenses/ web pages.

  Graphical Versus Console Application
  ------------------------------------
  The Java command line may contain options or file and folder names.  If no
  file or folder names are given on the command line, then this program runs as
  a graphical or "GUI" application with the usual dialog boxes and windows.
  See the "-?" option for a help summary:

      java  CheckPlainTrim2  -?

  The command line has more options than are visible in the graphical
  interface.  An option such as -u14 or -u16 is recommended because the default
  Java font is too small.  If file or folder names are given on the command
  line, then this program runs as a console application without a graphical
  interface.  A generated report is written on standard output, and may be
  redirected with the ">" or "1>" operators.  (Standard error may be redirected
  with the "2>" operator.)  An example command line is:

      java  CheckPlainTrim2  -s  d:\temp  >report.txt

  The console application will return an exit status of 1 for success, -1 for
  failure, and 0 for unknown.  The graphical interface can be very slow when
  the output text area gets too big, which will happen if thousands of files
  are reported.
*/

import java.awt.*;                // older Java GUI support
import java.awt.event.*;          // older Java GUI event support
import java.io.*;                 // standard I/O
import java.text.*;               // number formatting
import java.util.*;               // calendars, dates, lists, maps, vectors
import java.util.regex.*;         // regular expressions
import javax.swing.*;             // newer Java GUI support
import javax.swing.border.*;      // decorative borders
import javax.swing.event.*;       // document listener for text fields

public class CheckPlainTrim2
{
  /* constants */

  static final String COPYRIGHT_NOTICE =
    "Copyright (c) 2017 by Keith Fenske.  Apache License or GNU GPL.";
  static final int DEFAULT_HEIGHT = -1; // default window height in pixels
  static final int DEFAULT_LEFT = 50; // default window left position ("x")
  static final int DEFAULT_TOP = 50; // default window top position ("y")
  static final int DEFAULT_WIDTH = -1; // default window width in pixels
  static final String EMPTY_STATUS = ""; // message when no status to display
  static final int EXIT_FAILURE = -1; // incorrect request or errors found
  static final int EXIT_SUCCESS = 1; // request completed successfully
  static final int EXIT_UNKNOWN = 0; // don't know or nothing really done
  static final String[] FONT_SIZES = {"10", "12", "14", "16", "18", "20", "24",
    "30"};                        // point sizes for text in output text area
  static final String LOCAL_ENCODING = "(local default)";
                                  // our special name for local character set
  static final int MIN_FRAME = 200; // minimum window height or width in pixels
  static final String PROGRAM_TITLE =
    "Check Files for Plain Trimmed Text - by: Keith Fenske";
  static final String RAW_ENCODING = "(raw data bytes)";
                                  // our special name for no data encoding
  static final String[] SHOW_CHOICES = {"show all files", "show correct only",
    "show errors only"};
  static final String SUFFIX_DEFAULT = " .java  .html  .txt  .xml ";
                                  // initial list of file types (extensions)
  static final String SYSTEM_FONT = "Dialog"; // this font is always available
  static final String TEXT_BOTH = "plain trimmed text"; // descriptions
  static final String TEXT_PLAIN = "plain text"; // plain, ignore trim
  static final String TEXT_TRIM = "trimmed text"; // trimmed, ignore plain
  static final int TIMER_DELAY = 1000; // 1.000 seconds between status updates

  /* class variables */

  static JButton cancelButton;    // graphical button for <cancelFlag>
  static boolean cancelFlag;      // our signal from user to stop processing
  static JRadioButton checkBothButton, checkPlainButton, checkTrimButton;
                                  // graphical buttons for text types
  static JCheckBox encodeCheckbox; // graphical option for <encodeFlag>
  static JComboBox encodeDialog;  // graphical option for <encodeName>
  static String encodeName;       // name of assumed character set encoding
  static JButton exitButton;      // "Exit" button for ending this application
  static JFileChooser fileChooser; // asks for input and output file names
  static String fontName;         // font name for text in output text area
  static JComboBox fontNameDialog; // graphical option for <fontName>
  static int fontSize;            // point size for text in output text area
  static JComboBox fontSizeDialog; // graphical option for <fontSize>
  static NumberFormat formatComma; // formats with commas (digit grouping)
  static boolean hiddenFlag;      // true if we process hidden files or folders
  static JFrame mainFrame;        // this application's GUI window
  static boolean mswinFlag;       // true if running on Microsoft Windows
  static JButton openButton;      // "Open" button for files or folders
  static File[] openFileList;     // list of files selected by user
  static Thread openFilesThread;  // separate thread for doOpenButton() method
  static JTextArea outputText;    // generated report while opening files
  static boolean plainFlag;       // true if we are looking for plain text
  static JCheckBox recurseCheckbox; // graphical option for <recurseFlag>
  static boolean recurseFlag;     // true if we search folders and subfolders
  static JButton saveButton;      // "Save" button for writing output text
  static JComboBox showDialog;    // graphical choice for <show...Flag>
  static boolean showFailureFlag; // true if we show files with errors
  static boolean showOtherFlag;   // true if we show various other results
  static boolean showSuccessFlag; // true if we show files that are correct
  static JLabel statusDialog;     // status message during extended processing
  static String statusPending;    // will become <statusDialog> after delay
  static javax.swing.Timer statusTimer; // timer for updating status message
  static JCheckBox suffixCheckbox; // graphical option for <suffixFlag>
  static JTextField suffixDialog; // list of file types (file name extensions)
  static boolean suffixFlag;      // true if there is a list of file types
  static String[] suffixList;     // lowercase array or list of file types
  static String suffixText;       // unparsed text for list of file types
  static String textType;         // description of plain or trimmed text
  static long totalCorrect;       // number of files that are correct
  static long totalError;         // number of files with some type of error
  static long totalFiles;         // total number of files, select file types
  static long totalFolders;       // total number of folders or subfolders
  static boolean trimFlag;        // true if we are looking for trimmed text

/*
  main() method

  If we are running as a GUI application, set the window layout and then let
  the graphical interface run the show.
*/
  public static void main(String[] args)
  {
    ActionListener action;        // our shared action listener
    Font buttonFont;              // font for buttons, labels, status, etc
    boolean consoleFlag;          // true if running as a console application
    Border emptyBorder;           // remove borders around text areas
    boolean encodeFlag;           // true if user selects a character set
    int i;                        // index variable
    boolean maximizeFlag;         // true if we maximize our main window
    int windowHeight, windowLeft, windowTop, windowWidth;
                                  // position and size for <mainFrame>
    String word;                  // one parameter from command line

    /* Initialize variables used by both console and GUI applications. */

    buttonFont = null;            // by default, don't use customized font
    cancelFlag = false;           // don't cancel unless user complains
    consoleFlag = false;          // assume no files or folders on command line
    encodeFlag = false;           // by default, use local character set
    encodeName = LOCAL_ENCODING;  // default name for character set encoding
    fontName = "Verdana";         // preferred font name for output text area
    fontSize = 16;                // default point size for output text area
    hiddenFlag = false;           // by default, don't process hidden files
    mainFrame = null;             // during setup, there is no GUI window
    maximizeFlag = false;         // by default, don't maximize our main window
    mswinFlag = System.getProperty("os.name").startsWith("Windows");
    plainFlag = true;             // by default, look for plain text
    recurseFlag = false;          // by default, don't search subfolders
    showFailureFlag = true;       // by default, show files with errors
    showOtherFlag = true;         // by default, show various other results
    showSuccessFlag = true;       // by default, show files that are correct
    statusPending = EMPTY_STATUS; // begin with no text for <statusDialog>
    suffixFlag = false;           // by default, don't use list of file types
//  suffixList =                  // see call to parseSuffixList() below
    suffixText = SUFFIX_DEFAULT;  // default unparsed list of file types
    textType = TEXT_BOTH;         // description of plain or trimmed text
    totalCorrect = totalError = totalFiles = totalFolders = 0;
                                  // no files found yet
    trimFlag = true;              // by default, look for trimmed text
    windowHeight = DEFAULT_HEIGHT; // default window position and size
    windowLeft = DEFAULT_LEFT;
    windowTop = DEFAULT_TOP;
    windowWidth = DEFAULT_WIDTH;

    /* Initialize number formatting styles. */

    formatComma = NumberFormat.getInstance(); // current locale
    formatComma.setGroupingUsed(true); // use commas or digit groups

    /* Initialize our list of file types (file name extensions).  This sets the
    <suffixList> global variable.  It does not set <suffixFlag>. */

    parseSuffixList(suffixText);  // set initial list of file types

    /* Check command-line parameters for options. */

    for (i = 0; i < args.length; i ++)
    {
      word = args[i].toLowerCase(); // easier to process if consistent case
      if (word.length() == 0)
      {
        /* Ignore empty parameters, which are more common than you might think,
        when programs are being run from inside scripts (command files). */
      }

      else if (word.equals("?") || word.equals("-?") || word.equals("/?")
        || word.equals("-h") || (mswinFlag && word.equals("/h"))
        || word.equals("-help") || (mswinFlag && word.equals("/help")))
      {
        showHelp();               // show help summary
        System.exit(EXIT_UNKNOWN); // exit application after printing help
      }

      else if (word.startsWith("-e") || (mswinFlag && word.startsWith("/e")))
      {
        encodeFlag = true;        // user has chosen a character set
        encodeName = args[i].substring(2); // accept any string from user
      }

      else if (word.startsWith("-f") || (mswinFlag && word.startsWith("/f")))
      {
        suffixText = args[i].substring(2); // accept any string from user
        parseSuffixList(suffixText); // set initial list of file types
        suffixFlag = (suffixList.length > 0); // were there any file types?
      }

      else if (word.equals("-m1") || (mswinFlag && word.equals("/m1")))
      {
        plainFlag = true;         // look for plain text
        textType = TEXT_PLAIN;    // description
        trimFlag = false;         // ignore trimmed text
      }
      else if (word.equals("-m2") || (mswinFlag && word.equals("/m2")))
      {
        plainFlag = false;        // ignore plain text
        textType = TEXT_TRIM;     // description
        trimFlag = true;          // look for trimmed text
      }
      else if (word.equals("-m3") || (mswinFlag && word.equals("/m3")))
      {
        plainFlag = true;         // look for plain text
        textType = TEXT_BOTH;     // description
        trimFlag = true;          // look for trimmed text
      }

      else if (word.equals("-s") || (mswinFlag && word.equals("/s"))
        || word.equals("-s1") || (mswinFlag && word.equals("/s1")))
      {
        recurseFlag = true;       // start doing subfolders
      }
      else if (word.equals("-s0") || (mswinFlag && word.equals("/s0")))
        recurseFlag = false;      // stop doing subfolders

      else if (word.startsWith("-u") || (mswinFlag && word.startsWith("/u")))
      {
        /* This option is followed by a font point size that will be used for
        buttons, dialogs, labels, etc. */

        int size = -1;            // default value for font point size
        try                       // try to parse remainder as unsigned integer
        {
          size = Integer.parseInt(word.substring(2));
        }
        catch (NumberFormatException nfe) // if not a number or bad syntax
        {
          size = -1;              // set result to an illegal value
        }
        if ((size < 10) || (size > 99))
        {
          System.err.println("Dialog font size must be from 10 to 99: "
            + args[i]);           // notify user of our arbitrary limits
          showHelp();             // show help summary
          System.exit(EXIT_FAILURE); // exit application after printing help
        }
        buttonFont = new Font(SYSTEM_FONT, Font.PLAIN, size); // for big sizes
//      buttonFont = new Font(SYSTEM_FONT, Font.BOLD, size); // for small sizes
        fontSize = size;          // use same point size for output text font
      }

      else if (word.startsWith("-w") || (mswinFlag && word.startsWith("/w")))
      {
        /* This option is followed by a list of four numbers for the initial
        window position and size.  All values are accepted, but small heights
        or widths will later force the minimum packed size for the layout. */

        Pattern pattern = Pattern.compile(
          "\\s*\\(\\s*(\\d{1,5})\\s*,\\s*(\\d{1,5})\\s*,\\s*(\\d{1,5})\\s*,\\s*(\\d{1,5})\\s*\\)\\s*");
        Matcher matcher = pattern.matcher(word.substring(2)); // parse option
        if (matcher.matches())    // if option has proper syntax
        {
          windowLeft = Integer.parseInt(matcher.group(1));
          windowTop = Integer.parseInt(matcher.group(2));
          windowWidth = Integer.parseInt(matcher.group(3));
          windowHeight = Integer.parseInt(matcher.group(4));
        }
        else                      // bad syntax or too many digits
        {
          System.err.println("Invalid window position or size: " + args[i]);
          showHelp();             // show help summary
          System.exit(EXIT_FAILURE); // exit application after printing help
        }
      }

      else if (word.equals("-x") || (mswinFlag && word.equals("/x")))
        maximizeFlag = true;      // true if we maximize our main window

      else if (word.startsWith("-") || (mswinFlag && word.startsWith("/")))
      {
        System.err.println("Option not recognized: " + args[i]);
        showHelp();               // show help summary
        System.exit(EXIT_FAILURE); // exit application after printing help
      }

      else
      {
        /* Parameter does not look like an option.  Assume this is a file or
        folder name. */

        consoleFlag = true;       // don't allow GUI methods to be called
        processFileOrFolder(new File(args[i]));
        if (cancelFlag) break;    // exit <for> loop if cancel or fatal error
      }
    }

    /* If running as a console application, print a summary of what we found
    and/or changed.  Exit to the system with an integer status. */

    if (consoleFlag)              // was at least one file/folder given?
    {
      printSummary();             // what we found and what was changed
      if (totalError > 0)         // were there any errors?
        System.exit(EXIT_FAILURE);
      else if (totalCorrect > 0)  // were there any good files?
        System.exit(EXIT_SUCCESS);
      else                        // if there were no files at all
        System.exit(EXIT_UNKNOWN);
    }

    /* There were no file or folder names on the command line.  Open the
    graphical user interface (GUI).  We don't need to be inside an if-then-else
    construct here because the console application called System.exit() above.
    The standard Java interface style is the most reliable, but you can switch
    to something closer to the local system, if you want. */

//  try
//  {
//    UIManager.setLookAndFeel(
//      UIManager.getCrossPlatformLookAndFeelClassName());
////    UIManager.getSystemLookAndFeelClassName());
//  }
//  catch (Exception ulafe)
//  {
//    System.err.println("Unsupported Java look-and-feel: " + ulafe);
//  }

    /* Initialize shared graphical objects. */

    action = new CheckPlainTrim2User(); // create our shared action listener
    emptyBorder = BorderFactory.createEmptyBorder(); // for removing borders
    fileChooser = new JFileChooser(); // create our shared file chooser
    statusTimer = new javax.swing.Timer(TIMER_DELAY, action);
                                  // update status message on clock ticks only

    /* If our preferred font is not available for the output text area, then
    use the boring default font for the local system. */

    if (fontName.equals((new Font(fontName, Font.PLAIN, fontSize)).getFamily())
      == false)                   // create font, read back created name
    {
      fontName = SYSTEM_FONT;     // must replace with standard system font
    }

    /* Create the graphical interface as a series of little panels inside
    bigger panels.  The intermediate panel names are of no lasting importance
    and hence are only numbered (panel01, panel02, etc). */

    /* Create a vertical box to stack buttons and options. */

    JPanel panel01 = new JPanel();
    panel01.setLayout(new BoxLayout(panel01, BoxLayout.Y_AXIS));

    /* Create a horizontal panel for the action buttons. */

    JPanel panel11 = new JPanel(new BorderLayout(0, 0));

    openButton = new JButton("Open Files...");
    openButton.addActionListener(action);
    if (buttonFont != null) openButton.setFont(buttonFont);
    openButton.setMnemonic(KeyEvent.VK_O);
    openButton.setToolTipText("Start finding/opening files.");
    panel11.add(openButton, BorderLayout.WEST);

    JPanel panel12 = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));

    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(action);
    cancelButton.setEnabled(false);
    if (buttonFont != null) cancelButton.setFont(buttonFont);
    cancelButton.setMnemonic(KeyEvent.VK_C);
    cancelButton.setToolTipText("Stop finding/opening files.");
    panel12.add(cancelButton);

    saveButton = new JButton("Save Output...");
    saveButton.addActionListener(action);
    if (buttonFont != null) saveButton.setFont(buttonFont);
    saveButton.setMnemonic(KeyEvent.VK_S);
    saveButton.setToolTipText("Copy output text to a file.");
    panel12.add(saveButton);

    panel11.add(panel12, BorderLayout.CENTER);

    exitButton = new JButton("Exit");
    exitButton.addActionListener(action);
    if (buttonFont != null) exitButton.setFont(buttonFont);
    exitButton.setMnemonic(KeyEvent.VK_X);
    exitButton.setToolTipText("Close this program.");
    panel11.add(exitButton, BorderLayout.EAST);

    panel01.add(panel11);
    panel01.add(Box.createVerticalStrut(14)); // space between panels

    /* Options for limiting which files we look at (by file type). */

    JPanel panel21 = new JPanel(new BorderLayout(0, 0));

    suffixCheckbox = new JCheckBox(
      "Look for files with these file types or suffixes: ", suffixFlag);
    if (buttonFont != null) suffixCheckbox.setFont(buttonFont);
//  suffixCheckbox.addActionListener(action); // do last so don't fire early
    panel21.add(suffixCheckbox, BorderLayout.WEST);

    suffixDialog = new JTextField(suffixText, 20);
    suffixDialog.getDocument().addDocumentListener((DocumentListener) action);
    if (buttonFont != null) suffixDialog.setFont(buttonFont);
    suffixDialog.setMargin(new Insets(0, 2, 0, 2));
    suffixDialog.addActionListener(action); // do last so don't fire early
    panel21.add(suffixDialog, BorderLayout.CENTER);

    panel01.add(panel21);
    panel01.add(Box.createVerticalStrut(9)); // space between panels

    /* Options for the character set (text encoding). */

    JPanel panel22 = new JPanel(new BorderLayout(0, 0));

    encodeCheckbox = new JCheckBox(
      "Read files using this character set or text encoding: ", encodeFlag);
    if (buttonFont != null) encodeCheckbox.setFont(buttonFont);
//  encodeCheckbox.addActionListener(action); // do last so don't fire early
    panel22.add(encodeCheckbox, BorderLayout.WEST);

    encodeDialog = new JComboBox();
    encodeDialog.addItem(LOCAL_ENCODING); // start with our special names
    encodeDialog.addItem(RAW_ENCODING);
    Object[] list23 = java.nio.charset.Charset.availableCharsets().keySet()
      .toArray();                 // get character set names from local system
    for (i = 0; i < list23.length; i ++)
      encodeDialog.addItem((String) list23[i]); // insert each encoding name
    encodeDialog.setEditable(true); // allow user to enter alternate names
    if (buttonFont != null) encodeDialog.setFont(buttonFont);
    encodeDialog.setSelectedItem(encodeName); // selected item is our default
    encodeDialog.setToolTipText(
      "Select name of character set encoding for reading files.");
    encodeDialog.addActionListener(action); // do last so don't fire early
    panel22.add(encodeDialog, BorderLayout.CENTER);

    panel01.add(panel22);
    panel01.add(Box.createVerticalStrut(8)); // space between panels

    /* Options for the text types that we can look for. */

    JPanel panel24 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    ButtonGroup group25 = new ButtonGroup();

    checkPlainButton = new JRadioButton("Check for plain text only, ",
      (plainFlag && ! trimFlag));
    if (buttonFont != null) checkPlainButton.setFont(buttonFont);
    checkPlainButton.addActionListener(action); // do last so don't fire early
    group25.add(checkPlainButton);
    panel24.add(checkPlainButton);

    checkTrimButton = new JRadioButton("trimmed text only, or ",
      (trimFlag && ! plainFlag));
    if (buttonFont != null) checkTrimButton.setFont(buttonFont);
    checkTrimButton.addActionListener(action); // do last so don't fire early
    group25.add(checkTrimButton);
    panel24.add(checkTrimButton);

    checkBothButton = new JRadioButton("both plain and trimmed text.",
      (plainFlag && trimFlag));
    if (buttonFont != null) checkBothButton.setFont(buttonFont);
    checkBothButton.addActionListener(action); // do last so don't fire early
    group25.add(checkBothButton);
    panel24.add(checkBothButton);

    panel01.add(panel24);
    panel01.add(Box.createVerticalStrut(14)); // space between panels

    /* Miscellaneous options. */

    JPanel panel31 = new JPanel(new BorderLayout(10, 0));

    JPanel panel32 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

    fontNameDialog = new JComboBox(GraphicsEnvironment
      .getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    fontNameDialog.setEditable(false); // user must select one of our choices
    if (buttonFont != null) fontNameDialog.setFont(buttonFont);
    fontNameDialog.setSelectedItem(fontName); // select default font name
    fontNameDialog.setToolTipText("Font name for output text.");
    fontNameDialog.addActionListener(action); // do last so don't fire early
    panel32.add(fontNameDialog);

    panel32.add(Box.createHorizontalStrut(5));

    TreeSet sizelist = new TreeSet(); // collect font sizes 10 to 99 in order
    word = String.valueOf(fontSize); // convert number to a string we can use
    sizelist.add(word);           // add default or user's chosen font size
    for (i = 0; i < FONT_SIZES.length; i ++) // add our preferred size list
      sizelist.add(FONT_SIZES[i]); // assume sizes are all two digits (10-99)
    fontSizeDialog = new JComboBox(sizelist.toArray()); // give user nice list
    fontSizeDialog.setEditable(false); // user must select one of our choices
    if (buttonFont != null) fontSizeDialog.setFont(buttonFont);
    fontSizeDialog.setSelectedItem(word); // selected item is our default size
    fontSizeDialog.setToolTipText("Point size for output text.");
    fontSizeDialog.addActionListener(action); // do last so don't fire early
    panel32.add(fontSizeDialog);

    panel31.add(panel32, BorderLayout.WEST);

    JPanel panel33 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));

    recurseCheckbox = new JCheckBox("search subfolders", recurseFlag);
    if (buttonFont != null) recurseCheckbox.setFont(buttonFont);
    recurseCheckbox.setToolTipText("Select to search folders and subfolders.");
    recurseCheckbox.addActionListener(action); // do last so don't fire early
    panel33.add(recurseCheckbox);

    panel31.add(panel33, BorderLayout.CENTER);

    showDialog = new JComboBox(SHOW_CHOICES);
    showDialog.setEditable(false); // user must select one of our choices
    if (buttonFont != null) showDialog.setFont(buttonFont);
    showDialog.setSelectedIndex(0); // by default, show all files
    showDialog.setToolTipText("Select which files to report.");
    showDialog.addActionListener(action); // do last so don't fire early
    panel31.add(showDialog, BorderLayout.EAST);

    panel01.add(panel31);
    panel01.add(Box.createVerticalStrut(15)); // space between panels

    /* Bind all of the buttons and options above into a single panel so that
    the layout does not change when the window changes. */

    JPanel panel41 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    panel41.add(panel01);

    /* Create a scrolling text area for the generated output. */

    outputText = new JTextArea(20, 40);
    outputText.setEditable(false); // user can't change this text area
    outputText.setFont(new Font(fontName, Font.PLAIN, fontSize));
    outputText.setLineWrap(false); // don't wrap text lines
    outputText.setMargin(new Insets(5, 6, 5, 6)); // top, left, bottom, right
    outputText.setText(
      "\nCheck if files are in plain text and do not have trailing spaces or"
      + "\ntabs (white space) at the end of lines.  The contents of the files"
      + "\nare not changed."
      + "\n\nChoose your options; then open files or folders to search."
      + "\n\nCopyright (c) 2017 by Keith Fenske.  By using this program, you"
      + "\nagree to terms and conditions of the Apache License and/or GNU"
      + "\nGeneral Public License.\n\n");

    JScrollPane panel51 = new JScrollPane(outputText);
    panel51.setBorder(emptyBorder); // no border necessary here

    /* Create an entire panel just for the status message.  Set margins with a
    BorderLayout, because a few pixels higher or lower can make a difference in
    whether the position of the status text looks correct. */

    statusDialog = new JLabel(statusPending, JLabel.RIGHT);
    if (buttonFont != null) statusDialog.setFont(buttonFont);

    JPanel panel61 = new JPanel(new BorderLayout(0, 0));
    panel61.add(Box.createVerticalStrut(7), BorderLayout.NORTH);
    panel61.add(Box.createHorizontalStrut(10), BorderLayout.WEST);
    panel61.add(statusDialog, BorderLayout.CENTER);
    panel61.add(Box.createHorizontalStrut(10), BorderLayout.EAST);
//  panel61.add(Box.createVerticalStrut(5), BorderLayout.SOUTH);

    /* Combine buttons and options with output text.  The text area expands and
    contracts with the window size.  Put our status message at the bottom. */

    JPanel panel71 = new JPanel(new BorderLayout(0, 0));
    panel71.add(panel41, BorderLayout.NORTH); // buttons and options
    panel71.add(panel51, BorderLayout.CENTER); // text area
    panel71.add(panel61, BorderLayout.SOUTH); // status message

    /* Create the main window frame for this application.  We supply our own
    margins using the edges of the frame's border layout. */

    mainFrame = new JFrame(PROGRAM_TITLE);
    Container panel72 = mainFrame.getContentPane(); // where content meets frame
    panel72.setLayout(new BorderLayout(0, 0));
    panel72.add(Box.createVerticalStrut(15), BorderLayout.NORTH); // top margin
    panel72.add(Box.createHorizontalStrut(5), BorderLayout.WEST); // left
    panel72.add(panel71, BorderLayout.CENTER); // actual content in center
    panel72.add(Box.createHorizontalStrut(5), BorderLayout.EAST); // right
    panel72.add(Box.createVerticalStrut(5), BorderLayout.SOUTH); // bottom

    mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainFrame.setLocation(windowLeft, windowTop); // normal top-left corner
    if ((windowHeight < MIN_FRAME) || (windowWidth < MIN_FRAME))
      mainFrame.pack();           // do component layout with minimum size
    else                          // the user has given us a window size
      mainFrame.setSize(windowWidth, windowHeight); // size of normal window
    if (maximizeFlag) mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    mainFrame.validate();         // recheck application window layout
    mainFrame.setVisible(true);   // and then show application window

    /* Let the graphical interface run the application now. */

    openButton.requestFocusInWindow(); // give keyboard focus to "Open" button

  } // end of main() method

// ------------------------------------------------------------------------- //

/*
  doCancelButton() method

  This method is called while we are opening files or folders if the user wants
  to end the processing early, perhaps because it is taking too long.  We must
  cleanly terminate any secondary threads.  Leave whatever output has already
  been generated in the output text area.
*/
  static void doCancelButton()
  {
    cancelFlag = true;            // tell other threads that all work stops now
    putOutput("Cancelled by user."); // print message and scroll
  }


/*
  doOpenButton() method

  Allow the user to select one or more files or folders for processing.
*/
  static void doOpenButton()
  {
    /* Ask the user for input files or folders. */

    fileChooser.resetChoosableFileFilters(); // remove any existing filters
    fileChooser.setDialogTitle("Open Files or Folders...");
    fileChooser.setFileHidingEnabled(! hiddenFlag); // may show hidden files
    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    fileChooser.setMultiSelectionEnabled(true); // allow more than one file
    if (fileChooser.showOpenDialog(mainFrame) != JFileChooser.APPROVE_OPTION)
      return;                     // user cancelled file selection dialog box
    openFileList = sortFileList(fileChooser.getSelectedFiles());
                                  // get list of files selected by user

    /* We have a list of files or folders.  Disable the "Open" button until we
    are done, and enable a "Cancel" button in case our secondary thread runs
    for a long time and the user panics. */

    cancelButton.setEnabled(true); // enable button to cancel this processing
    cancelFlag = false;           // but don't cancel unless user complains
    openButton.setEnabled(false); // suspend "Open" button until we are done
    outputText.setText("");       // clear output text area
    totalCorrect = totalError = totalFiles = totalFolders = 0;
                                  // no files found yet

    /* Get user's chosen character set (text encoding), if any.  We don't set
    <encodeFlag> here, because <encodeFlag> is a local variable only used while
    setting up the GUI.  The function of a flag is replaced by a fake encoding
    called <LOCAL_ENCODING>. */

    if (encodeCheckbox.isSelected()) // get the user's character set, if any
      encodeName = (String) encodeDialog.getSelectedItem();
    else                          // if not selected, then use our default
      encodeName = LOCAL_ENCODING;

    /* Get user's list of file types (file name extensions), if any. */

    parseSuffixList(suffixDialog.getText()); // get caller's list of file types
    suffixFlag = suffixCheckbox.isSelected() && (suffixList.length > 0);

    /* Clear status message (bottom of window) and start secondary thread. */

    setStatusMessage(EMPTY_STATUS); // clear text in status message
    statusTimer.start();          // start updating status on clock ticks

    openFilesThread = new Thread(new CheckPlainTrim2User(), "doOpenRunner");
    openFilesThread.setPriority(Thread.MIN_PRIORITY);
                                  // use low priority for heavy-duty workers
    openFilesThread.start();      // run separate thread to open files, report

  } // end of doOpenButton() method


/*
  doOpenRunner() method

  This method is called inside a separate thread by the runnable interface of
  our "user" class to process the user's selected files in the context of the
  "main" class.  By doing all the heavy-duty work in a separate thread, we
  won't stall the main thread that runs the graphical interface, and we allow
  the user to cancel the processing if it takes too long.
*/
  static void doOpenRunner()
  {
    int i;                        // index variable

    /* Loop once for each file name selected.  Don't assume that these are all
    valid file names. */

    for (i = 0; i < openFileList.length; i ++)
    {
      if (cancelFlag) break;      // exit <for> loop if cancel or fatal error
      processFileOrFolder(openFileList[i]); // process this file or folder
    }

    /* Print a summary and scroll the output, even if we were cancelled. */

    printSummary();               // what we found and what was changed

    /* We are done.  Turn off the "Cancel" button and allow the user to click
    the "Start" button again. */

    cancelButton.setEnabled(false); // disable "Cancel" button
    openButton.setEnabled(true);  // enable "Open" button

    statusTimer.stop();           // stop updating status message by timer
    setStatusMessage(EMPTY_STATUS); // and clear any previous status message

  } // end of doOpenRunner() method


/*
  doSaveButton() method

  Ask the user for an output file name, create or replace that file, and copy
  the contents of our output text area to that file.  The output file will be
  in the default character set for the system, so if there are special Unicode
  characters in the displayed text (Arabic, Chinese, Eastern European, etc),
  then you are better off copying and pasting the output text directly into a
  Unicode-aware application like Microsoft Word.
*/
  static void doSaveButton()
  {
    FileWriter output;            // output file stream
    File userFile;                // file chosen by the user

    /* Ask the user for an output file name. */

    fileChooser.resetChoosableFileFilters(); // remove any existing filters
    fileChooser.setDialogTitle("Save Output as Text File...");
    fileChooser.setFileHidingEnabled(true); // don't show hidden files
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(false); // allow only one file
    if (fileChooser.showSaveDialog(mainFrame) != JFileChooser.APPROVE_OPTION)
      return;                     // user cancelled file selection dialog box
    userFile = fileChooser.getSelectedFile();

    /* See if we can write to the user's chosen file. */

    if (userFile.isDirectory())   // can't write to directories or folders
    {
      JOptionPane.showMessageDialog(mainFrame, (userFile.getName()
        + " is a directory or folder.\nPlease select a normal file."));
      return;
    }
    else if (userFile.isHidden()) // won't write to hidden (protected) files
    {
      JOptionPane.showMessageDialog(mainFrame, (userFile.getName()
        + " is a hidden or protected file.\nPlease select a normal file."));
      return;
    }
    else if (userFile.isFile() == false) // if file doesn't exist
    {
      /* Maybe we can create a new file by this name.  Do nothing here. */
    }
    else if (userFile.canWrite() == false) // file exists, but is read-only
    {
      JOptionPane.showMessageDialog(mainFrame, (userFile.getName()
        + " is locked or write protected.\nCan't write to this file."));
      return;
    }
    else if (JOptionPane.showConfirmDialog(mainFrame, (userFile.getName()
      + " already exists.\nDo you want to replace this with a new file?"))
      != JOptionPane.YES_OPTION)
    {
      return;                     // user cancelled file replacement dialog
    }

    /* Write lines to output file. */

    try                           // catch file I/O errors
    {
      output = new FileWriter(userFile); // try to open output file
      outputText.write(output);   // couldn't be much easier for writing!
      output.close();             // try to close output file
    }
    catch (IOException ioe)
    {
      putOutput("Can't write to text file: " + ioe.getMessage());
    }
  } // end of doSaveButton() method


/*
  hasCorrectSuffix() method

  Return true if a given string (file name) has an acceptable suffix (file type
  or file name extension), and false otherwise.
*/
  static boolean hasCorrectSuffix(String input)
  {
    int i;                        // index variable
    String lower;                 // caller's input converted to lowercase

    if ((suffixFlag == false) || (suffixList.length == 0)) // is there a list?
      return(true);               // no, then all file types are allowed
    lower = input.toLowerCase();  // compare file types in lowercase
    for (i = 0; i < suffixList.length; i ++) // for each acceptable file type
      if (lower.endsWith(suffixList[i])) // does input match this file type?
        return(true);             // yes, then accept this file name
    return(false);                // no, did not match any file type
  }


/*
  isSuffixDelimiter() method

  Return true if a given character is a delimiter for the parseSuffixList()
  method, and false otherwise.
*/
  static boolean isSuffixDelimiter(char ch)
  {
    return((ch == ' ') || (ch == '+') || (ch == ',') || (ch == ':')
      || (ch == ';') || (ch == '|'));
  }


/*
  parseSuffixList() method

  Given a string with a list of file types (file name extensions) separated by
  delimiters (i.e., spaces), parse this into a lowercase array with one file
  type per element.  We don't do regular expressions or wild cards.
*/
  static void parseSuffixList(String input)
  {
    StringBuffer buffer;          // faster than String for multiple appends
    char ch;                      // one character from input string
    int i;                        // index variable
    int length;                   // size of input string in characters
    Vector list;                  // temporary vector of file types
    String lower;                 // caller's input converted to lowercase

    lower = input.toLowerCase();  // we want all file types in lowercase
    length = lower.length();      // get size of input string in characters

    buffer = new StringBuffer();  // allocate empty string buffer for result
    i = 0;                        // start with first input character
    list = new Vector();          // start with an empty vector of file types
    while (i < length)            // while there are more characters to do
    {
      while ((i < length) && isSuffixDelimiter(ch = lower.charAt(i)))
        i ++;                     // ignore leading spaces (delimiters)
      buffer.setLength(0);        // empty any previous contents of buffer
      while ((i < length) && ! isSuffixDelimiter(ch = lower.charAt(i)))
      {
        buffer.append(ch);        // copy one character to buffer
        i ++;                     // this character has been consumed
      }
      if (buffer.length() > 0)    // did we find anything between spaces?
        list.add(buffer.toString()); // yes, copy to temporary vector
    }
    suffixList = (String[]) list.toArray(new String[0]); // vector to array

  } // end of parseSuffixList() method


/*
  printSummary() method

  Tell the user what we found and what was changed.
*/
  static void printSummary()
  {
    putOutput("Found " + formatComma.format(totalFiles)
      + ((totalFiles == 1) ? " file" : " files") + " in "
      + formatComma.format(totalFolders)
      + ((totalFolders == 1) ? " folder" : " folders") + ": "
      + formatComma.format(totalCorrect)
      + ((totalCorrect == 1) ? " was" : " were") + " correct and "
      + formatComma.format(totalError) + " had errors.");
  }


/*
  processFileOrFolder() method

  The caller gives us a Java File object that may be a file, a folder, or just
  random garbage.  Search all files.  Get folder contents and process each file
  found, doing subfolders only if the <recurseFlag> is true.
*/
  static void processFileOrFolder(File givenFile)
  {
    BufferedInputStream byteStream; // input stream for raw data bytes
    File canon;                   // full directory resolution of <givenFile>
    int ch;                       // one input byte or character (as integer)
    BufferedReader charStream;    // input stream for decoded characters
    File[] contents;              // contents if <givenFile> is a folder
//  boolean firstFlag;            // true only for the first byte/character
    int foundChar;                // first bad character found, if any
    boolean foundSpace;           // true if trailing white space found
    String givenName;             // caller's file name only, without path
    String givenPath;             // name of caller's file, including path
    int i;                        // index variable
    File next;                    // next File object from <contents>
    boolean whitePending;         // true if pending white space

    if (cancelFlag) return;       // stop if user cancel or fatal error

    /* Decide what kind of File object this is, if it's even real!  We process
    all files/folders given to us, no matter whether they are hidden or not.
    It's only when we look at subfolders that we pay attention to <hiddenFlag>
    and <recurseFlag>. */

    try { canon = givenFile.getCanonicalFile(); } // full directory search
    catch (IOException ioe) { canon = givenFile; } // accept abstract file
    givenName = canon.getName();  // get the file name only
    givenPath = canon.getPath();  // get file name with path
    setStatusMessage(givenPath);  // use name with path for status text

    /* Most of the work is done later in this method.  Search through folders
    and subfolders here, eliminate File objects that don't exist, and leave
    only real files for later. */

    if (canon.isDirectory())      // is this a folder?
    {
      totalFolders ++;            // one more folder or subfolder found
      putOutput("Searching folder " + givenPath);
      contents = sortFileList(canon.listFiles()); // sorted, no filter
      for (i = 0; i < contents.length; i ++) // for each file in order
      {
        if (cancelFlag) return;   // stop if user cancel or fatal error
        next = contents[i];       // get next File object from <contents>
        if (next.isHidden() && (hiddenFlag == false))
        {
          if (showOtherFlag)
            putOutput(next.getName() + " - ignoring hidden file or subfolder");
        }
        else if (next.isDirectory()) // is this a subfolder (in the folder)?
        {
          if (recurseFlag)        // should we look at subfolders?
            processFileOrFolder(next); // yes, search this subfolder
          else if (showOtherFlag)
            putOutput(next.getName() + " - ignoring subfolder");
        }
        else if (next.isFile())   // is this a file (in the folder)?
        {
          if (hasCorrectSuffix(next.getName())) // does file have correct type?
            processFileOrFolder(next); // yes, call ourself to do this file
//        else if (showOtherFlag)
//          putOutput(next.getName() + " - ignoring file");
        }
        else
        {
          /* File or folder does not exist.  Ignore without comment. */
        }
      }
      return;                     // folder is complete
    }
    else if (canon.isFile() == false) // most likely does not exist
    {
      putOutput(givenName + " - not a file or folder");
//    cancelFlag = true;          // don't do anything more
      totalError ++;              // count as error, even if don't know reason
      return;
    }
    totalFiles ++;                // one more file found

    /* We have a file to check.  Accept all files here, because the directory
    search above removed those we shouldn't see. */

    try                           // catch I/O errors (file not found, etc)
    {
//    firstFlag = true;           // true only for the first byte/character
      foundChar = -1;             // no bad characters found yet
      foundSpace = whitePending = false; // no white space found yet

      /* Open the file.  We have to deal with two different readers, one for
      bytes and one for characters. */

      byteStream = null;          // there is no byte stream yet
      charStream = null;          // there is no character stream yet
      if (encodeName.equals(LOCAL_ENCODING)) // use local system's encoding?
      {
        charStream = new BufferedReader(new FileReader(canon));
      }
      else if (encodeName.equals(RAW_ENCODING)) // use raw bytes as characters?
      {
        byteStream = new BufferedInputStream(new FileInputStream(canon));
      }
      else                        // must be some named character set encoding
      {
        charStream = new BufferedReader(new InputStreamReader(new
          FileInputStream(canon), encodeName));
      }

      /* Read until we find both problems or reach the end-of-file.  At least
      one of <plainFlag> or <trimFlag> must be true.  Otherwise, this <while>
      loop does nothing and assumes success. */

      while ((cancelFlag == false)
        && ((plainFlag && (foundChar < 0))
          || (trimFlag && (foundSpace == false))))
      {
        if (byteStream != null)   // are we reading raw data bytes?
          ch = byteStream.read(); // read one byte as integer
        else                      // no, reading character text
          ch = charStream.read(); // read one character as integer
        if (ch < 0) break;        // exit from <while> loop on end-of-file

        if ((ch == 0x0A) || (ch == 0x0D)) // check for newlines before spaces
        {
          foundSpace |= whitePending; // remember trailing white space
          whitePending = false;   // cancel pending white space, if any
        }
        else if ((ch == 0x09) || (ch == 0x20) || (ch == 0x3000))
                                  // short list of Unicode spaces, tabs
                                  // see also: isSpaceChar() isWhitespace()
        {
          whitePending = trimFlag; // there is pending white space
        }
        else if ((ch >= 0x21) && (ch <= 0x7E)) // printable US-ASCII text?
        {
          whitePending = false;   // cancel pending white space, if any
        }
//      else if ((ch == 0xFEFF) && firstFlag) // Unicode "byte order mark"
//      {
          /* Ignore Unicode "byte order mark" (BOM, U+FEFF), but only at the
          beginning of a file. */
//      }
        else                      // character is not plain text
        {
          if (plainFlag && (foundChar < 0)) // first not plain character?
            foundChar = ch;       // yes, remember first bad character
          if ((ch != 0x00) && (ch != 0x7F)) // for all but NUL and DEL ...
            whitePending = false; // cancel pending white space, if any
        }
//      firstFlag = false;        // not the first byte/character anymore
      }
      if (byteStream != null) byteStream.close(); // close byte file if open
      if (charStream != null) charStream.close(); // close char file if open
      foundSpace |= whitePending; // some files end with white space

      /* Decide what to say about this file. */

      if (cancelFlag) return;     // stop if user cancel or fatal error
      if ((foundChar >= 0) || foundSpace) // was anything bad found?
      {
        if (showFailureFlag)
          putOutput(givenName
            + ((foundChar >= 0) ? (" - invalid character, 0x"
              + Integer.toHexString(foundChar).toUpperCase()) : "")
            + (foundSpace ? " - trailing spaces or tabs" : ""));
        totalError ++;            // one more file with an error
      }
      else                        // if no trailing white space found
      {
        if (showSuccessFlag)
          putOutput(givenName + " - is " + textType);
        totalCorrect ++;          // one more file that was correct
      }
    }
    catch (UnsupportedEncodingException uee) // instance of IOException
    {
      putOutput(givenName + " - invalid character set name <" + encodeName
        + ">");
      cancelFlag = true;          // don't do anything more
      totalError ++;              // one more file with an error
    }
    catch (IOException ioe)       // file may be locked, invalid, etc
    {
      putOutput(givenName + " - " + ioe.getMessage());
//    cancelFlag = true;          // don't do anything more
      totalError ++;              // one more file with an error
    }
  } // end of processFileOrFolder() method


/*
  putOutput() method

  Append a complete line of text to the end of the output text area.  We add a
  newline character at the end of the line, not the caller.  By forcing all
  output to go through this same method, one complete line at a time, the
  generated output is cleaner and can be redirected.

  The output text area is forced to scroll to the end, after the text line is
  written, by selecting character positions that are much too large (and which
  are allowed by the definition of the JTextComponent.select() method).  This
  is easier and faster than manipulating the scroll bars directly.  However, it
  does cancel any selection that the user might have made, for example, to copy
  text from the output area.
*/
  static void putOutput(String text)
  {
    if (mainFrame == null)        // during setup, there is no GUI window
      System.out.println(text);   // console output goes onto standard output
    else
    {
      outputText.append(text + "\n"); // graphical output goes into text area
      outputText.select(999999999, 999999999); // force scroll to end of text
    }
  }


/*
  setStatusMessage() method

  Set the text for the status message if we are running as a GUI application.
  This gives the user some indication of our progress when processing is slow.
  If the update timer is running, then this message will not appear until the
  timer kicks in.  This prevents the status from being updated too often, and
  hence being unreadable.
*/
  static void setStatusMessage(String text)
  {
    if (mainFrame == null)        // are we running as a console application?
      return;                     // yes, console doesn't show running status
    statusPending = text;         // always save caller's status message
    if (statusTimer.isRunning())  // are we updating on a timed basis?
      return;                     // yes, wait for the timer to do an update
    statusDialog.setText(statusPending); // show the status message now
  }


/*
  showHelp() method

  Show the help summary.  This is a UNIX standard and is expected for all
  console applications, even very simple ones.
*/
  static void showHelp()
  {
    System.err.println();
    System.err.println(PROGRAM_TITLE);
    System.err.println();
    System.err.println("  java  CheckPlainTrim2  [options]  [fileOrFolderNames]");
    System.err.println();
    System.err.println("Options:");
    System.err.println("  -? = -help = show summary of command-line syntax");
    System.err.println("  -e\"name\" = name of character set (text encoding) for reading files");
    System.err.println("  -f\"string\" = list of file types (file name extensions), separated by spaces");
    System.err.println("  -m1 = look for plain text only, ignore trimmed");
    System.err.println("  -m2 = look for trimmed text only, ignore plain");
    System.err.println("  -m3 = look for plain and trimmed text (default)");
    System.err.println("  -s0 = do only given files or folders, no subfolders (default)");
    System.err.println("  -s1 = -s = process files, folders, and subfolders");
    System.err.println("  -u# = font size for buttons, dialogs, etc; default is local system;");
    System.err.println("      example: -u16");
    System.err.println("  -w(#,#,#,#) = normal window position: left, top, width, height;");
    System.err.println("      example: -w(50,50,700,500)");
    System.err.println("  -x = maximize application window; default is normal window");
    System.err.println();
    System.err.println("Output may be redirected with the \">\" operator.  If no file or folder names");
    System.err.println("are given on the command line, then a graphical interface will open.");
    System.err.println();
    System.err.println(COPYRIGHT_NOTICE);
//  System.err.println();

  } // end of showHelp() method


/*
  sortFileList() method

  When we ask for a list of files or subfolders in a directory, the list is not
  likely to be in our preferred order.  Java does not guarantee any particular
  order, and the observed order is whatever is supplied by the underlying file
  system (which can be very jumbled for FAT16/FAT32).  We would like the file
  names to be sorted, and since we recurse on subfolders, we also want the
  subfolders to appear in order.

  The caller's parameter may be <null> and this may happen if the caller asks
  File.listFiles() for the contents of a protected system directory.  All calls
  to listFiles() in this program are wrapped inside a call to us, so we replace
  a null parameter with an empty array as our result.
*/
  static File[] sortFileList(File[] input)
  {
    String fileName;              // file name without the path
    int i;                        // index variable
    TreeMap list;                 // our list of files
    File[] result;                // our result
    StringBuffer sortKey;         // created sorting key for each file

    if (input == null)            // were we given a null pointer?
      result = new File[0];       // yes, replace with an empty array
    else if (input.length < 2)    // don't sort lists with zero or one element
      result = input;             // just copy input array as result array
    else
    {
      /* First, create a sorted list with our choice of index keys and the File
      objects as data.  Names are sorted as files or folders, then in lowercase
      to ignore differences in uppercase versus lowercase, then in the original
      form for systems where case is distinct. */

      list = new TreeMap();       // create empty sorted list with keys
      sortKey = new StringBuffer(); // allocate empty string buffer for keys
      for (i = 0; i < input.length; i ++)
      {
        sortKey.setLength(0);     // empty any previous contents of buffer
        if (input[i].isDirectory()) // is this "file" actually a folder?
          sortKey.append("2 ");   // yes, put subfolders after files
        else                      // must be a file or an unknown object
          sortKey.append("1 ");   // put files before subfolders

        fileName = input[i].getName(); // get the file name without the path
        sortKey.append(fileName.toLowerCase()); // start by ignoring case
        sortKey.append(" ");      // separate lowercase from original case
        sortKey.append(fileName); // then sort file name on original case
        list.put(sortKey.toString(), input[i]); // put file into sorted list
      }

      /* Second, now that the TreeMap object has done all the hard work of
      sorting, pull the File objects from the list in order as determined by
      the sort keys that we created. */

      result = (File[]) list.values().toArray(new File[0]);
    }
    return(result);               // give caller whatever we could find

  } // end of sortFileList() method


/*
  userButton() method

  This method is called by our action listener actionPerformed() to process
  buttons, in the context of the main CheckPlainTrim2 class.
*/
  static void userButton(ActionEvent event)
  {
    Object source = event.getSource(); // where the event came from
    if (source == cancelButton)   // "Cancel" button
    {
      doCancelButton();           // stop opening files or folders
    }
    else if (source == checkBothButton) // text types that we can look for
    {
      plainFlag = true;         // look for plain text
      textType = TEXT_BOTH;     // description
      trimFlag = true;          // look for trimmed text
    }
    else if (source == checkPlainButton)
    {
      plainFlag = true;         // look for plain text
      textType = TEXT_PLAIN;    // description
      trimFlag = false;         // ignore trimmed text
    }
    else if (source == checkTrimButton)
    {
      plainFlag = false;        // ignore plain text
      textType = TEXT_TRIM;     // description
      trimFlag = true;          // look for trimmed text
    }
    else if (source == encodeDialog) // character set or text encoding
    {
      encodeCheckbox.setSelected(true); // any change forces selection
    }
    else if (source == exitButton) // "Exit" button
    {
      System.exit(0);             // always exit with zero status from GUI
    }
    else if (source == fontNameDialog) // font name for output text area
    {
      /* We can safely assume that the font name is valid, because we obtained
      the names from getAvailableFontFamilyNames(), and the user can't edit
      this dialog field. */

      fontName = (String) fontNameDialog.getSelectedItem();
      outputText.setFont(new Font(fontName, Font.PLAIN, fontSize));
    }
    else if (source == fontSizeDialog) // point size for output text area
    {
      /* We can safely parse the point size as an integer, because we supply
      the only choices allowed, and the user can't edit this dialog field. */

      fontSize = Integer.parseInt((String) fontSizeDialog.getSelectedItem());
      outputText.setFont(new Font(fontName, Font.PLAIN, fontSize));
    }
    else if (source == openButton) // "Open" button for files or folders
    {
      doOpenButton();             // open files or folders for processing
    }
    else if (source == recurseCheckbox) // if we search folders and subfolders
    {
      recurseFlag = recurseCheckbox.isSelected();
    }
    else if (source == saveButton) // "Save Output" button
    {
      doSaveButton();             // write output text area to a file
    }
    else if (source == showDialog) // which files or conditions to report
    {
      showFailureFlag = showOtherFlag = showSuccessFlag = false; // disable all
      switch (showDialog.getSelectedIndex())
      {                           // same index order as SHOW_CHOICES
        case (0):                 // show all files
          showFailureFlag = showOtherFlag = showSuccessFlag = true; // enable
          break;
        case (1):                 // show correct only
          showSuccessFlag = true;
          break;
        case (2):                 // show errors only
          showFailureFlag = true;
          break;
        default:
          System.err.println(
            "Error in userButton(): unknown showDialog index: "
            + showDialog.getSelectedIndex());
                                  // should never happen, so write on console
          break;
      }
    }
    else if (source == statusTimer) // update timer for status message text
    {
      if (statusPending.equals(statusDialog.getText()) == false)
        statusDialog.setText(statusPending); // new status, update the display
    }
    else if (source == suffixDialog) // list of file types
    {
      suffixCheckbox.setSelected(true); // Enter key forces selection
    }
    else                          // fault in program logic, not by user
    {
      System.err.println("Error in userButton(): unknown ActionEvent: "
        + event);                 // should never happen, so write on console
    }
  } // end of userButton() method


/*
  userDocument() method

  This method is called by our action listener when the user changes a text
  field that has a document listener.  In this application, we don't really
  care what the exact change is; we are more interested in switching radio
  buttons or checkboxes to match which fields are edited.
*/
  static void userDocument(javax.swing.text.Document source)
  {
    if (source == suffixDialog.getDocument()) // list of file types
    {
      suffixCheckbox.setSelected(true); // editing forces selection
    }
    else                          // fault in program logic, not by user
    {
      System.err.println("Error in userDocument(): unknown document object.");
                                  // should never happen, so write on console
    }
  } // end of userDocument() method

} // end of CheckPlainTrim2 class

// ------------------------------------------------------------------------- //

/*
  CheckPlainTrim2User class

  This class listens to input from the user and passes back event parameters to
  a static method in the main class.
*/

class CheckPlainTrim2User implements ActionListener, DocumentListener, Runnable
{
  /* empty constructor */

  public CheckPlainTrim2User() { }

  /* button listener, dialog boxes, etc */

  public void actionPerformed(ActionEvent event)
  {
    CheckPlainTrim2.userButton(event);
  }

  /* document listeners for changes to text fields */

  public void changedUpdate(DocumentEvent event)
  {
    /* Ignore that an attribute or set of attributes changed. */
  }

  public void insertUpdate(DocumentEvent event)
  {
    CheckPlainTrim2.userDocument(event.getDocument());
  }

  public void removeUpdate(DocumentEvent event)
  {
    CheckPlainTrim2.userDocument(event.getDocument());
  }

  /* separate heavy-duty processing thread */

  public void run()
  {
    CheckPlainTrim2.doOpenRunner();
  }

} // end of CheckPlainTrim2User class

/* Copyright (c) 2017 by Keith Fenske.  Apache License or GNU GPL. */
