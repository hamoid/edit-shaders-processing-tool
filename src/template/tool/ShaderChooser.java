package template.tool;

import processing.app.Base;
import processing.app.ui.Editor;
import processing.app.ui.Toolkit;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

// based on
// https://github.com/processing/processing/blob/master/app/src/processing/app/ui/ColorChooser.java

// TODO: use trees to show all files inside data/ ?
// https://docs.oracle.com/javase/tutorial/uiswing/components/tree.html


public class ShaderChooser {
    private JDialog window;
    private File templatesPath, sketchDataPath;
    private String editorCommand;
    private DefaultListModel userShadersModel;
    private DefaultListModel templateShadersModel;
    private DefaultListModel clickedModel;

    private JList userShadersList;
    private JList templateShadersList;
    private JList clickedList;

    private int clickedIndex;
    private String clickedPath;

    private Base base;
    private Editor editor;
    private Timer timer;
    private FileAction fileAction;

    public ShaderChooser(Base base, boolean modal,
                         ActionListener actionListener) {
        this.base = base;

        window = new JDialog(base.getActiveEditor(), "Edit Shader", modal);

        // Check if the active editor has changed every 200ms
        timer = new Timer(200, actionEvent -> {
            if (editor != base.getActiveEditor()) {
                editor = base.getActiveEditor();
                populate();
            }
        });
        timer.start();

        MouseAdapter templateShadersListClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    System.out.println("Double click on " + index);
                }
            }
        };

        fileAction = new FileAction();

        /*
  _           __   _
 | |         / _| | |
 | |   ___  | |_  | |_
 | |  / _ \ |  _| | __|
 | | |  __/ | |   | |_
 |_|  \___| |_|    \__|

         */

        JPanel paneLeft = new JPanel();
        paneLeft.setLayout(new BorderLayout());

        userShadersModel = new DefaultListModel();

        //Create the list and put it in a scroll pane.
        userShadersList = new JList(userShadersModel);
        userShadersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userShadersList.setSelectedIndex(0);
        userShadersList.addListSelectionListener(this::userShaderSelected);
        userShadersList.setVisibleRowCount(18);
        JScrollPane userShadersScroll = new JScrollPane(userShadersList);

        paneLeft.add(new JLabel("Sketch"), BorderLayout.PAGE_START);
        paneLeft.add(userShadersScroll, BorderLayout.CENTER);

        JButton editButton = new JButton("edit");
        editButton.addActionListener(this::onEditPressed);

        JButton renameButton = new JButton("rename");
        renameButton.addActionListener(this::onRenamePressed);

        JButton cloneButton = new JButton("clone");
        cloneButton.addActionListener(this::onClonePressed);

        JButton deleteButton = new JButton("delete");
        deleteButton.addActionListener(this::onDeletePressed);

        JPanel buttons1 = new JPanel();
        buttons1.setLayout(new BoxLayout(buttons1, BoxLayout.LINE_AXIS));
        buttons1.add(editButton);
        buttons1.add(Box.createHorizontalStrut(5));
        buttons1.add(renameButton);
        buttons1.add(Box.createHorizontalStrut(5));
        buttons1.add(cloneButton);
        buttons1.add(Box.createHorizontalStrut(5));
        buttons1.add(deleteButton);
        buttons1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        paneLeft.add(buttons1, BorderLayout.PAGE_END);


/*
              _       _       _   _
             (_)     | |     | | | |
  _ __ ___    _    __| |   __| | | |   ___
 | '_ ` _ \  | |  / _` |  / _` | | |  / _ \
 | | | | | | | | | (_| | | (_| | | | |  __/
 |_| |_| |_| |_|  \__,_|  \__,_| |_|  \___|

*/

        JPanel paneMid = new JPanel();
        paneMid.setLayout(new BoxLayout(paneMid, BoxLayout.PAGE_AXIS));

        JButton copyLeftButton = new JButton("  <  ");
        copyLeftButton.addActionListener(this::onCopyLeftPressed);

        JButton copyRightButton = new JButton("  >  ");
        copyRightButton.addActionListener(this::onCopyRightPressed);

        paneMid.add(copyLeftButton);
        paneMid.add(copyRightButton);

/*
         _           _       _
        (_)         | |     | |
  _ __   _    __ _  | |__   | |_
 | '__| | |  / _` | | '_ \  | __|
 | |    | | | (_| | | | | | | |_
 |_|    |_|  \__, | |_| |_|  \__|
              __/ |
             |___/

 */
        JPanel paneRight = new JPanel();
        paneRight.setLayout(new BorderLayout());

        templateShadersModel = new DefaultListModel();

        //Create the list and put it in a scroll pane.
        templateShadersList = new JList(templateShadersModel);
        templateShadersList
                .setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateShadersList.setSelectedIndex(0);
        templateShadersList
                .addListSelectionListener(this::onTemplateShaderSelected);
        templateShadersList.addMouseListener(templateShadersListClick);
        templateShadersList.setVisibleRowCount(18);
        JScrollPane templateShadersScroll =
                new JScrollPane(templateShadersList);

        paneRight.add(new JLabel("Templates"), BorderLayout.PAGE_START);
        paneRight.add(templateShadersScroll, BorderLayout.CENTER);


/*
          _   _
         | | | |
   __ _  | | | |
  / _` | | | | |
 | (_| | | | | |
  \__,_| |_| |_|


 */
        Container pane = window.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.LINE_AXIS));

        pane.add(paneLeft);
        pane.add(paneMid);
        pane.add(paneRight);

        window.pack();
        //window.setResizable(false);

        window.setLocationRelativeTo(null);

        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                hide();
            }
        });
        Toolkit.registerWindowCloseKeys(window.getRootPane(),
                actionEvent -> hide());

        Toolkit.setIcon(window);
    }

    public void show() {
        window.setVisible(true);
    }

    public void hide() {
        window.setVisible(false);
    }

    private void setTemplateShaders(File[] files) {
        templateShadersModel.clear();
        if (files != null) {
            for (File f : files) {
                templateShadersModel.addElement(f.getName());
            }
        }
    }

    private void setOwnShaders(File[] files) {
        userShadersModel.clear();
        if (files != null) {
            for (File f : files) {
                userShadersModel.addElement(f.getName());
            }
        }
    }

    public void setTemplatesPath(File templatesPath) {
        this.templatesPath = templatesPath;
    }

    public void populate() {
        sketchDataPath = base.getActiveEditor().getSketch().getDataFolder();

        try {
            // Create data folder if missing
            Files.createDirectories(sketchDataPath.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (templatesPath.exists() && templatesPath.isDirectory()) {
            File[] files = templatesPath.listFiles(
                    (dir, name) -> name.matches(".*\\.(glsl|vert|frag)$"));
            Arrays.sort(files);
            setTemplateShaders(files);
        } else {
            setTemplateShaders(null);
        }

        if (sketchDataPath.exists() && sketchDataPath.isDirectory()) {
            File[] files = sketchDataPath.listFiles(
                    (dir, name) -> name.matches(".*\\.(glsl|vert|frag)$"));
            Arrays.sort(files);
            setOwnShaders(files);
        } else {
            setOwnShaders(null);
        }

    }

    public void setEditorCommand(String command) {
        editorCommand = command;
    }


    /*
                                        _
                                       | |
           ___  __   __   ___   _ __   | |_   ___
          / _ \ \ \ / /  / _ \ | '_ \  | __| / __|
         |  __/  \ V /  |  __/ | | | | | |_  \__ \
          \___|   \_/    \___| |_| |_|  \__| |___/

    */
    private void onCopyRightPressed(ActionEvent ev) {
        if (clickedList == userShadersList) {
            fileAction.dst = new File(templatesPath.getAbsolutePath() +
                    File.separator +
                    fileAction.src.getFileName().toString()).toPath();
            fileAction.operation = FileAction.Operation.COPY;

            fileCopyOrMove();
        }
    }

    private void onCopyLeftPressed(ActionEvent ev) {
        if (clickedList == templateShadersList) {
            fileAction.dst = new File(sketchDataPath.getAbsolutePath() +
                    File.separator +
                    fileAction.src.getFileName().toString()).toPath();
            fileAction.operation = FileAction.Operation.COPY;

            fileCopyOrMove();
        }
    }

    private void fileCopyOrMove() {
        if (Files.exists(fileAction.dst)) {
            String[] options = new String[]{"Cancel", "Rename", "Overwrite"};
            int response = JOptionPane.showOptionDialog(null,
                    "Do you want to overwrite " +
                            fileAction.dst.getFileName().toString() +
                            "\nor save it with a new name?",
                    "File already exists!",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                    null, options, options[0]);

            if (response == 2) { // Overwrite
                fileAction.execute();
            } else if (response == 1) { // New name
                String fileName =
                        promptFileName(fileAction.dst.getFileName().toString());

                if (fileName != null) {
                    String path = fileAction.dst.getParent().toString();
                    fileAction.dst = new File(path +
                            File.separator + fileName).toPath();

                    fileCopyOrMove();
                }
            }

        } else {
            fileAction.execute();
            populate();
        }
    }

    private String promptFileName(String targetFileName) {
        boolean done = false;
        do {
            targetFileName = JOptionPane.showInputDialog(
                    "New file name", targetFileName);
            if (targetFileName == null) {
                done = true;
            } else if (!targetFileName.matches(".*\\.(glsl|vert|frag)$")) {
                JOptionPane.showMessageDialog(null,
                        "File extension should be " +
                                ".glsl, .vert or .frag");
            } else {
                done = true;
            }
        } while (!done);
        return targetFileName;
    }

    private void onRenamePressed(ActionEvent ev) {
        String fileName =
                promptFileName((String) clickedList.getSelectedValue());

        if (fileName != null) {
            fileAction.dst = new File(clickedPath +
                    File.separator + fileName).toPath();
            fileAction.operation = FileAction.Operation.MOVE;

            fileCopyOrMove();
        }
    }

    private void onClonePressed(ActionEvent ev) {
        String fileName =
                promptFileName((String) clickedList.getSelectedValue());

        if (fileName != null) {
            fileAction.dst = new File(clickedPath +
                    File.separator + fileName).toPath();
            fileAction.operation = FileAction.Operation.COPY;

            fileCopyOrMove();
        }
    }

    private void onDeletePressed(ActionEvent ev) {
        String[] options = new String[]{"Cancel", "Delete"};
        int response = JOptionPane.showOptionDialog(null,
                "Do you want to delete " + clickedList.getSelectedValue() +
                        "?", "Please confirm",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (response == 1) {
            try {
                Files.deleteIfExists(fileAction.src.toAbsolutePath());
                populate();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onEditPressed(ActionEvent ev) {
        try {
            String[] parts = editorCommand
                    .replace("%PATH%", clickedPath)
                    .replace("%FILE%",
                            (String) clickedList.getSelectedValue())
                    .split(",");

            Runtime.getRuntime().exec(parts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateFileAction() {
        String sourceFileName = (String) clickedList.getSelectedValue();

        fileAction.src = new File(clickedPath
                + File.separator + sourceFileName).toPath();
    }

    private void userShaderSelected(ListSelectionEvent ev) {
        clickedList = userShadersList;
        clickedIndex = ev.getFirstIndex();
        clickedModel = userShadersModel;
        clickedPath = sketchDataPath.getAbsolutePath();
        updateFileAction();

        // deselect all entries in other list
        templateShadersList.clearSelection();
    }

    private void onTemplateShaderSelected(ListSelectionEvent ev) {
        clickedList = templateShadersList;
        clickedIndex = ev.getFirstIndex();
        clickedModel = templateShadersModel;
        clickedPath = templatesPath.getAbsolutePath();
        updateFileAction();

        // deselect all entries in other list
        userShadersList.clearSelection();
    }
}
