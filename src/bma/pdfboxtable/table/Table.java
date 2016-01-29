package bma.pdfboxtable.table;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

/**
 * Table content for a pdf document.
 *
 * @author Bogdan Aldea
 */
public class Table {
    private static final Color DEFAULT_HEADER_BACKGROUND_COLOR = new Color(224, 224, 224);

    private List<TableColumn> columns;
    private String[][] content;
    private Float width;
    private boolean drawGrid;
    private boolean drawHeaders;
    private float cellInsidePadding;
    private Color headerBackgroundColor = DEFAULT_HEADER_BACKGROUND_COLOR;

    /**
     * @param columns table columns
     * @param content table content
     */
    public Table(List<TableColumn> columns, String[][] content) {
        super();
        this.columns = columns;
        this.content = content;
    }

    /**
     * @param column table column
     * @param content table content
     * constructor for building individual rows
     */
    public Table(TableColumn column, String content) {
        super();
        this.columns = Arrays.asList(column);
        this.content = new String[][] {{content}};
    }

    /**
     * @param column table column
     * @param rows table content (only one column)
     */
    public Table(TableColumn column, List<String> rows) {
        super();
        this.columns = Arrays.asList(column);
        this.content = new String[rows.size()][];

        for (int rowNumber = 0; rowNumber < rows.size(); rowNumber++) {
            String[] row = new String[1];
            row[0] = rows.get(rowNumber);
            content[rowNumber] = row;
        }

    }

    /**
     * @return table width
     */
    public float calculateWidth() {
        float columnsWidth = 0;
        for (TableColumn column : columns) {
            columnsWidth += column.getWidth();
        }
        return columnsWidth;
    }

    /**
     * @return the width
     */
    public Float getWidth() {
        if (width == null) {
            width = calculateWidth();
        }
        return width;
    }

    /**
     * @return the columns
     */
    public List<TableColumn> getColumns() {
        return columns;
    }

    /**
     * @return the content
     */
    public String[][] getContent() {
        return content;
    }

    /**
     * @return the drawGrid
     */
    public boolean isDrawGrid() {
        return drawGrid;
    }

    /**
     * @param drawGrid the drawGrid to set
     */
    public void setDrawGrid(boolean drawGrid) {
        this.drawGrid = drawGrid;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(Float width) {
        this.width = width;
    }

    /**
     * @return the drawHeaders
     */
    public boolean isDrawHeaders() {
        return drawHeaders;
    }

    /**
     * @param drawHeaders the drawHeaders to set
     */
    public void setDrawHeaders(boolean drawHeaders) {
        this.drawHeaders = drawHeaders;
    }

    /**
     * @param cellInsidePadding the cellInsidePadding to set
     */
    public void setCellInsidePadding(float cellInsidePadding) {
        this.cellInsidePadding = cellInsidePadding;
    }

    /**
     * @return the cellInsidePadding
     */
    public float getCellInsidePadding() {
        return cellInsidePadding;
    }

    /**
     * @param cells list of row cells
     * @return row array
     */
    public static String[] generateRow(String... cells) {
        return cells;
    }

    /**
     * @return the headerBackgroundColor
     */
    public Color getHeaderBackgroundColor() {
        return headerBackgroundColor;
    }

    /**
     * @param headerBackgroundColor the headerBackgroundColor to set
     */
    public void setHeaderBackgroundColor(Color headerBackgroundColor) {
        this.headerBackgroundColor = headerBackgroundColor;
    }

}
