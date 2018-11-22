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
import java.nio.file.Path;
import java.util.Arrays;

// based on
// https://github.com/processing/processing/blob/master/app/src/processing/app/ui/ColorChooser.java

// TODO: use trees to show all files inside data/ ?
// https://docs.oracle.com/javase/tutorial/uiswing/components/tree.html


public class ShaderChooser {
    JDialog window;
    File templatesPath, sketchDataPath;
    Path sourceFile;
    private String editorCommand;
    private DefaultListModel userShadersModel;
    private DefaultListModel templateShadersModel;
    private JList userShadersList;
    private JList templateShadersList;
    private JButton editButton;
    private JButton deleteButton;
    private JButton renameButton;
    private JButton copyRightButton;
    private JButton copyLeftButton;
    private MouseAdapter templateShadersListClick;
    private JTextField filenameTextField;
    private JButton createButton;
    private Base base;
    private Editor editor;
    private Timer timer;


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

        templateShadersListClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    System.out.println("Double click on " + index);
                }
            }
        };

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

        editButton = new JButton("edit");
        editButton.addActionListener(this::onEditPressed);

        renameButton = new JButton("rename");
        renameButton.addActionListener(this::onRenamePressed);

        deleteButton = new JButton("delete");
        deleteButton.addActionListener(this::onDeletePressed);

        JPanel buttons1 = new JPanel();
        buttons1.setLayout(new BoxLayout(buttons1, BoxLayout.LINE_AXIS));
        buttons1.add(editButton);
        buttons1.add(Box.createHorizontalStrut(5));
        buttons1.add(renameButton);
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

        copyLeftButton = new JButton("  <  ");
        copyLeftButton.addActionListener(this::onCopyLeftPressed);

        copyRightButton = new JButton("  >  ");
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

        // ---------------------------------------------
        // buttons

        createButton = new JButton("create");
        createButton.addActionListener(this::onCreatePressed);

        filenameTextField = new JTextField(10);

        JPanel buttons2 = new JPanel();
        buttons2.setLayout(new BoxLayout(buttons2, BoxLayout.LINE_AXIS));
        buttons2.add(filenameTextField);
        buttons2.add(Box.createHorizontalStrut(5));
        buttons2.add(createButton);
        buttons2.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        paneRight.add(buttons2, BorderLayout.PAGE_END);


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

    private void onCopyRightPressed(ActionEvent ev) {
    }

    private void onCopyLeftPressed(ActionEvent ev) {
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

            // Recursive version, for the future
//            try {
//                Files.walk(Paths.get(dataPath))
//                        .filter(Files::isRegularFile)
//                        .forEach(System.out::println);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            // This is not recursive
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

    private void onCreatePressed(ActionEvent actionEvent) {
        String targetFileName = filenameTextField.getText();
        if (targetFileName.length() < 6) {
            return;
        }
        if (!targetFileName.contains(".")) {
            targetFileName = targetFileName + ".glsl";
        }
        Path targetFile = new File(sketchDataPath.getAbsolutePath() +
                File.separator + targetFileName).toPath();
        try {
            Files.copy(sourceFile, targetFile);
            userShadersModel.addElement(targetFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onRenamePressed(ActionEvent ev) {
        String targetFileName = filenameTextField.getText();
        if (targetFileName.length() < 6) {
            return;
        }
        if (!targetFileName.contains(".")) {
            targetFileName = targetFileName + ".glsl";
        }
        Path targetFile = new File(sketchDataPath.getAbsolutePath() +
                File.separator + targetFileName).toPath();
        try {
            Files.move(sourceFile, targetFile);
            int index = userShadersList.getSelectedIndex();
            userShadersModel.remove(index);
            userShadersModel.addElement(targetFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onDeletePressed(ActionEvent ev) {
        try {
            Files.deleteIfExists(sourceFile.toAbsolutePath());
            int index = userShadersList.getSelectedIndex();
            userShadersModel.remove(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
        filenameTextField.setText("");
    }

    private void onEditPressed(ActionEvent ev) {
        try {
            String[] parts = editorCommand.split(",");

            // In the received string, replace placeholder variables by
            // their correct values
            for (int i = 0; i < parts.length; i++) {
                parts[i] = parts[i].replace("%PATH%",
                        sketchDataPath.getAbsolutePath());
                parts[i] = parts[i].replace("%FILE%",
                        sourceFile.getFileName().toString());
            }

            Runtime.getRuntime().exec(parts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void userShaderSelected(ListSelectionEvent ev) {
        String sourceFileName =
                (String) userShadersList.getSelectedValue();
        filenameTextField.setText(sourceFileName);
        sourceFile = new File(
                sketchDataPath.getAbsolutePath() + File.separator +
                        sourceFileName).toPath();
        editButton.setEnabled(true);
        renameButton.setEnabled(true);
        deleteButton.setEnabled(true);
    }

    private void onTemplateShaderSelected(ListSelectionEvent ev) {
        String sourceFileName =
                (String) templateShadersList.getSelectedValue();
        filenameTextField.setText(sourceFileName);
        sourceFile = new File(
                templatesPath.getAbsolutePath() + File.separator +
                        sourceFileName).toPath();
        editButton.setEnabled(false);
        renameButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
}
