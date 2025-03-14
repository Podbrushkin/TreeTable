package net.byteseek.demo.treetable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import net.byteseek.swing.treetable.TreeTableHeaderRenderer;
import net.byteseek.swing.treetable.TreeTableModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TSVTreeTable {

    public static void main(String[] args) {
        MyObjectForm.setSystemLookAndFeel();
        if (args.length < 3) {
            System.err.println("Usage: java TSVTreeTable <tsv-file> <id-column> <parent-id-column>");
            System.exit(1);
        }

        String tsvFile = args[0];
        String idColumn = args[1];
        String parentIdColumn = args[2];

        try {
            List<String[]> rows = parseTSV(tsvFile);
            DefaultMutableTreeNode root = buildTree(rows, idColumn, parentIdColumn);

            // Display the tree in a JFrame
            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("TSV TreeTable");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                TreeTableModel treeTableModel = new TSVTreeTableModel(root,rows.get(0));
                JTable table = new JTable();
                treeTableModel.bindTable(table, new TreeTableHeaderRenderer());

                frame.add(new JScrollPane(table));
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            });
        } catch (IOException e) {
            System.err.println("Error reading TSV file: " + e.getMessage());
        }
    }

    /**
     * Parses a TSV file into a list of maps, where each map represents a row with column names as keys.
     */
    private static List<String[]> parseTSV(String filePath) throws IOException {
        return Files.readAllLines(Paths.get(filePath))
            .stream()
            .map(s -> s.split("\t",-1))
            .collect(Collectors.toList());
    }

    /**
     * Builds a tree structure from the TSV records using the specified id and parentId columns.
     */
    private static DefaultMutableTreeNode buildTree(List<String[]> rows, String idColumn, String parentIdColumn) {
        Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<>();
        
    
        List<String> headers = List.of(rows.get(0));
        int idIndex = headers.indexOf(idColumn);
        int parentIdIndex = headers.indexOf(parentIdColumn);

        if (idIndex == -1 || parentIdIndex == -1) {
            throw new IllegalArgumentException("idColumnName or parentIdColumnName not found in TSV header");
        }
        
        String[] rootRow = new String[headers.size()];
        rootRow[idIndex] = "Virtual Root";
        DefaultMutableTreeNode virtualRoot = new DefaultMutableTreeNode(rootRow);
    
        // Create nodes for all records
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            String id = row[idIndex];
            String parentId = row[parentIdIndex];
    
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(row);
            nodeMap.put(id, node);
    
            if (parentId == null || parentId.isEmpty()) {
                virtualRoot.add(node); // Add to virtual root if no parent
            }
        }
    
        // Build the tree structure
        Set<String> danglingParentIds = new HashSet<>();
        for (int i = 1; i < rows.size(); i++) {
            String[] row = rows.get(i);
            String parentId = row[parentIdIndex];
            if (parentId != null && !parentId.isEmpty()) {
                DefaultMutableTreeNode parentNode = nodeMap.get(parentId);
                if (parentNode != null) {
                    parentNode.add(nodeMap.get(row[idIndex]));
                } else {
                    System.out.println("Parent node not found for ID: " + parentId);
                    danglingParentIds.add(parentId);
    
                    // Create a placeholder row for the missing parent
                    String[] placeholderRow = new String[headers.size()];
                    placeholderRow[idIndex] = parentId; // Set the ID
                    placeholderRow[parentIdIndex] = "";  // Assume it has no parent
                    for (int j = 0; j < placeholderRow.length; j++) {
                        if (placeholderRow[j] == null) {
                            placeholderRow[j] = ""; // Fill empty values
                        }
                    }
    
                    // Create a new node for the placeholder parent
                    DefaultMutableTreeNode placeholderNode = new DefaultMutableTreeNode(placeholderRow);
                    nodeMap.put(parentId, placeholderNode);
    
                    // Add the current node as a child of the placeholder parent
                    placeholderNode.add(nodeMap.get(row[idIndex]));
                }
            }
        }
    
        // Add dangling parent nodes to the virtual root
        if (danglingParentIds.size() == 1 && virtualRoot.isLeaf()) {
            DefaultMutableTreeNode danglingNode = nodeMap.get(danglingParentIds.toArray()[0]);
            virtualRoot = danglingNode;
        } else {
            for (String id : danglingParentIds) {
                DefaultMutableTreeNode danglingNode = nodeMap.get(id);
                if (danglingNode != null) {
                    virtualRoot.add(danglingNode); // Add to the virtual root
                }
            }
        }
    
        return virtualRoot;
    }

    
}