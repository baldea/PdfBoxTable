package bma.pdfboxtable.pdf;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import bma.pdfboxtable.table.Table;
import bma.pdfboxtable.table.TableColumn;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import static bma.pdfboxtable.table.TextVerticalAlignment.BOTTOM;
import static bma.pdfboxtable.table.TextVerticalAlignment.TOP;
import static bma.pdfboxtable.table.TextAlignment.RIGHT;

/**
 * Pdf document implementation of PdfBox Document.
 *
 * @author Bogdan Aldea
 */
public class PageablePdf extends PDDocument {
    private static final float FONT_FACTOR = 1000f;
    private static final float POINTS_PER_INCH = 72f;
    private static final float MM_PER_INCH = 25.4f;
    private static final float MM_TO_POINTS = POINTS_PER_INCH / MM_PER_INCH; // 2.83
    private static final float DEFAULT_FONT_SIZE = 10f;
    private static final float DEFAULT_HEADING_FONT_SIZE = 12f;
    private static final float DEFAULT_PADDING = 5f * MM_TO_POINTS;
    private static final float DEFAULT_FOOTER_BOTTOM_PADDING = 5f * MM_TO_POINTS;
    private static final float DEFAULT_FOOTER_FONT_SIZE = 8f;
    private static final PDFont DEFAULT_FOOTER_FONT = PDType1Font.HELVETICA_OBLIQUE;

    private PDFont fontBold = PDType1Font.HELVETICA_BOLD;
    private PDFont fontNormal = PDType1Font.HELVETICA;
    private PDFont currentFont = PDType1Font.HELVETICA;
    private float currentFontSize = DEFAULT_FONT_SIZE;
    private float headingFontSize = DEFAULT_HEADING_FONT_SIZE;
    private PDFont headingFont = PDType1Font.HELVETICA_BOLD;
    private float headingBottomPadding = DEFAULT_PADDING;
    private float headingTopPadding = DEFAULT_PADDING;
    private float paragraphPadding = DEFAULT_PADDING;
    private float footerBottomPadding = DEFAULT_FOOTER_BOTTOM_PADDING;
    private float footerFontSize = DEFAULT_FOOTER_FONT_SIZE;
    private PDFont footerFont = DEFAULT_FOOTER_FONT;

    private boolean includePageNumber;

    private float currentPositionX;
    private float currentPositionY;

    private PDPage currentPage;
    private PDPageContentStream currentPageContentStream;
    private float contentTopPadding;
    private float contentRightPadding;
    private float contentBottomPadding;
    private float contentLeftPadding;

    private float pageHeight;
    private float pageWidth;

    private List<String> footerLines;

    /**
     * Create a new document specifying page size.
     *
     * @param pageWidth page with in points
     * @param pageHeight page height in points
     */
    public PageablePdf(float pageWidth, float pageHeight) {
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
    }

    /**
     * Create a new document from a COSDocument
     */
    public PageablePdf(COSDocument document) {
        super(document);

        // set page with and height from the first page
        if (!getDocumentCatalog().getAllPages().isEmpty()) {
            PDPage firstPage = (PDPage) getDocumentCatalog().getAllPages().iterator().next();
            pageHeight = firstPage.getMediaBox().getHeight();
            pageWidth = firstPage.getMediaBox().getWidth();
        }
    }

    /**
     * Check if there is enough space for a new line, is not, than go the the next page.
     *
     * @param newLineHeight the height of next line
     * @throws IOException If there is an error writing to the page contents.
     */
    public void changePageIfNeeded(float newLineHeight) throws IOException {
        if (currentPageContentStream == null) {
            return;
        }

        if (getCurrentPositionY() - newLineHeight < contentBottomPadding) {
            currentPageContentStream.close();
            addPage(getCurrentPage());
            currentPage = new PDPage(new PDRectangle(getPageWidth(), getPageHeight()));
            currentPageContentStream = new PDPageContentStream(this, currentPage);
            setCurrentPosition(getCurrentPositionX(), pageHeight - contentTopPadding - newLineHeight);
        }

    }

    public void cropCurrentPage() {
        // cropping
        PDRectangle rectangle = new PDRectangle();
        rectangle.setUpperRightY(getCurrentPage().findCropBox().getUpperRightY());
        // add one more line
        rectangle.setLowerLeftY(getCurrentPositionY() - (getCurrentFontSize() * MM_TO_POINTS));
        rectangle.setUpperRightX(getCurrentPage().findCropBox().getUpperRightX());
        rectangle.setLowerLeftX(getCurrentPage().findCropBox().getLowerLeftX());
        getCurrentPage().setCropBox(rectangle);
    }

    /**
     * Convert document on byte array.
     */
    public byte[] toByteArray() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            this.save(out);
        } catch (IOException | COSVisitorException e) {
            throw new RuntimeException("Exception while converting pdf to byte array: " + e.getMessage(), e);
        }
        return out.toByteArray();
    }

    /**
     * Set the current position at top left corner.
     *
     * By default the current position is (x,y) = (0,0) which is bottom left corner.
     */
    public void setCurrentPositionAtStartOfThePage() {
        setCurrentPositionY(pageHeight - contentTopPadding);
        setCurrentPositionX(contentLeftPadding);

    }

    /**
     * Set current coordinates and move the text position in the current page content stream.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    public void setCurrentPosition(float x, float y) {
        setCurrentPositionX(x);
        setCurrentPositionY(y);
    }

    /*
     * Close the current page content stream. This should be call when we finish writing to the document.
     *
     * @throws IOException  If there is an error closing {@link PDPageContentStream}
     */
    private void closeCurrentPageContentStream() throws IOException {
        if (currentPageContentStream != null) {
            currentPageContentStream.close();
            addPage(getCurrentPage());
        }
    }

    /**
     * @return the currentPage
     */
    public PDPage getCurrentPage() {
        if (currentPage == null) {
            currentPage = new PDPage(new PDRectangle(getPageWidth(), getPageHeight()));
        }
        return currentPage;
    }
    /**
     * @return the currentPageContentStream
     * @throws IOException If there is an error writing to the page contents.
     */
    private PDPageContentStream getCurrentPageContentStream() throws IOException {
        if (currentPageContentStream == null) {
            currentPageContentStream = new PDPageContentStream(this, getCurrentPage());
        }

        return currentPageContentStream;
    }

    /**
     * Draw heading in pdf document
     *
     * @param heading heading
     * @throws IOException If there is an error writing to the stream.
     */
    public void drawHeading(String heading) throws IOException {

        float headingHeight = calculateHeight(getHeadingFont(), getHeadingFontSize());
        changePageIfNeeded(headingHeight);

        setCurrentPosition(getContentLeftPadding(),
                getCurrentPositionY() - getHeadingTopPadding());

        getCurrentPageContentStream().beginText();
        setCurrentPosition(getContentLeftPadding(),
                getCurrentPositionY() - headingHeight);
        getCurrentPageContentStream().moveTextPositionByAmount(getCurrentPositionX(),
                getCurrentPositionY());
        getCurrentPageContentStream().setFont(getHeadingFont(),
                getHeadingFontSize());

        getCurrentPageContentStream().drawString(heading);
        getCurrentPageContentStream().endText();

        setCurrentPosition(getContentLeftPadding(), getCurrentPositionY()
                - getHeadingBottomPadding() - getHeadingTopPadding());

    }

    /**
     * Draw table at current position.
     *
     * @param table pdf table
     * @throws IOException If there is an error while drawing on the screen.
     */
    public void drawTable(Table table) throws IOException {
        drawTable(table, getCurrentPositionX(), getCurrentPositionY());
    }

    /**
     * Draw table at a specific position.
     * @param table pdf table
     * @param topLeftCornerX position x
     * @param topLeftCornerY position y
     * @throws IOException If there is an error while drawing on the screen.
     */
    public void drawTable(Table table, float topLeftCornerX, float topLeftCornerY)
            throws IOException {
        setCurrentPosition(topLeftCornerX, topLeftCornerY);

        if (table.isDrawHeaders()) {
            drawTableHeaders(table);
        }

        for (int rowNumber = 0; rowNumber < table.getContent().length; rowNumber++) {
            drawRow(table, rowNumber);
        }
    }

    private void drawTableHeaders(Table table) throws IOException {

        PDFont headerFont = getFontBold();
        float fontSize = getCurrentFontSize();
        String[][] cellsContent = new String[table.getColumns().size()][];
        int necessaryLines = 1;
        for (int cellNumber = 0; cellNumber < table.getColumns().size(); cellNumber++) {
            TableColumn column = table.getColumns().get(cellNumber);
            float contentWidth = column.getWidth() - (2 * table.getCellInsidePadding());
            List<String> lines = splitTextInLines(column.getHeader(), contentWidth, headerFont, fontSize);
            cellsContent[cellNumber] = lines.toArray(new String[0]);
            if (lines.size() > necessaryLines) {
                necessaryLines = lines.size();
            }
        }

        float lineHeight = calculateHeight(headerFont, fontSize);
        float rowHeight = (necessaryLines * lineHeight) + (2 * table.getCellInsidePadding());

        changePageIfNeeded(rowHeight);
        float topLeftCornerY = getCurrentPositionY();
        float topLeftCornerX = getCurrentPositionX();

        // draw header background
        getCurrentPageContentStream().setNonStrokingColor(table.getHeaderBackgroundColor());
        getCurrentPageContentStream().fillRect(topLeftCornerX, topLeftCornerY - rowHeight, table.getWidth(),
                rowHeight);
        getCurrentPageContentStream().setNonStrokingColor(Color.BLACK);

        // column background override header background
        drawColumnBackground(table.getColumns(), topLeftCornerX, topLeftCornerY, rowHeight);

        if (table.isDrawGrid()) {
            drawRowGrid(table.getColumns(), topLeftCornerX, topLeftCornerY, rowHeight);
        }

        drawRowContent(table, headerFont, cellsContent, lineHeight, rowHeight, false);

        // go on the next line
        setCurrentPosition(topLeftCornerX, topLeftCornerY - rowHeight);

    }

    private void drawRow(Table table, int rowNumber) throws IOException {

        PDFont rowFont = getCurrentFont();
        float fontSize = getCurrentFontSize();

        String[] row = table.getContent()[rowNumber];
        String[][] cellsContentLines = new String[row.length][];
        int necessaryLines = 1;
        for (int cellNumber = 0; cellNumber < row.length; cellNumber++) {
            String[] lines = generateRowContentLines(table, rowFont, fontSize, row, cellNumber);
            cellsContentLines[cellNumber] = lines;
            if (lines.length > necessaryLines) {
                necessaryLines = lines.length;
            }
        }

        float lineHeight = calculateHeight(rowFont, fontSize);
        float rowHeight = (necessaryLines * lineHeight) + (2 * table.getCellInsidePadding());

        changePageIfNeeded(rowHeight);
        float topLeftCornerX = getCurrentPositionX();
        float topLeftCornerY = getCurrentPositionY();

        drawColumnBackground(table.getColumns(), topLeftCornerX, topLeftCornerY, rowHeight);

        if (table.isDrawGrid()) {
            drawRowGrid(table.getColumns(), topLeftCornerX, topLeftCornerY, rowHeight);
        }

        // draw row content
        drawRowContent(table, rowFont, cellsContentLines, lineHeight, rowHeight, true);

        // go on the next line
        setCurrentPosition(topLeftCornerX, topLeftCornerY - rowHeight);

    }

    /**
     * This is splitting the row content based on available space on different lines.
     *
     * There are some limitation on next column overlap which should be addressed:
     *  - treat the case when next column vertical alignment is not BOTTOM
     *  - treat the case when column content will not fit in on line
     *  - do better space management, if next column alignment is center or right.
     *  - (optional) make to overlap more than one column
     *  In all these cases we don't do the overlap
     *
     * TODO fix all the above limitations
     */
    private String[] generateRowContentLines(Table table, PDFont rowFont, float fontSize, String[] row, int cellNumber) throws IOException {
        if (row[cellNumber] == null) {
            String[] emptyLine = {""};
            return emptyLine;
        }

        TableColumn column = table.getColumns().get(cellNumber);
        float contentWidth = column.getWidth() - (2 * table.getCellInsidePadding());
        String rowContent = row[cellNumber];

        if (!column.isOverlapNextColumn() || isLastColumn(table, cellNumber)) {
            return splitTextInLines(rowContent, contentWidth, rowFont, fontSize).toArray(new String[0]);
        }

        TableColumn nextColumn = table.getColumns().get(cellNumber + 1);

        // NOT SUPPORTED YET: other alignment
        if (nextColumn.getVerticalAlignment() != BOTTOM || nextColumn.getAlignment() != RIGHT) {
            return splitTextInLines(rowContent, contentWidth, rowFont, fontSize).toArray(new String[0]);
        }

        float nextColumnContentAvailableWidth = nextColumn.getWidth() - (2 * table.getCellInsidePadding());
        String nextColumnContent = row[cellNumber + 1];
        List<String> nextColumnLines = splitTextInLines(nextColumnContent, nextColumnContentAvailableWidth, rowFont, fontSize);

        // NOT SUPPORTED YET: next column require more than one line
        if (nextColumnLines.size() > 1) {
            return splitTextInLines(rowContent, contentWidth, rowFont, fontSize).toArray(new String[0]);
        }

        float withOverlapContentWidth = contentWidth + nextColumn.getWidth();
        List<String> lines = splitTextInLines(rowContent, withOverlapContentWidth, rowFont, fontSize);

        float lastLineWidth = calculateWidth(lines.get(lines.size() - 1), rowFont, fontSize);
        float nextColumnContentWidth = calculateWidth(nextColumnContent, rowFont, fontSize);
        if (withOverlapContentWidth < (lastLineWidth + nextColumnContentWidth)) {
            // add a new line in order to avoid the overlap
            lines.add("");
        }

        return lines.toArray(new String[0]);
    }

    private boolean isLastColumn(Table table, int cellNumber) {
        return table.getColumns().size() == cellNumber + 1;
    }

    /**
     * Draw content for all the cells in the row
     *
     * @param cellsContent matrix of lines for the table row. The first dimension is the column number and the second
     *                     dimension is the line number in the row cell
     */
    private void drawRowContent(Table table, PDFont rowFont, String[][] cellsContent,
                                float lineHeight, float rowHeight, boolean checkColumnAlignment) throws IOException {
        float rowY = getCurrentPositionY();
        for (int cellNumber = 0; cellNumber < cellsContent.length; cellNumber++) {
            TableColumn currentColumn = table.getColumns().get(cellNumber);
            // column font have priority
            PDFont cellFont = table.getColumns().get(cellNumber).getFont() != null
                    ? table.getColumns().get(cellNumber).getFont() : rowFont;

            int numberOfLinesInCell = cellsContent[cellNumber].length;
            float contentStartY = calculateRowContentStartY(rowY, currentColumn, table.getCellInsidePadding(),
                    numberOfLinesInCell, lineHeight, rowHeight, checkColumnAlignment);
            setCurrentPosition(getCurrentPositionX(), contentStartY);

            for (int cellLineNumber = 0; cellLineNumber < numberOfLinesInCell; cellLineNumber++) {
                String line = cellsContent[cellNumber][cellLineNumber];
                float lineWidth = calculateWidth(line, cellFont, getCurrentFontSize());
                setCurrentPosition(getCurrentPositionX(),
                        getCurrentPositionY() - lineHeight);
                float contentStartX = calculateRowContentStartX(getCurrentPositionX(), currentColumn,
                        lineWidth, table.getCellInsidePadding(), checkColumnAlignment);

                // draw the line
                getCurrentPageContentStream().beginText();
                getCurrentPageContentStream().moveTextPositionByAmount(contentStartX,
                        getCurrentPositionY());
                getCurrentPageContentStream().setFont(cellFont, getCurrentFontSize());
                getCurrentPageContentStream().drawString(line);
                getCurrentPageContentStream().endText();

            }
            // got the next cell
            float xIncrease = table.getColumns().get(cellNumber).getWidth();
            setCurrentPosition(getCurrentPositionX() + xIncrease, rowY);
        }
    }

    /**
     * Calculate from where we should start writing first line in the cell based on text vertical alignment.
     * @param rowHeight is the sum of 2 x cellPadding and lineHeight X maxim number of lines in the row
     */
    private float calculateRowContentStartY(float cellTopY, TableColumn column, float cellPadding,
                                            int numberOfLinesInCell, float lineHeight, float rowHeight, boolean checkColumnAlignment) {
        if (!checkColumnAlignment || column.getVerticalAlignment() == TOP) {
            return cellTopY - cellPadding;
        }

        float yDecrease; // moving down on the page
        switch (column.getVerticalAlignment()) {
            case TOP:
                yDecrease = cellPadding;
                break;
            case MIDDLE:
                yDecrease = rowHeight - cellPadding - (lineHeight * numberOfLinesInCell) / 2;
                break;
            case BOTTOM:
                yDecrease = rowHeight - cellPadding - (lineHeight * numberOfLinesInCell);
                break;
            default:
                yDecrease = 0;
                break;
        }

        return cellTopY - yDecrease;
    }

    /**
     * Calculate from where we should start writing the text in the cell based on text alignment.
     */
    private float calculateRowContentStartX(float cellLeftX, TableColumn column, float textWidth,
                                            float cellPadding, boolean checkColumnAlignment) {
        if (!checkColumnAlignment) {
            return cellLeftX + cellPadding;
        }

        float xIncrease;
        switch (column.getAlignment()) {
            case LEFT:
                xIncrease = cellPadding;
                break;
            case CENTER:
                xIncrease = (column.getWidth() - textWidth) / 2;
                break;
            case RIGHT:
                xIncrease = column.getWidth() - textWidth - cellPadding;
                break;
            default:
                xIncrease = 0;
                break;
        }
        return cellLeftX + xIncrease;
    }

    /**
     * Split the text at space (" ") on a list of lines base on maxWidth.
     *
     * TODO support other word delimiter beside space (e.g. tab)
     *
     * @throws IOException If there is an error getting the width information.
     */
    private List<String> splitTextInLines(String text, float maxWidth, PDFont font, float fontSize)
            throws IOException {
        if (text == null) {
            return Collections.singletonList("");
        }

        float stringWidth = calculateWidth(text, font, fontSize);

        if (stringWidth <= maxWidth) {
            return new ArrayList<>(Arrays.asList(text));
        }

        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        float spaceWidth = calculateWidth(" ", font, fontSize);
        float lineWidth = 0;
        StringBuilder lineBuilder = new StringBuilder();
        for (String word : words) {
            float wordWidth = calculateWidth(word, font, fontSize);
            if (lineWidth + wordWidth <= maxWidth) {
                lineBuilder.append(" ");
                lineBuilder.append(word);
                lineWidth += spaceWidth + wordWidth;
            } else {
                // add one row
                if (lineWidth == 0) {
                    // this is the first word on the line
                    lines.add(word);
                } else {
                    lines.add(lineBuilder.toString().trim());
                    lineBuilder = new StringBuilder();
                    lineBuilder.append(word);
                    lineWidth = wordWidth;
                }

            }
        }

        if (lineBuilder.length() > 0) {
            lines.add(lineBuilder.toString().trim());
        }

        return lines;
    }

    /**
     * Draw column background for one row or headers.
     */
    private void drawColumnBackground(List<TableColumn> columns, float rowTopLeftX,
                                      float rowTopLefY, float rowHeight) throws IOException {
        // draw bg color
        float cellTopLeftX = rowTopLeftX;
        for (TableColumn column : columns) {
            if (column.getBackgroundColor() != null) {

                getCurrentPageContentStream().setNonStrokingColor(column.getBackgroundColor());
                getCurrentPageContentStream().fillRect(cellTopLeftX, rowTopLefY - rowHeight,
                        column.getWidth(), rowHeight);
                getCurrentPageContentStream().setNonStrokingColor(Color.BLACK);

            }
            cellTopLeftX += column.getWidth();
        }
    }

    /**
     *
     * Draw grid for a row table.
     */
    private void drawRowGrid(List<TableColumn> columns, float topLeftCornerX,
                             float topLeftCornerY, float rowHeight) throws IOException {
        float rowTopLeftCornerX = topLeftCornerX;
        boolean previousColumnDrawGrid = false;
        for (TableColumn column : columns) {
            boolean currentDrawGrid = !column.isHideGrid();
            if (currentDrawGrid || previousColumnDrawGrid) {
                // left line
                getCurrentPageContentStream().drawLine(rowTopLeftCornerX, topLeftCornerY, rowTopLeftCornerX,
                        topLeftCornerY - rowHeight);
            }
            if (currentDrawGrid) {
                // top line
                getCurrentPageContentStream().drawLine(rowTopLeftCornerX, topLeftCornerY,
                        rowTopLeftCornerX + column.getWidth(), topLeftCornerY);
                // bottom line
                getCurrentPageContentStream().drawLine(rowTopLeftCornerX, topLeftCornerY - rowHeight,
                        rowTopLeftCornerX + column.getWidth(), topLeftCornerY - rowHeight);
            }
            // next
            rowTopLeftCornerX += column.getWidth();
            previousColumnDrawGrid = currentDrawGrid;
        }
        if (previousColumnDrawGrid) {
            // last right line
            getCurrentPageContentStream().drawLine(rowTopLeftCornerX, topLeftCornerY, rowTopLeftCornerX,
                    topLeftCornerY - rowHeight);
        }

    }

    /**
     * Calculate height for font and size.
     *
     * @param font font use to draw the line, If null use the current one
     * @param aFontSize font size
     * @return height of the string
     * @throws IOException If there is an error getting the width information.
     */
    public float calculateHeight(PDFont font, float aFontSize) {
        return font.getFontDescriptor().getFontBoundingBox().getHeight() / FONT_FACTOR * aFontSize;
    }

    /**
     * Calculate necessary with for the text using specified font and size.
     *
     * @param string the string for which we want to calculate width
     * @param font font use to write the string
     * @param aFontSize font size
     * @return the width of the string
     * @throws IOException If there is an error getting the width information.
     */
    public float calculateWidth(String string, PDFont font, float aFontSize) throws IOException {
        return font.getStringWidth(string) / FONT_FACTOR * aFontSize;
    }

    /**
     * This will load a document from an input stream.
     *
     * @param input The stream that contains the document.
     * @return The document that was loaded.
     * @throws IOException If there is an error reading from the stream.
     */
    public static PageablePdf load(InputStream input) throws IOException {
        PDDocument pdDocument = PDDocument.load(input, null);
        return new PageablePdf(pdDocument.getDocument());
    }

    /**
     * This method should be call before saving the pdf. It is responsible for closing the current page content stream.
     * adding the current page to the document and drawing the header and footer.
     */
    public void closeDocument() throws IOException {
        closeCurrentPageContentStream();
        drawHeaderAndFooter();
    }

    /**
     * Draw header and footer on each page. This should be called before closing saving the document.
     *
     * @throws IOException If the underlying stream has a problem being written to.
     */
    protected void drawHeaderAndFooter() throws IOException {
        for (int i = 0; i < getDocumentCatalog().getAllPages().size(); i++) {
            PDPage page = (PDPage) getDocumentCatalog().getAllPages().get(i);
            try (PDPageContentStream contentStream = new PDPageContentStream(this, page, true, true, true)) {
                drawPageHeader(contentStream);
                drawPageFooter(contentStream, i + 1);
            }
        }
    }

    /**
     * By default we don't add any header to the document, but if we want a header you have to override it.
     * If we override we have to set the top padding big enough to accommodate it.
     *
     * TODO add a setter for headers so we can add a header without extend this class.
     */
    protected void drawPageHeader(PDPageContentStream contentStream) {
        // do nothing
    }

    protected void drawPageFooter(PDPageContentStream pageContentStream, int pageNumber)
            throws IOException {

        float lineHeight = calculateHeight(footerFont, footerFontSize);
        float lineY = lineHeight * getFooterLines().size() + footerBottomPadding;

        if (includePageNumber) {
            lineY += lineHeight;
        }

        for (String line : getFooterLines()) {
            float lineWidth = calculateWidth(line, footerFont, footerFontSize);
            pageContentStream.beginText();
            pageContentStream.setFont(footerFont, footerFontSize);
            pageContentStream.moveTextPositionByAmount((getPageWidth() - lineWidth) / 2, lineY);
            pageContentStream.drawString(line);
            pageContentStream.endText();

            lineY = lineY - lineHeight;
        }

        if (includePageNumber) {
            drawPageNumber(pageContentStream, pageNumber, lineY);
        }
    }

    protected void drawPageNumber(PDPageContentStream pageContentStream, int pageNumber, float lineY) throws IOException {
        String pageNumberMessage =
                Integer.toString(pageNumber) + " / " + this.getDocumentCatalog().getAllPages().size();
        float lineWidth = calculateWidth(pageNumberMessage, footerFont, footerFontSize);
        pageContentStream.beginText();
        pageContentStream.setFont(footerFont, footerFontSize);
        pageContentStream.moveTextPositionByAmount((getPageWidth() - lineWidth) / 2, lineY);
        pageContentStream.drawString(pageNumberMessage);
        pageContentStream.endText();
    }

    public void setFooterLines(List<String> footerLines) {
        this.footerLines = footerLines;
    }

    protected List<String> getFooterLines() {
        return footerLines != null ? footerLines : Collections.emptyList();
    }

    /**
     * @return the currentPositionX
     */
    public float getCurrentPositionX() {
        return currentPositionX;
    }

    /**
     * @param currentPositionX the currentPositionX to set
     */
    public void setCurrentPositionX(float currentPositionX) {
        this.currentPositionX = currentPositionX;
    }

    /**
     * @return the currentPositionY
     */
    public float getCurrentPositionY() {
        return currentPositionY;
    }

    /**
     * @param currentPositionY the currentPositionY to set
     */
    public void setCurrentPositionY(float currentPositionY) {
        this.currentPositionY = currentPositionY;
    }


    public PDFont getFontBold() {
        return fontBold;
    }

    public void setFontBold(PDFont fontBold) {
        this.fontBold = fontBold;
    }

    public PDFont getCurrentFont() {
        return currentFont;
    }

    public void setCurrentFont(PDFont currentFont) {
        this.currentFont = currentFont;
    }

    public float getCurrentFontSize() {
        return currentFontSize;
    }

    public void setCurrentFontSize(float currentFontSize) {
        this.currentFontSize = currentFontSize;
    }

    public float getPageHeight() {
        return pageHeight;
    }

    public float getPageWidth() {
        return pageWidth;
    }

    public float getContentTopPadding() {
        return contentTopPadding;
    }

    public void setContentTopPadding(float contentTopPadding) {
        this.contentTopPadding = contentTopPadding;
    }

    public float getContentRightPadding() {
        return contentRightPadding;
    }

    public void setContentRightPadding(float contentRightPadding) {
        this.contentRightPadding = contentRightPadding;
    }

    public float getContentBottomPadding() {
        return contentBottomPadding;
    }

    public void setContentBottomPadding(float contentBottomPadding) {
        this.contentBottomPadding = contentBottomPadding;
    }

    public float getContentLeftPadding() {
        return contentLeftPadding;
    }

    public void setContentLeftPadding(float contentLeftPadding) {
        this.contentLeftPadding = contentLeftPadding;
    }

    public float getHeadingFontSize() {
        return headingFontSize;
    }

    public void setHeadingFontSize(float headingFontSize) {
        this.headingFontSize = headingFontSize;
    }

    public PDFont getHeadingFont() {
        return headingFont;
    }

    public void setHeadingFont(PDFont headingFont) {
        this.headingFont = headingFont;
    }

    public float getHeadingBottomPadding() {
        return headingBottomPadding;
    }

    public void setHeadingBottomPadding(float headingBottomPadding) {
        this.headingBottomPadding = headingBottomPadding;
    }

    public float getHeadingTopPadding() {
        return headingTopPadding;
    }

    public void setHeadingTopPadding(float headingTopPadding) {
        this.headingTopPadding = headingTopPadding;
    }

    public float getParagraphPadding() {
        return paragraphPadding;
    }

    public void setParagraphPadding(float paragraphPadding) {
        this.paragraphPadding = paragraphPadding;
    }

    public PDFont getFontNormal() {
        return fontNormal;
    }

    public void setFontNormal(PDFont fontNormal) {
        this.fontNormal = fontNormal;
    }

    public float getFooterBottomPadding() {
        return footerBottomPadding;
    }

    public void setFooterBottomPadding(float footerBottomPadding) {
        this.footerBottomPadding = footerBottomPadding;
    }

    public float getFooterFontSize() {
        return footerFontSize;
    }

    public void setFooterFontSize(float footerFontSize) {
        this.footerFontSize = footerFontSize;
    }

    public PDFont getFooterFont() {
        return footerFont;
    }

    public void setFooterFont(PDFont footerFont) {
        this.footerFont = footerFont;
    }

    public boolean isIncludePageNumber() {
        return includePageNumber;
    }

    public void setIncludePageNumber(boolean includePageNumber) {
        this.includePageNumber = includePageNumber;
    }
}
