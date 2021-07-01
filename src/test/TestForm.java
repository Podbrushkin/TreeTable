package test;

import net.byteseek.swing.treetable.JTreeTable;
import net.byteseek.swing.treetable.TreeTableModel;
import net.byteseek.swing.treetable.TreeTableNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class TestForm {

    private JPanel panel1;
    private JPanel rootPanel;
    private JLabel topLabel;
    private JScrollPane scrollPane;
    private JTreeTable JTreeTable1;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Test");
        frame.setContentPane(new TestForm().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents() {
        JTreeTable1 = new JTreeTable();

        TreeTableNode rootNode = new TreeTableNode(new TestClass("My first test class", 1000, false), true);
        rootNode.add(new TreeTableNode(new TestClass("First child test class", 256, true), false));
        rootNode.add(new TreeTableNode(new TestClass("Second child test class", 32, false), false));
        TreeTableNode subchildrenNode = new TreeTableNode(new TestClass("Third child with children", 16, false), true);
        subchildrenNode.add(new TreeTableNode(new TestClass("First sub child!", 9999, true), false));
        subchildrenNode.add(new TreeTableNode(new TestClass("Second sub child!!", 1111, false), false));
        rootNode.add(subchildrenNode);
        rootNode.add(new TreeTableNode(new TestClass("Fourth child test class", 32, false), false));

        TreeTableModel treeTableModel = new TestTreeTableModel(rootNode, true);
        treeTableModel.initializeTable(JTreeTable1);
    }
}
