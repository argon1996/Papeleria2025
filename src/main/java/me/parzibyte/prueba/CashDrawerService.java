package me.parzibyte.prueba;

import me.parzibyte.sistemaventasspringboot.entity.ProductoVendido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class CashDrawerService {

    private static final String PRINTER_NAME = "ImpresoraTermica"; // Cambia al nombre de tu impresora térmica
    private static final Logger logger = LoggerFactory.getLogger(CashDrawerService.class);

    public void openDrawer() throws Exception {
        PrintService printService = findPrintService(PRINTER_NAME);
        if (printService == null) {
            throw new Exception("No se encontró la impresora: " + PRINTER_NAME);
        }

        byte[] openDrawerCommand = {(byte) 0x1B, (byte) 0x70, (byte) 0x00, (byte) 0x19, (byte) 0xFA};
        ByteArrayInputStream bais = new ByteArrayInputStream(openDrawerCommand);
        SimpleDoc doc = new SimpleDoc(bais, javax.print.DocFlavor.INPUT_STREAM.AUTOSENSE, null);

        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        javax.print.DocPrintJob job = printService.createPrintJob();

        job.addPrintJobListener(new PrintJobAdapter() {
            @Override
            public void printJobCompleted(PrintJobEvent pje) {
                System.out.println("Print job completed.");
            }

            @Override
            public void printJobFailed(PrintJobEvent pje) {
                System.out.println("Print job failed.");
            }
        });

        job.print(doc, attributes);
    }

    public void printTest() throws Exception {
        PrintService printService = findPrintService(PRINTER_NAME);
        if (printService == null) {
            throw new Exception("No se encontró la impresora: " + PRINTER_NAME);
        }

        String testReceipt = "=== Test Receipt ===\n"
                + "Item 1    1.00\n"
                + "Item 2    2.00\n"
                + "Total    3.00\n";

        ByteArrayInputStream bais = new ByteArrayInputStream(testReceipt.getBytes(StandardCharsets.UTF_8));
        SimpleDoc doc = new SimpleDoc(bais, javax.print.DocFlavor.INPUT_STREAM.AUTOSENSE, null);

        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        javax.print.DocPrintJob job = printService.createPrintJob();

        job.addPrintJobListener(new PrintJobAdapter() {
            @Override
            public void printJobCompleted(PrintJobEvent pje) {
                System.out.println("Print job completed.");
            }

            @Override
            public void printJobFailed(PrintJobEvent pje) {
                System.out.println("Print job failed.");
            }
        });

        job.print(doc, attributes);
    }

    public void printFormattedInvoice(List<ProductoVendido> productosVendidos, double totalVenta) throws Exception {
        PrintService printService = findPrintService(PRINTER_NAME);
        if (printService == null) {
            throw new Exception("No se encontró la impresora: " + PRINTER_NAME);
        }

        StringBuilder sb = new StringBuilder();

        // Obtener fecha y hora actuales
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String fechaHoraActual = sdf.format(new Date());

        // Comandos ESC/POS para formatear la factura
        sb.append((char) 0x1B).append((char) 0x40); // Reset printer
        sb.append((char) 0x1B).append((char) 0x61).append((char) 0x01); // Center align
        sb.append("Interestelar\n\n");

        sb.append((char) 0x1B).append((char) 0x61).append((char) 0x00); // Left align
        sb.append("Fecha: ").append(fechaHoraActual.split(" ")[0]).append("\n");
        sb.append("Hora: ").append(fechaHoraActual.split(" ")[1]).append("\n");
        sb.append("Teléfono: 3142649585\n");
        sb.append("------------------------------\n");
        sb.append("Producto       Cant  Precio\n");
        sb.append("------------------------------\n");

        for (ProductoVendido productoVendido : productosVendidos) {
            sb.append(String.format("%-13s %3.0f %8.2f\n", productoVendido.getNombre(), productoVendido.getCantidad(), productoVendido.getPrecio()));
        }

        sb.append("------------------------------\n");
        sb.append(String.format("Total:              %8.2f\n", totalVenta));
        sb.append("------------------------------\n\n"); // Agrega un espacio extra antes del mensaje

        sb.append((char) 0x1B).append((char) 0x61).append((char) 0x01); // Center align
        sb.append("\n\n¡Gracias por su compra!\n\n"); // Agrega un espacio extra antes y después del mensaje

        // Feed and cut paper
        sb.append((char) 0x1D).append((char) 0x56).append((char) 0x42).append((char) 0x00);

        ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        SimpleDoc doc = new SimpleDoc(bais, javax.print.DocFlavor.INPUT_STREAM.AUTOSENSE, null);

        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        javax.print.DocPrintJob job = printService.createPrintJob();

        job.addPrintJobListener(new PrintJobAdapter() {
            @Override
            public void printJobCompleted(PrintJobEvent pje) {
                System.out.println("Print job completed.");
            }

            @Override
            public void printJobFailed(PrintJobEvent pje) {
                System.out.println("Print job failed.");
            }
        });

        job.print(doc, attributes);
    }

    private PrintService findPrintService(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : printServices) {
            if (printService.getName().equalsIgnoreCase(printerName)) {
                return printService;
            }
        }
        return null;
    }
}
