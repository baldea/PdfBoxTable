package bma.pdfboxtable.table;

import java.awt.Color;

import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Display information for a column in {@link Table}.
 *
 * @author Bogdan Aldea
 */
public class TableColumn {
    private String header;
    private float width;
    private TextAlignment alignment = TextAlignment.LEFT;
    private TextVerticalAlignment verticalAlignment = TextVerticalAlignment.TOP;
    private Color backgroundColor;
    private PDFont font;
    private boolean hideGrid;

    /**
     * This should be used only when next column have an optional value and only if:
     *  - the text on the next column will not require 2 lines
     *  - next column vertical alignment is BOTTOM
     *
     *  TODO - make it usable without the above restrictions
     */
    private boolean overlapNextColumn;

    /**
     * @param header columns header
     * @param width column width
     */
    public TableColumn(String header, float width) {
        super();
        this.header = header;
        this.width = width;
    }

    /**
     * @param header columns header
     * @param width column width
     */
    public TableColumn(String header, float width, TextAlignment alignment) {
        super();
        this.header = header;
        this.width = width;
        this.alignment = alignment;
    }

    public TableColumn(float width, TextAlignment alignment, TextVerticalAlignment verticalAlignment) {
        this.width = width;
        this.alignment = alignment;
        this.verticalAlignment = verticalAlignment;
    }

    /**
     * @return the header
     */
    public String getHeader() {
        return header;
    }

    /**
     * @param header the header to set
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * @return the width
     */
    public float getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * @return the alignment
     */
    public TextAlignment getAlignment() {
        return alignment;
    }

    /**
     * @param newAlignment the alignment to set
     * @return Same object
     */
    public TableColumn setAlignment(TextAlignment newAlignment) {
        this.alignment = newAlignment;
        return this;
    }

    /**
     * @return the backgroundColor
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * @param newBackgroundColor the backgroundColor to set
     * @return Same object
     */
    public TableColumn setBackgroundColor(Color newBackgroundColor) {
        this.backgroundColor = newBackgroundColor;
        return this;
    }

    /**
     * @return the font
     */
    public PDFont getFont() {
        return font;
    }

    /**
     * @param font the font to set
     * @return Same object
     */
    public TableColumn setFont(PDFont font) {
        this.font = font;
        return this;
    }

    /**
     * @return the hideGrid
     */
    public boolean isHideGrid() {
        return hideGrid;
    }

    /**
     * @param hideGrid the hideGrid to set
     * @return Same object
     */
    public TableColumn setHideGrid(boolean hideGrid) {
        this.hideGrid = hideGrid;
        return this;
    }

    public TextVerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(TextVerticalAlignment verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public boolean isOverlapNextColumn() {
        return overlapNextColumn;
    }

    public void setOverlapNextColumn(boolean overlapNextColumn) {
        this.overlapNextColumn = overlapNextColumn;
    }
}
