package net.byteseek.demo.treetable;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreeNode;

import net.byteseek.swing.treetable.TreeTableModel;
import net.byteseek.swing.treetable.TreeUtils;

public class TSVTreeTableModel extends TreeTableModel  {
    private String[] columnNames;
    public TSVTreeTableModel(TreeNode root, String[] columnNames) {
        super(root, true);
        this.columnNames = columnNames;
    }

    @Override
    public Object getColumnValue(TreeNode node, int column) {
        try {
            String[] row = TreeUtils.getUserObject(node);
            return row[column];
        } catch (Exception e) {
            System.out.println(node);
            throw e;
        }
        
    }

    @Override
    public TableColumnModel createTableColumnModel() {
        TableColumnModel result = new DefaultTableColumnModel();
        for (int i = 0; i < columnNames.length; i++) {
            result.addColumn(createColumn(i, columnNames[i]));
        }
        return result;
    }
    
}
