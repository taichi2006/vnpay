package murach.vnpay;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet; // Thêm import này
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

// Thêm dòng @WebServlet này để Tomcat nhận diện đường dẫn
@WebServlet("/vnpay_return")
public class VnpayReturnServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = URLEncoder.encode(params.nextElement(), StandardCharsets.US_ASCII);
            String fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName).append('=').append(fieldValue);
                if (itr.hasNext()) hashData.append('&');
            }
        }

        String signValue = Config.hmacSHA512(Config.vnp_HashSecret, hashData.toString());
        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(request.getParameter("vnp_TransactionStatus"))) {
                request.setAttribute("message", "Thanh toán thành công!");

                // Lấy giỏ hàng từ Session
                HttpSession session = request.getSession();
                murach.business.Cart cart = (murach.business.Cart) session.getAttribute("cart");

                if (cart != null) {
                    // Chuyển giỏ hàng sang request để mang sang trang JSP hiển thị hóa đơn
                    request.setAttribute("invoiceCart", cart);
                    // Xóa giỏ hàng cũ khỏi session vì khách đã mua xong
                    session.removeAttribute("cart");
                }

            } else {
                request.setAttribute("message", "Thanh toán thất bại hoặc bị hủy.");
            }
        } else {
            request.setAttribute("message", "Chữ ký không hợp lệ!");
        }

        request.getRequestDispatcher("/vnpay_return.jsp").forward(request, response);
    }
}
