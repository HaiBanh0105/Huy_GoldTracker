package com.example.goldtracker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;
import com.github.mikephil.charting.data.Entry;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PdfGenerator {

    public static void exportGoldReport(Context context, String goldType, String days, List<Entry> entries, List<String> labels) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // 1. Tiêu đề
        paint.setColor(Color.RED);
        paint.setTextSize(24);
        paint.setFakeBoldText(true);
        canvas.drawText("BÁO CÁO BIẾN ĐỘNG GIÁ VÀNG", 110, 60, paint);

        // 2. Thông tin chung (Tọa độ Y từ 100 - 150)
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        canvas.drawText("Loại vàng: " + goldType, 50, 100, paint);
        canvas.drawText("Khoảng thời gian: " + days + " ngày gần nhất", 50, 125, paint);
        canvas.drawText("Đơn vị: Triệu VND / Lượng", 50, 150, paint);

        // --- LOGIC TÍNH TOÁN ---
        float maxPrice = -1;
        float minPrice = Float.MAX_VALUE;
        float sumPrice = 0;
        for (Entry entry : entries) {
            float val = entry.getY();
            if (val > maxPrice) maxPrice = val;
            if (val < minPrice) minPrice = val;
            sumPrice += val;
        }
        float avgPrice = sumPrice / entries.size();
        float netChange = entries.get(entries.size() - 1).getY() - entries.get(0).getY();

        // 3. Phần Thống kê nhanh (Tọa độ Y từ 200 - 250)
        paint.setFakeBoldText(true);
        paint.setTextSize(15);
        canvas.drawText("THỐNG KÊ BIẾN ĐỘNG:", 50, 200, paint);

        paint.setFakeBoldText(false);
        paint.setTextSize(13);
        canvas.drawText("- Cao nhất: " + String.format("%.3f", maxPrice), 70, 225, paint);
        canvas.drawText("- Thấp nhất: " + String.format("%.3f", minPrice), 70, 250, paint);
        canvas.drawText("- Trung bình: " + String.format("%.3f", avgPrice), 300, 225, paint);

        // Đổi màu cho biến động Tăng/Giảm (Điểm nhấn sáng tạo)
        if (netChange >= 0) paint.setColor(Color.parseColor("#008000")); // Xanh lá nếu tăng
        else paint.setColor(Color.RED); // Đỏ nếu giảm

        String changeText = (netChange >= 0 ? "Tăng: +" : "Giảm: ") + String.format("%.3f", netChange);
        canvas.drawText("- Tổng biến động: " + changeText, 300, 250, paint);

        // 4. Bảng dữ liệu chi tiết (Bắt đầu từ Y = 320 để không bị đè)
        paint.setColor(Color.BLACK); // Trả lại màu đen
        int tableStartY = 320;
        paint.setStrokeWidth(1);

        // Đường kẻ ngang đầu bảng
        canvas.drawLine(50, tableStartY - 20, 500, tableStartY - 20, paint);

        paint.setFakeBoldText(true);
        canvas.drawText("Ngày (DD/MM)", 70, tableStartY, paint);
        canvas.drawText("Giá Bán (Triệu)", 350, tableStartY, paint);

        // Đường kẻ ngang dưới tiêu đề cột
        canvas.drawLine(50, tableStartY + 10, 500, tableStartY + 10, paint);

        paint.setFakeBoldText(false);
        int currentY = tableStartY + 40; // Dòng dữ liệu đầu tiên

        for (int i = 0; i < entries.size(); i++) {
            if (currentY > 800) break; // Chống tràn trang A4

            canvas.drawText(labels.get(i), 70, currentY, paint);
            canvas.drawText(String.format("%.3f", entries.get(i).getY()), 350, currentY, paint);

            currentY += 30; // Khoảng cách giữa các dòng
        }

        // Kết thúc trang và lưu file
        document.finishPage(page);

        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String fileName = "BaoCaoVang_" + timeStamp + ".pdf";
        java.io.File file = new java.io.File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), fileName);

        try {
            document.writeTo(new java.io.FileOutputStream(file));
            android.widget.Toast.makeText(context, "Đã xuất PDF vào thư mục Downloads", android.widget.Toast.LENGTH_LONG).show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        document.close();
    }
}