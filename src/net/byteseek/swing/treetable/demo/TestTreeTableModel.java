package net.byteseek.swing.treetable.demo;

import net.byteseek.swing.treetable.TreeTableCellRenderer;
import net.byteseek.swing.treetable.TreeTableModel;
import net.byteseek.swing.treetable.TreeTableNode;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.util.Comparator;

public class TestTreeTableModel extends TreeTableModel {

    private static final int NUM_COLUMNS = 3;

    private final TableColumn[] TABLE_COLUMNS = new TableColumn[NUM_COLUMNS];
    private final Comparator<TreeTableNode> nodeComparator = new TestNodeComparator();

    private Icon leafIcon;              // tree node that allows no children.
    private Icon openIcon;              // tree node displaying children.
    private Icon closedIcon;            // tree node not displaying children.

    public TestTreeTableModel(TreeTableNode rootNode, boolean showRoot) {
        super(rootNode, NUM_COLUMNS, showRoot);
        setIcons();

        buildColumns();
    }

    private void setIcons() {
        if (UIManager.getLookAndFeel().getID().equals("GTK")) {
            setLeafIcon(UIManager.getIcon("FileView.fileIcon"));
            setOpenIcon(UIManager.getIcon("FileView.directoryIcon"));
            setClosedIcon(UIManager.getIcon("FileView.directoryIcon"));
        } else {
            // Leaf, open and closed icons not available in all look and feels...not in GTK, but is in metal...
            setLeafIcon(UIManager.getIcon("Tree.leafIcon"));
            setOpenIcon(UIManager.getIcon("Tree.openIcon"));
            setClosedIcon(UIManager.getIcon("Tree.closedIcon"));
        }
    }

    public Object getColumnValue(final TreeTableNode node, final int column) {
        final Object o = node.getUserObject();
        if (o instanceof TestClass) {
            final TestClass obj = (TestClass) o;
            switch (column) {
                case 0: return obj.getDescription();
                case 1: return obj.getSize();
                case 2: return obj.isEnabled();
            }
        }
        return null;
    }

    @Override
    public void setColumnValue(final TreeTableNode node, final int column, final Object value) {
        final Object o = node.getUserObject();
        if (o instanceof TestClass) {
            final TestClass obj = (TestClass) o;
            switch (column) {
                case 0: {
                    obj.setDescription((String) value);
                    break;
                }
                case 1: {
                    obj.setSize((Long) value);
                    break;
                }
                case 2: {
                    obj.setEnabled((Boolean) value);
                    break;
                }
            }
        }
    }

    @Override
    public TableColumn getTableColumn(int column) {
        return TABLE_COLUMNS[column];
    }

    @Override
    public Comparator<?> getColumnComparator(int column) {
        return null; //TODO: return comparator for column values to enable sorting.
    }

    @Override
    public Comparator<TreeTableNode> getNodeComparator() {
        return nodeComparator;
    }

    @Override
    public Icon getNodeIcon(TreeTableNode node) {
        if (node != null) {
            if (node.getAllowsChildren()) {
                return node.isExpanded() ? openIcon : closedIcon;
            }
            return leafIcon;
        }
        return null;
    }

    public void setLeafIcon(final Icon leafIcon) {
        this.leafIcon = leafIcon;
    }

    public void setClosedIcon(final Icon closedIcon) {
        this.closedIcon = closedIcon;
    }

    public void setOpenIcon(final Icon openIcon) {
        this.openIcon = openIcon;
    }

    private void buildColumns() {
        TABLE_COLUMNS[0] = createColumn("description", 0, new TreeTableCellRenderer(this));
        TABLE_COLUMNS[1] = createColumn("size", 1, null);
        TABLE_COLUMNS[2] = createColumn("enabled", 2,null);
    }

}
