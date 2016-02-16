# PdfBoxTable

This is an example of PdfBox implementation with support for tables.

PdfBox provides the option for adding text to a pdf and for drawing lines, but it doesn't have solution out of the box for adding a table with grind to a pdf.
It is quite simple to add a table if you know the content upfront and you know how wide your columns are and if you have enough space on the page.
PdfBoxTable can handle tables with dynamic content and know how to wrap the text inside the cell keeping the alignment between cells from the same row.

First you have to define the pdf specifying the size:
```
PageablePdf pdf = new PageablePdf(PDPage.PAGE_SIZE_A4.getWidth(), PDPage.PAGE_SIZE_A4.getHeight());
```

Then create a table by passing a list of columns and the content of the table:
```
Table table = new Table(tableColumns, tableContent);
```

And at the end you just have to draw the table:
```
pageablePdf.drawTable(table);
```

For an example please check PdfBoxTableExample.pdf

Other features include:
* display settings for each column: background colour, font, alignment (horizontal & vertical) 
* cell padding
* optional grid for a column
* column overlapping 
