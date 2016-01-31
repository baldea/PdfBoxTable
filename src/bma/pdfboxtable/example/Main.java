package bma.pdfboxtable.example;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bma.pdfboxtable.pdf.PageablePdf;
import bma.pdfboxtable.table.Table;
import bma.pdfboxtable.table.TableColumn;
import bma.pdfboxtable.table.TextAlignment;
import bma.pdfboxtable.table.TextVerticalAlignment;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 *  Example of how to create a pdf and add tables.
 *
 * @author Bogdan Aldea
 */
public class Main {

    public static void main(String[] args) throws IOException, COSVisitorException {
        PageablePdf pdf = new PageablePdf(PDPage.PAGE_SIZE_A4.getWidth(), PDPage.PAGE_SIZE_A4.getHeight());

        pdf.setFooterLines(Arrays.asList("This document is an example of how to create PDF with tables."));
        pdf.setContentLeftPadding(20f);
        pdf.setContentTopPadding(10f);
        pdf.setContentBottomPadding(40f); // This include the footer too
        pdf.setIncludePageNumber(true);

        pdf.setCurrentPositionAtStartOfThePage();

        appendTableWithVerticalGap(pdf);
        appendTableWithOptionalColumn(pdf);
        appendTableOnTwoPages(pdf);

        pdf.closeDocument();

        pdf.save("PdfBoxTableExample.pdf");

    }

    private static void appendTableOnTwoPages(PageablePdf pageablePdf) throws IOException {
        pageablePdf.drawHeading("Table on 2 pages");

        String[][] tableContent = {{"Rosemary Olson", "1"},
                {"Phil Turner", "2"},
                {"Joyce Chambers", "3"},
                {"Tara Coleman", "4"},
                {"Jessie Padilla", "5"},
                {"Carla Wilkerson", "1"},
                {"Jacqueline Bridges", "2"},
                {"Ana Adams", "3"},
                {"Dewey Nash", "4"},
                {"Nora Dean", "5"},
                {"Antonio Martinez", "1"},
                {"Sidney Shaw", "2"},
                {"Irma Ramos", "3"},
                {"Carol Knight", "4"},
                {"Tom Stanley", "5"},
                {"Richard Johnnie Matthews", "1"},
                {"Penny Glover", "2"},
                {"Rodolfo Smith", "3"},
                {"Joanne Edwards", "4"},
                {"Henrietta Adrienne Summers", "5"},
                {"Julio Delgado", "1"},
                {"Charles Davis", "2"},
                {"Ross Jordan", "3"},
                {"Olive Dunn", "4"},
                {"Laurie Leonard", "5"},
                {"George Harper", "1"},
                {"Jan Waters", "2"},
                {"Jane Scott", "3"},
                {"Marty Holloway", "4"},
                {"Gladys Snyder", "5"},
                {"Leslie Schneider", "1"},
                {"Van Alvarado", "2"},
                {"May Sharp", "3"},
                {"Arnold Rowe", "4"},
                {"Belinda Huff", "5"},
                {"Constance Charlene Douglas", "1"},
                {"Pamela Myers", "2"},
                {"Dorothy Curry", "3"},
                {"Irene Andrews", "4"},
                {"Jesse Sandoval", "5"},
                {"Winifred Cruz", "1"},
                {"Marco Pierce", "2"},
                {"Antonia Bowman", "3"},
                {"Roberta Carter", "4"},
                {"Kelvin Gilbert", "5"},
                {"Steve Cole", "1"},
                {"Lamar Garcia", "2"},
                {"Mable Potter", "3"},
                {"Ira Rodriguez", "4"},
                {"Mario Alvarez", "5"}};

        List<TableColumn> tableColumns = new ArrayList<>();
        tableColumns.add(new TableColumn("Employee", 100F));
        tableColumns.add(new TableColumn("Department ID", 100F));

        Table table = new Table(tableColumns, tableContent);
        table.setDrawHeaders(true);
        table.setCellInsidePadding(3f);
        table.setDrawGrid(true);

        pageablePdf.drawTable(table);
    }

    private static void appendTableWithVerticalGap(PageablePdf pageablePdf) throws IOException {
        pageablePdf.drawHeading("Table with vertical gap");

        String[][] tableContent = {{"Item 1", "1", "$100.00", "$20.00", "$2.00", "", "$78.00"},
                {"Item 2", "1", "$50.00", "$10.00", "$1.00", "", "$39.00"},
                {"Item 3 with a very long name", "1", "$10.00", "$2.00", "$0.20", "", "$7.80"},
                {"Item 4", "2", "$150.00", "$30.00", "$3.00", "", "$117.00"}
        };

        List<TableColumn> tableColumns = new ArrayList<>();

        tableColumns.add(new TableColumn("Item Name", 100F));
        tableColumns.add(new TableColumn("Qty", 25F, TextAlignment.CENTER));
        tableColumns.add(new TableColumn("Sale Price", 75F, TextAlignment.RIGHT));

        TableColumn commissionColumn = new TableColumn("Commission", 75F, TextAlignment.RIGHT);
        commissionColumn.setBackgroundColor(new Color(166, 166, 166));
        tableColumns.add(commissionColumn);

        tableColumns.add(new TableColumn("Vat @ 10%", 40F, TextAlignment.RIGHT));

        TableColumn gapColumn = new TableColumn("", 30F);
        gapColumn.setHideGrid(true);
        gapColumn.setBackgroundColor(Color.WHITE);
        tableColumns.add(gapColumn);

        tableColumns.add(new TableColumn("Amount due", 100F, TextAlignment.RIGHT));

        Table table = new Table(tableColumns, tableContent);
        table.setDrawHeaders(true);
        table.setCellInsidePadding(3f);
        table.setDrawGrid(true);

        pageablePdf.drawTable(table);

    }


    private static void appendTableWithOptionalColumn(PageablePdf pageablePdf) throws IOException {

        pageablePdf.drawHeading("Table with overlapping optional columns");


        String[][] tableContent = {{"Product 1", "2 X $10.00", "$20.00"},
                {"Fancy Product with long name", "", "$10.00"},
                {"Fancy Product with long name and quantity", "3 X $10.00", "$30.00"}
        };

        List<TableColumn> tableColumns = new ArrayList<>();

        TableColumn productPrice = new TableColumn("", 100F);
        productPrice.setOverlapNextColumn(true);
        tableColumns.add(productPrice);

        tableColumns.add(new TableColumn(60F, TextAlignment.RIGHT, TextVerticalAlignment.BOTTOM)); // quantity
        tableColumns.add(new TableColumn(50F, TextAlignment.RIGHT, TextVerticalAlignment.BOTTOM)); // price

        Table table = new Table(tableColumns, tableContent);
        table.setCellInsidePadding(2f);

        pageablePdf.drawTable(table);
    }

}
