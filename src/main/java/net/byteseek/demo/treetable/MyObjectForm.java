/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2021, Matt Palmer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.byteseek.demo.treetable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import net.byteseek.swing.treetable.TreeTableHeaderRenderer;
import net.byteseek.swing.treetable.TreeTableModel;
import net.byteseek.swing.treetable.TreeUtils;

//TODO: toggle button for group by.

//TODO: remove dependency on intellij classes (or find maven dependencies).  Needs to build on anyone's machine,
//      and not just with intellij.  Maybe change to using absolutely standard swing components.

public class MyObjectForm {

    public static final int MAX_LEVELS = 3;
    public static final int MAX_CHILDREN = 10;
    public static final int CHANCE_OUT_OF_TEN_FOR_CHILDREN = 5;

    public static void main(String[] args) {
        setSystemLookAndFeel();
        JFrame frame = new JFrame("TreeTable");
        MyObjectForm form = new MyObjectForm();
        form.buildGui();
        form.initForm();
        frame.setContentPane(form.rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private static final Predicate<TreeNode> NODE_FILTER = treeNode -> ((MyObject) ((DefaultMutableTreeNode) treeNode).getUserObject()).getDescription().contains("s");

    private TreeTableModel treeTableModel;
    private DefaultTreeModel treeModel;

    private JPanel panel1;
    private JPanel rootPanel;
    private JScrollPane scrollPane;
    private JTable table1;
    private JButton showRootButton;
    private JButton insertButton;
    private JButton deleteButton;
    private JButton groupToggleButton;
    private JButton toggleFilterButton;
    private Random random;
    private List<String> wordList;
    private boolean showRoot;

    public MyObjectForm() {
    }

    public void buildGui() {
        rootPanel = new JPanel(new BorderLayout()); // Use BorderLayout for the root panel
        panel1 = new JPanel(new BorderLayout()); // Use BorderLayout for the main panel
        scrollPane = new JScrollPane();
        table1 = new JTable();
        showRootButton = new JButton("Show Root");
        insertButton = new JButton("Insert");
        deleteButton = new JButton("Delete");
        toggleFilterButton = new JButton("Toggle Filter");
    
        // Create a panel for the buttons and use FlowLayout to arrange them horizontally
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(showRootButton);
        buttonPanel.add(insertButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(toggleFilterButton);
    
        // Add the table and buttons to the main panel
        panel1.add(scrollPane, BorderLayout.CENTER); // Table takes the center
        panel1.add(buttonPanel, BorderLayout.SOUTH); // Buttons go below the table
    
        // Add the main panel to the root panel
        rootPanel.add(panel1, BorderLayout.CENTER); // Main panel takes the whole window
    
        // Set the table as the view for the scroll pane
        scrollPane.setViewportView(table1);
    }

    /**
     * Split the initialization into a separate method, as had bugs with NullPointerExceptions due to components
     * not yet initialized when trying to use member components in the constructor.  The setup method is auto
     * generated by the Swing form designer in IntelliJ, doesn't seem to guarantee when it will run, or it's being
     * re-ordered somehow due to incorrect optimisation or something.  Anyway, safer to initialize after construction.
     */
    public void initForm() {
        MyObject rootObject = buildRandomTree(MAX_LEVELS, CHANCE_OUT_OF_TEN_FOR_CHILDREN);
        TreeNode rootNode = TreeUtils.buildTree(rootObject, MyObject::getChildren, parent -> parent.getChildren().size() > 0);
        table1.setRowHeight(24);
        treeTableModel = createTreeTableModel(rootNode);
        treeModel = createTreeModel(rootNode);
        addButtonActionListeners();
    }

    private void addButtonActionListeners() {
        showRootButton.addActionListener(e -> treeTableModel.setShowRoot(!treeTableModel.getShowRoot()));

        insertButton.addActionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treeTableModel.getSelectedNode();
            MyObject newObject = new MyObject(getRandomDescription(), random.nextInt(100000000), random.nextBoolean());
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newObject, random.nextBoolean());
            if (treeTableModel.isExpanded(selectedNode)) { // If expanded, insert as first child:
                treeModel.insertNodeInto(newNode, selectedNode, 0);
            } else { // Insert as next sibling of selected node.
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
                if (parentNode != null) {
                    int insertIndex = parentNode.getIndex(selectedNode) + 1;
                    treeModel.insertNodeInto(newNode, parentNode, insertIndex);
                }
            }
        });

        deleteButton.addActionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) treeTableModel.getSelectedNode();
            if (selectedNode != treeTableModel.getRoot()) { // can't remove root node - will get illegal argument exception from a tree model.  use set root to change to a new root.
                treeModel.removeNodeFromParent(selectedNode);
            }
        });

        toggleFilterButton.addActionListener(e -> treeTableModel.setNodeFilter(treeTableModel.isFiltering() ? null : NODE_FILTER));
    }

    private DefaultTreeModel createTreeModel(TreeNode rootNode) {
        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        model.addTreeModelListener(treeTableModel);
        return model;
    }

    private TreeTableModel createTreeTableModel(TreeNode rootNode) {
        TreeTableModel localTreeTableModel = new MyObjectTreeTableModel(rootNode, showRoot);

        TreeTableHeaderRenderer renderer = new TreeTableHeaderRenderer();
        renderer.setShowNumber(true); // true is default, this code is just for testing the false option.

        localTreeTableModel.bindTable(table1, renderer); //, new RowSorter.SortKey(0, SortOrder.ASCENDING));
        localTreeTableModel.addExpandCollapseListener(new TreeTableModel.ExpandCollapseListener() {
            @Override
            public boolean nodeExpanding(TreeNode node) {
                if (node.getChildCount() == 0) { // if a node is expanding but has no children, set it to allow no children.
                    ((DefaultMutableTreeNode) node).setAllowsChildren(false);
                }
                return true;
            }

            @Override
            public boolean nodeCollapsing(TreeNode node) {
                return true;
            }
        });
        return localTreeTableModel;
    }

    private MyObject buildRandomTree(int maxLevels, int chanceOutOfTenForChildren) {
        random = new Random(1086);
        readWordList();
        MyObject rootObject = new MyObject(getRandomDescription(), random.nextInt(100000000), random.nextBoolean());
        buildRandomChildren(rootObject, maxLevels, chanceOutOfTenForChildren, 0, true);
        return rootObject;
    }

    private void buildRandomChildren(MyObject parent, int maxLevels, int chanceOutOfTenForChildren, int level, boolean forceChildren) {
        boolean hasChildren = level <= maxLevels && random.nextInt(10) < chanceOutOfTenForChildren;
        if (hasChildren || forceChildren) { // force children for root to ensure we get a tree and not a root without children.
            int numChildren = random.nextInt(MAX_CHILDREN) + 1;
            for (int child = 0; child < numChildren; child++) {
                MyObject childObject = new MyObject(getRandomDescription(), random.nextInt(100000000), random.nextBoolean());
                parent.addChild(childObject);
                buildRandomChildren(childObject, maxLevels, chanceOutOfTenForChildren, level + 1, false);
            }
        }
    }

    private void readWordList() {
        try {
            wordList = Files.readAllLines(Paths.get(getFilePath("/wordlist.txt")));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(panel1, e.getMessage());
            System.exit(1);
        }
    }

    private String getRandomDescription() {
        return wordList.get(random.nextInt(wordList.size())) + ' ' + wordList.get(random.nextInt(wordList.size()));
    }

    private String getFilePath(final String resourceName) {
        return new File(this.getClass().getResource(resourceName).getFile()).toPath().toString();
    }

    static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
