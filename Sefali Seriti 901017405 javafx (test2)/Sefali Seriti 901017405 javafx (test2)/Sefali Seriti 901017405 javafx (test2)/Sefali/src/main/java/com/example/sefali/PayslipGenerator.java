package com.example.sefali;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PayslipGenerator {
    public static void generatePayslip(Employee employee, Payroll payroll)
            throws IOException, DocumentException {
        // Create document with proper page size and margins
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        String filename = "payslip_" + employee.getId() + "_"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + ".pdf";

        try {
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            addTitle(document);
            addEmployeeInfo(document, employee);
            addSalaryDetails(document, payroll);
            addFooter(document);
        } finally {
            if (document != null && document.isOpen()) {
                document.close();
            }
        }
    }

    private static void addTitle(Document document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLUE);
        Paragraph title = new Paragraph("MONTHLY PAYSLIP", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
    }

    private static void addEmployeeInfo(Document document, Employee employee) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(20);

        addCell(table, "Employee ID:", String.valueOf(employee.getId()));
        addCell(table, "Name:", employee.getFirstName() + " " + employee.getLastName());
        addCell(table, "Position:", employee.getPosition());
        addCell(table, "Department:", employee.getDepartment());
        addCell(table, "Pay Period:", LocalDate.now().getMonth().toString());

        document.add(table);
    }

    private static void addSalaryDetails(Document document, Payroll payroll) throws DocumentException {
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Paragraph heading = new Paragraph("Salary Details:", boldFont);
        heading.setSpacingAfter(10);
        document.add(heading);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        addCell(table, "Basic Salary:", String.format("$%.2f", payroll.getBasicSalary()));
        addCell(table, "Allowances:", String.format("$%.2f", payroll.getAllowances()));
        addCell(table, "Deductions:", String.format("$%.2f", payroll.getDeductions()));

        PdfPCell totalLabel = new PdfPCell(new Phrase("Net Salary:", boldFont));
        totalLabel.setBorder(Rectangle.NO_BORDER);
        totalLabel.setPadding(5);
        table.addCell(totalLabel);

        PdfPCell totalValue = new PdfPCell(new Phrase(
                String.format("$%.2f", payroll.getFinalSalary()),
                boldFont));
        totalValue.setBorder(Rectangle.NO_BORDER);
        totalValue.setPadding(5);
        table.addCell(totalValue);

        document.add(table);
    }

    private static void addFooter(Document document) throws DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.DARK_GRAY);
        Paragraph footer = new Paragraph(
                "Generated on: " + LocalDate.now().format(DateTimeFormatter.ISO_DATE) +
                        "\n© Company Name",
                footerFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
    }

    private static void addCell(PdfPTable table, String header, String value) {
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        PdfPCell headerCell = new PdfPCell(new Phrase(header, cellFont));
        headerCell.setBackgroundColor(new BaseColor(240, 240, 240)); // Light gray
        headerCell.setBorderWidth(1);
        headerCell.setPadding(5);
        table.addCell(headerCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, cellFont));
        valueCell.setBorderWidth(1);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }
}