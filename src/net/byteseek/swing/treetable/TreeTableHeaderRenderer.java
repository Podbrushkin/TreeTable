package net.byteseek.swing.treetable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.List;

/**
 * Renders a header for a JTable which shows multi-column sorting, putting a number for each sort key against the
 * icon for ascending or descending.
 */
public class TreeTableHeaderRenderer extends JLabel implements TableCellRenderer {

    private static final int ICON_AND_NUMBER_WIDTH = 32; // hard coded value for ascend/descend icon and text... should calculate?
    private static final int PADDING = 4; // white space padding at end of ascend/descend .

    private Icon sortAscendingIcon;
    private Icon sortDescendingIcon;
    private JLabel sortAscendingIconLabel;
    private JLabel sortDescendingIconLabel;
    private Color gridColor;
    private Color sortcolumnTextColor;
    private Font headerFont;
    private Font boldHeaderFont;
    private boolean boldOnSorted;

    private int sortColumn;
    private SortOrder sortOrder;

    /**
     * Constructs a TreeTableHeaderRenderer.
     */
    public TreeTableHeaderRenderer() {
        setSortAscendingIcon(UIManager.getIcon("Table.ascendingSortIcon"));
        setSortDescendingIcon(UIManager.getIcon("Table.descendingSortIcon"));
        setSortColumnTextColor(Color.GRAY);
        setHorizontalAlignment(JLabel.CENTER);
        setBoldOnSorted(true);
    }

    @Override
    public void setBorder(final Border border) {
        // If the border is already a compound with an inner SortIconBorder, just set it directly.
        if (border instanceof CompoundBorder && ((CompoundBorder) border).getInsideBorder() instanceof SortIconBorder) {
            super.setBorder(border);
        } else {  // Wrap the border in a CompoundBorder using the SortIconBorder inside to render sort icon and number.
            super.setBorder(new CompoundBorder(border, new SortIconBorder()));
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (table != null) {
            JTableHeader header = table.getTableHeader();
            if (header != null) {
                setFont(header.getFont());
                setForeground(header.getForeground());
                setBackground(header.getBackground());
                setText(value == null? "" : value.toString());
                setColumnSorted(table, column);
                setGridColor(table.getGridColor());
            }
        }
        return this;
    }

    @Override
    public void setFont(Font newFont) {
        if (newFont != headerFont || newFont != boldHeaderFont) {
            headerFont = newFont;
            boldHeaderFont = getBoldFont(newFont);
        }
        super.setFont(newFont);
    }

    public void setBoldOnSorted(boolean boldOnSorted) {
        this.boldOnSorted = boldOnSorted;
    }

    public boolean getBoldOnSorted() {
        return boldOnSorted;
    }

    public void setSortColumnTextColor(final Color sortcolumnTextColor) {
        this.sortcolumnTextColor = sortcolumnTextColor;
    }

    public Color getSortColumnTextColor() {
        return sortcolumnTextColor;
    }

    public void setSortAscendingIcon(final Icon sortAscendingIcon) {
        this.sortAscendingIcon = sortAscendingIcon;
        if (sortAscendingIconLabel == null) {
            sortAscendingIconLabel = new JLabel(sortAscendingIcon);
            sortAscendingIconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, PADDING));
        } else {
            sortAscendingIconLabel.setIcon(sortAscendingIcon);
        }
        //TODO: calc icon widths...?
    }

    public Icon getSortAscendingIcon() {
        return sortAscendingIcon;
    }

    public void setSortDescendingIcon(final Icon sortDescendingIcon) {
        this.sortDescendingIcon = sortDescendingIcon;
        if (sortDescendingIconLabel == null) {
            sortDescendingIconLabel = new JLabel(sortDescendingIcon);
            sortDescendingIconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, PADDING));
        } else {
            sortDescendingIconLabel.setIcon(sortDescendingIcon);
        }
        //TODO: calc icon widths...?
    }

    public Icon getSortDescendingIcon() {
        return sortDescendingIcon;
    }

    protected void setGridColor(Color gridColor) {
        if (this.gridColor != gridColor) {
            this.gridColor = gridColor;
            setBorder(new MatteBorder(1, 0, 1, 1, gridColor));
        }
    }

    protected void setColumnSorted(JTable table, int column) {
        final RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
        if (rowSorter != null) {
            List<? extends RowSorter.SortKey> sortKeys = rowSorter.getSortKeys();
            if (sortKeys != null) {
                for (int sortKeyIndex = 0; sortKeyIndex < sortKeys.size(); sortKeyIndex++) {
                    RowSorter.SortKey key = sortKeys.get(sortKeyIndex);
                    if (key.getColumn() == column && key.getSortOrder() != SortOrder.UNSORTED) {
                        sortOrder = key.getSortOrder();
                        sortColumn = sortKeyIndex;
                        setBold(true);
                        return;
                    }
                }
            }
        }
        setBold(false);
        sortOrder = SortOrder.UNSORTED;
        sortColumn = -1;
    }

    protected void setBold(final boolean bold) {
        if (boldOnSorted) {
            setFont(bold ? boldHeaderFont : headerFont);
        }
    }

    protected Font getBoldFont(Font font) {
        if ((font.getStyle() & Font.BOLD) == Font.BOLD) {
            return font;
        }
        return font.deriveFont(font.getStyle() | Font.BOLD);
    }

    /**
     * A border that leaves space to paint a sort icon and sort key number.
     */
    protected class SortIconBorder implements Border {

        private final Insets insets = new Insets(4, ICON_AND_NUMBER_WIDTH, 4, 0);

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            if (sortColumn >= 0 && sortOrder != SortOrder.UNSORTED) {
                final JLabel labelToPaint = sortOrder == SortOrder.ASCENDING ? sortAscendingIconLabel : sortDescendingIconLabel;
                labelToPaint.setSize(ICON_AND_NUMBER_WIDTH, height);
                labelToPaint.setFont(headerFont);
                labelToPaint.setText(Integer.toString(sortColumn + 1));
                labelToPaint.setForeground(sortcolumnTextColor);
                labelToPaint.paint(g);
            }
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return insets;
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
