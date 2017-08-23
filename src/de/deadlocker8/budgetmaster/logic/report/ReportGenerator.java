package de.deadlocker8.budgetmaster.logic.report;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import de.deadlocker8.budgetmaster.logic.Budget;
import de.deadlocker8.budgetmaster.logic.CategoryBudget;
import de.deadlocker8.budgetmaster.logic.utils.Helpers;

public class ReportGenerator
{
	private ArrayList<ReportItem> reportItems;
	private ArrayList<CategoryBudget> categoryBudgets;
	private ColumnOrder columnOrder;
	private boolean includeBudget;
	private boolean splitTable;
	private boolean includeCategoryBudgets;
	private File savePath;
	private String currency;
	private DateTime date;
	private Budget budget;

	public ReportGenerator(ArrayList<ReportItem> reportItems, ArrayList<CategoryBudget> categoryBudgets, ColumnOrder columnOrder, boolean includeBudget, boolean splitTable, boolean includeCategoryBudgets, File savePath, String currency, DateTime date, Budget budget)
	{	
		this.reportItems = reportItems;
		this.categoryBudgets = categoryBudgets;
		this.columnOrder = columnOrder;
		this.includeBudget = includeBudget;
		this.splitTable = splitTable;
		this.includeCategoryBudgets = includeCategoryBudgets;
		this.savePath = savePath;
		this.currency = currency;
		this.date = date;
		this.budget = budget;
	}

	private Chapter generateHeader()
	{
		Font chapterFont = new Font(FontFamily.HELVETICA, 16, Font.BOLDITALIC);		
		Chunk chunk = new Chunk("Monatsbericht - " + date.toString("MMMM yyyy"), chapterFont);
		Chapter chapter = new Chapter(new Paragraph(chunk), 1);
		chapter.setNumberDepth(0);
		chapter.add(Chunk.NEWLINE);		
		return chapter;
	}

	private PdfPTable generateTable(int tableWidth, AmountType amountType)
	{
		int numberOfColumns = columnOrder.getColumns().size();
		int totalIncome = 0;
		int totalPayment = 0;

		if(numberOfColumns > 0)
		{
			float[] proportions = new float[numberOfColumns];
			for(int i = 0; i < columnOrder.getColumns().size(); i++)
			{
				proportions[i] = columnOrder.getColumns().get(i).getProportion();
			}
			
			PdfPTable table = new PdfPTable(proportions);
			table.setWidthPercentage(tableWidth);
			Font font = new Font(FontFamily.HELVETICA, 8, Font.NORMAL, GrayColor.BLACK);

			for(ColumnType column : columnOrder.getColumns())
			{
				PdfPCell cell = new PdfPCell(new Phrase(column.getName(), font));
				cell.setBackgroundColor(GrayColor.LIGHT_GRAY);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);
			}

			for(ReportItem currentItem : reportItems)
			{
				if(currentItem.getAmount() > 0)
				{
					totalIncome += currentItem.getAmount();
					if(amountType == AmountType.PAYMENT)
					{
						continue;
					}
				}
				else
				{
					totalPayment += currentItem.getAmount();
					if(amountType == AmountType.INCOME)
					{
						continue;
					}
				}

				for(ColumnType column : columnOrder.getColumns())
				{
					PdfPCell cell = new PdfPCell(new Phrase(getProperty(currentItem, column), font));
					cell.setBackgroundColor(new BaseColor(Color.WHITE));
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					table.addCell(cell);
				}
			}

			PdfPCell cellTotal;
			String total = "";
			switch(amountType)
			{
				case BOTH:
					String totalIncomeString = Helpers.getCurrencyString(totalIncome, currency);
					String totalPaymentString = Helpers.getCurrencyString(totalPayment, currency);
					total = "Einnahmen: " + totalIncomeString + " / Ausgaben: " + totalPaymentString;
					break;
				case INCOME:
					total = "Summe: " + Helpers.getCurrencyString(totalIncome, currency);
					break;
				case PAYMENT:
					total = "Summe: " + Helpers.getCurrencyString(totalPayment, currency);
					break;
				default:
					break;
			}

			cellTotal = new PdfPCell(new Phrase(total, font));
			cellTotal.setBackgroundColor(new BaseColor(Color.WHITE));
			cellTotal.setColspan(numberOfColumns);
			cellTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
			table.addCell(cellTotal);

			return table;
		}
		return null;
	}

	public void generate() throws FileNotFoundException, DocumentException
	{
		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(savePath));
		writer.setPageEvent(new HeaderFooterPageEvent());
		document.open();
		document.setMargins(50, 45, 50, 70);
		Font headerFont = new Font(FontFamily.HELVETICA, 14, Font.BOLD);
		Font smallHeaderFont = new Font(FontFamily.HELVETICA, 12, Font.BOLD);

		document.add(generateHeader());
		document.add(Chunk.NEWLINE);
		
		if(includeBudget)
		{
			Font fontGreen = new Font(FontFamily.HELVETICA, 12, Font.NORMAL, new BaseColor(36, 122, 45));
			Font fontRed = new Font(FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.RED);
			Font fontBlack = new Font(FontFamily.HELVETICA, 12, Font.BOLD);
			document.add(new Paragraph("Budget", headerFont));
			document.add(Chunk.NEWLINE);
			document.add(new Paragraph("Einnahmen: " + Helpers.getCurrencyString(budget.getIncomeSum(), currency), fontGreen));
			document.add(new Paragraph("Ausgaben: " + Helpers.getCurrencyString(budget.getPaymentSum(), currency), fontRed));
			document.add(new Paragraph("Restbudget: " + Helpers.getCurrencyString(budget.getIncomeSum()-budget.getPaymentSum(), currency), fontBlack));			
			document.add(Chunk.NEWLINE);
		}
		
		document.add(new Paragraph("Buchungsübersicht", headerFont));
		document.add(Chunk.NEWLINE);

		if(splitTable)
		{
			document.add(new Paragraph("Einnahmen", smallHeaderFont));
			document.add(Chunk.NEWLINE);
			PdfPTable table = generateTable(100, AmountType.INCOME);
			if(table != null)
			{
				document.add(table);
			}

			document.add(Chunk.NEWLINE);
			document.add(new Paragraph("Ausgaben", smallHeaderFont));
			document.add(Chunk.NEWLINE);
			table = generateTable(100, AmountType.PAYMENT);
			if(table != null)
			{
				document.add(table);
			}
		}
		else
		{
			PdfPTable table = generateTable(100, AmountType.BOTH);
			if(table != null)
			{
				document.add(table);
			}
		}

		if(includeCategoryBudgets)
		{
			document.add(Chunk.NEWLINE);
			document.add(new Paragraph("Verbrauch nach Kategorien", headerFont));
			document.add(Chunk.NEWLINE);
			PdfPTable table = generateCategoryBudgets();
			if(table != null)
			{
				document.add(table);
			}
		}

		document.close();
	}

	private PdfPTable generateCategoryBudgets()
	{
		PdfPTable table = new PdfPTable(2);
		table.setWidthPercentage(100);
		Font font = new Font(FontFamily.HELVETICA, 8, Font.NORMAL, GrayColor.BLACK);
		
		//header cells
		PdfPCell cellHeaderCategory = new PdfPCell(new Phrase("Kategorie", font));
		cellHeaderCategory.setBackgroundColor(GrayColor.LIGHT_GRAY);
		cellHeaderCategory.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cellHeaderCategory);
		PdfPCell cellHeaderAmount = new PdfPCell(new Phrase("Betrag", font));
		cellHeaderAmount.setBackgroundColor(GrayColor.LIGHT_GRAY);
		cellHeaderAmount.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(cellHeaderAmount);		

		for(CategoryBudget budget : categoryBudgets)
		{			
			String name = budget.getName();
			if(name.equals("NONE"))
			{
				name = "Keine Kategorie";
			}			
			PdfPCell cellName = new PdfPCell(new Phrase(name, font));
			cellName.setBackgroundColor(new BaseColor(Color.WHITE));
			cellName.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cellName);
			
			PdfPCell cellAmount = new PdfPCell(new Phrase(Helpers.getCurrencyString(budget.getBudget() / 100.0, currency), font));
			cellAmount.setBackgroundColor(new BaseColor(Color.WHITE));
			cellAmount.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(cellAmount);
		}
		
		return table;
	}

	private String getProperty(ReportItem reportItem, ColumnType columnType)
	{
		switch(columnType)
		{
			case AMOUNT:
				return Helpers.getCurrencyString(reportItem.getAmount(), currency);
			case CATEGORY:
				String name = reportItem.getCategory().getName();
				if(name.equals("NONE"))
				{
					name = "Keine Kategorie";
				}			
				return name;
			case DATE:			    
				return DateTime.parse(reportItem.getDate(), DateTimeFormat.forPattern("YYYY-MM-dd")).toString("dd.MM.YYYY");
			case DESCRIPTION:
				return reportItem.getDescription();
			case NAME:
				return reportItem.getName();
			case POSITION:
				return String.valueOf(reportItem.getPosition());
			case RATING:
				return reportItem.getAmount() > 0 ? "+" : "-";
			case REPEATING:	
				if(reportItem.getRepeating())
				{
					return "Ja";
				}
				else
				{
					return "Nein";
				}				
			default:
				return null;
		}
	}
}