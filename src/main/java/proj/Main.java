package proj;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Main {
    // JDBC URL, username, and password
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/notedb";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "rootpassword";

    // Retrieve notes from the database and update the JList
    private static void refreshNotes(JList<String> notesList, DefaultListModel<String> listModel) throws SQLException {
        listModel.clear(); // Clear existing notes
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
            String sql = "SELECT * FROM notes";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    String title = resultSet.getString("title");
                    listModel.addElement(title); // Add each note to the list
                }
            }
        }
    }

    public static void main(String[] args) {
        // Create a JFrame
        JFrame frame = new JFrame("Note Taking App");

        // Set layout to BorderLayout
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.WHITE); // Set background color

        // Create panel for notes list
        JPanel notesListPanel = new JPanel();
        notesListPanel.setLayout(new BorderLayout());
        notesListPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding

        // Create list model
        DefaultListModel<String> listModel = new DefaultListModel<>();

        // Create JList to display notes
        JList<String> notesList = new JList<>(listModel);
        notesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notesList.setVisibleRowCount(10); // Display more rows
        JScrollPane listScrollPane = new JScrollPane(notesList);
        listScrollPane.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding

        // Create a button to add new note
        JButton newNoteButton = new JButton("New Note");
        newNoteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newNote = JOptionPane.showInputDialog(frame, "Enter the new note:");
                if (newNote != null && !newNote.isEmpty()) {
                    try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
                        String sql = "INSERT INTO notes (title, content) VALUES (?, ?)";
                        try (PreparedStatement statement = connection.prepareStatement(sql)) {
                            statement.setString(1, newNote);
                            statement.setString(2, ""); // Empty content initially
                            statement.executeUpdate();
                            refreshNotes(notesList, listModel); // Refresh notes in the list
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // Add the button and the list to the panel
        notesListPanel.add(newNoteButton, BorderLayout.NORTH);
        notesListPanel.add(listScrollPane, BorderLayout.CENTER);

        // Create panel for displaying and editing notes
        JPanel noteDisplayPanel = new JPanel();
        noteDisplayPanel.setLayout(new BorderLayout());
        noteDisplayPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding

        // Create text area for displaying and editing notes
        JTextArea noteTextArea = new JTextArea();
        noteTextArea.setFont(new Font("Arial", Font.PLAIN, 16)); // Increase font size
        noteTextArea.setBorder(new LineBorder(Color.BLACK)); // Set black border
        JScrollPane textAreaScrollPane = new JScrollPane(noteTextArea);
        textAreaScrollPane.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding
        noteDisplayPanel.add(textAreaScrollPane, BorderLayout.CENTER);

        // Add a listener to the list selection to display the selected note in the text area
        notesList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedTitle = notesList.getSelectedValue();
                try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
                    String sql = "SELECT * FROM notes WHERE title = ?";
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        statement.setString(1, selectedTitle);
                        ResultSet resultSet = statement.executeQuery();
                        if (resultSet.next()) {
                            String content = resultSet.getString("content");
                            noteTextArea.setText(content); // Set content in the text area
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Add a document listener to the text area to update the content in the database
        noteTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateContent();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateContent();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateContent();
            }

            private void updateContent() {
                String selectedTitle = notesList.getSelectedValue();
                String updatedContent = noteTextArea.getText();
                if (selectedTitle != null && !selectedTitle.isEmpty()) {
                    try (Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)) {
                        String sql = "UPDATE notes SET content = ? WHERE title = ?";
                        try (PreparedStatement statement = connection.prepareStatement(sql)) {
                            statement.setString(1, updatedContent);
                            statement.setString(2, selectedTitle);
                            statement.executeUpdate(); // Update content in the database
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // Add panels to the frame
        frame.add(notesListPanel, BorderLayout.WEST);
        frame.add(noteDisplayPanel, BorderLayout.CENTER);

        // Set the size of the frame
        frame.setSize(800, 600); // Increase the size of the frame

        // Set the default close operation
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the visibility of the frame
        frame.setVisible(true);

        // Initial loading of notes from the database
        try {
            refreshNotes(notesList, listModel);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
