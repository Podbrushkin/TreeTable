package net.byteseek.swing.treetable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;

public class TreeTableCellRenderer extends DefaultTableCellRenderer implements TreeTableModel.TreeClickHandler {

    private static final int PADDING = 4;  // how many pixels to pad left and right, so display looks nice.
    private static final int DEFAULT_PIXELS_PER_LEVEL = 18; // It's a bit subjective... 16 felt too small, 20 too big.

    private final TreeTableModel treeTableModel;
    private final Insets insets = new Insets(0, 0, 0, 0);

    private int pixelsPerLevel = DEFAULT_PIXELS_PER_LEVEL;

    private Icon expandedIcon;          // expand icon, dependent on the look and feel theme.
    private Icon collapsedIcon;         // collapse icon, dependent on the look and feel theme.

    // We have labels for each of the icons, because for some reason GTK icons won't paint on Graphic objects,
    // but when embedded in a JLabel it paints fine.  They seem to be some kind of Icon proxy object...
    private JLabel expandedIconLabel;
    private JLabel collapsedIconLabel;

    // Calculated max icon width of the expand and collapse icons, to get consistent indentation levels.
    private int maxIconWidth;

    private TreeTableNode currentNode; // The node about to be rendered.

    public TreeTableCellRenderer(final TreeTableModel treeTableModel) {
        this.treeTableModel = treeTableModel;
        setExpandedIcon(UIManager.getIcon("Tree.expandedIcon"));
        setCollapsedIcon(UIManager.getIcon("Tree.collapsedIcon"));
        setBorder(new ExpandHandleBorder());
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
                                                   final boolean hasFocus, final int row, final int column) {
        setForeground(isSelected? table.getSelectionForeground() : table.getForeground());
        setBackground(isSelected? table.getSelectionBackground() : table.getBackground());
        setFont(table.getFont());
        setValue(value);
        setToolTipText(value.toString());
        final TreeTableNode node = getNodeAtRow(table, row);
        setNode(node);
        setNodeIcon(node);
        return this;
    }

    @Override
    public boolean clickOnExpand(TreeTableNode node, int column, MouseEvent evt) {
        TreeTableModel localModel = treeTableModel;
        final int columnModelIndex = localModel.getTableColumnModel().getColumn(column).getModelIndex();
        if (columnModelIndex == localModel.getTreeColumn() && node != null & node.getAllowsChildren()) {
            final int columnStart = localModel.calculateWidthToLeft(column);
            final int expandEnd = getNodeIndent(node);
            final int mouseX = evt.getPoint().x;
            if (mouseX > columnStart && mouseX < columnStart + expandEnd) {
                return true;
            }
        }
        return false;
    }

    private void setNodeIcon(TreeTableNode node) {
        setIcon(treeTableModel.getNodeIcon(node));
    }

    public int getPixelsPerLevel() {
        return pixelsPerLevel;
    }

    public void setPixelsPerLevel(int pixelsPerLevel) {
        this.pixelsPerLevel = pixelsPerLevel;
    }

    public void setExpandedIcon(final Icon expandedIcon) {
        if (expandedIconLabel == null) {
            expandedIconLabel = new JLabel(expandedIcon, JLabel.RIGHT);
            expandedIconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, PADDING));
        } else {
            expandedIconLabel.setIcon(expandedIcon);
        }
        this.expandedIcon = expandedIcon;
        setMaxIconWidth();
    }

    public void setCollapsedIcon(final Icon collapsedIcon) {
        if (collapsedIconLabel == null) {
            collapsedIconLabel = new JLabel(collapsedIcon, JLabel.RIGHT);
            collapsedIconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, PADDING));
        } else {
            collapsedIconLabel.setIcon(collapsedIcon);
        }
        this.collapsedIcon = expandedIcon;
        setMaxIconWidth();
    }

    protected TreeTableNode getNodeAtRow(final JTable table, final int row) {
        return treeTableModel.getNodeAtTableRow(table, row);
    }

    protected int getNodeIndent(final TreeTableNode node) {
        final int adjustShowRoot = treeTableModel.getShowRoot()? 0 : 1;
        return PADDING + maxIconWidth + ((node.getLevel() - adjustShowRoot) * pixelsPerLevel);
    }

    private void setNode(final TreeTableNode node) {
        insets.left = getNodeIndent(node);
        currentNode = node;
    }

    private void setMaxIconWidth() {
        int expandedWidth = expandedIcon == null? 0 : expandedIcon.getIconWidth();
        int collapsedWidth = collapsedIcon == null? 0 : collapsedIcon.getIconWidth();
        maxIconWidth = Math.max(expandedWidth, collapsedWidth);
    }

    /**
     * A class which provides an expanded border to render the tree expand/collapse handle icon, indented by insets.left.
     */
    private class ExpandHandleBorder implements Border {

        @Override
        public void paintBorder(final Component c, final Graphics g, final int x, final int y,
                                final int width, final int height) {
            if (currentNode.getAllowsChildren()) {
                final JLabel labelToPaint = currentNode.isExpanded() ? expandedIconLabel : collapsedIconLabel;
                labelToPaint.setSize(insets.left, height);
                labelToPaint.paint(g);
            }
        }

        @Override
        public Insets getBorderInsets(final Component c) {
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }

}
