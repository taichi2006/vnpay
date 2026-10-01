package murach.cart;

import jakarta.servlet.http.HttpSession;
import murach.data.ProductIO;
import murach.business.LineItem;
import murach.business.Cart;
import murach.business.Product;
import java.io.*;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

// Các thư viện thêm vào cho VNPay
import murach.vnpay.Config;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class CartServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String url = "/index.jsp";
        ServletContext sc = getServletContext();

        String action = request.getParameter("action");

        if (action == null) {
            action = "cart";
        }

        if (action.equals("shop")) {
            url = "/index.jsp";
        }
        else if (action.equals("cart")) {

            String productCode = request.getParameter("productCode");
            String quantityString = request.getParameter("quantity");

            HttpSession session = request.getSession();
            Cart cart = (Cart) session.getAttribute("cart");

            if (cart == null) {
                cart = new Cart();
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityString);
                if (quantity < 0) {
                    quantity = 1;
                }
            } catch (NumberFormatException nfe) {
                quantity = 1;
            }

            String path = sc.getRealPath("/WEB-INF/products.txt");
            Product product = ProductIO.getProduct(productCode, path);

            LineItem lineItem = new LineItem();
            lineItem.setProduct(product);
            lineItem.setQuantity(quantity);

            if (quantity > 0) {
                cart.addItem(lineItem);
            } else {
                cart.removeItem(lineItem);
            }

            session.setAttribute("cart", cart);
            url = "/cart.jsp";
        }
        else if (action.equals("update")) {

            String productCode = request.getParameter("productCode");
            String quantityString = request.getParameter("quantity");

            HttpSession session = request.getSession();
            Cart cart = (Cart) session.getAttribute("cart");

            if (cart == null) {
                cart = new Cart();
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityString);
                if (quantity < 0) {
                    quantity = 1;
                }
            } catch (NumberFormatException nfe) {
                quantity = 1;
            }

            String path = sc.getRealPath("/WEB-INF/products.txt");
            Product product = ProductIO.getProduct(productCode, path);

            LineItem lineItem = new LineItem();
            lineItem.setProduct(product);
            lineItem.setQuantity(quantity);

            if (quantity > 0) {
                cart.updateItem(lineItem);
            } else {
                cart.removeItem(lineItem);
            }

            session.setAttribute("cart", cart);
            url = "/cart.jsp";
        }
        // FULL CODE CHECKOUT TÍCH HỢP VNPAY (CÓ ĐỔI TIỀN & CHỌN BANKCODE)
        else if (action.equals("checkout")) {
            HttpSession session = request.getSession();
            Cart cart = (Cart) session.getAttribute("cart");

            if (cart != null && cart.getItems().size() > 0) {
                // 1. Lấy phương thức thanh toán người dùng chọn từ form
                String bankCode = request.getParameter("bankCode");

                // 2. Tính tổng tiền giỏ hàng (Đang là USD)
                double totalUSD = 0;
                for (LineItem item : cart.getItems()) {
                    totalUSD += item.getTotal();
                }

                // 3. Quy đổi tỷ giá (Ví dụ: 1 USD = 25,400 VND)
                double exchangeRate = 25400.0;
                double totalVND = totalUSD * exchangeRate;

                // VNPay yêu cầu số tiền nguyên tệ nhân với 100
                long amount = (long) (totalVND * 100);

                // 4. Chuẩn bị tham số gửi sang VNPay
                String vnp_Version = "2.1.0";
                String vnp_Command = "pay";
                String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
                String vnp_IpAddr = request.getRemoteAddr();

                Map<String, String> vnp_Params = new HashMap<>();
                vnp_Params.put("vnp_Version", vnp_Version);
                vnp_Params.put("vnp_Command", vnp_Command);
                vnp_Params.put("vnp_TmnCode", Config.vnp_TmnCode);
                vnp_Params.put("vnp_Amount", String.valueOf(amount));
                vnp_Params.put("vnp_CurrCode", "VND");

                // Nếu người dùng có chọn loại thẻ/QR cụ thể thì mới truyền vào
                if (bankCode != null && !bankCode.isEmpty()) {
                    vnp_Params.put("vnp_BankCode", bankCode);
                }

                vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
                vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + vnp_TxnRef);
                vnp_Params.put("vnp_OrderType", "other");
                vnp_Params.put("vnp_Locale", "vn");
                vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl);
                vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

                Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                String vnp_CreateDate = formatter.format(cld.getTime());
                vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

                // 5. Sắp xếp và tạo mã băm (Checksum)
                List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
                Collections.sort(fieldNames);
                StringBuilder hashData = new StringBuilder();
                StringBuilder query = new StringBuilder();

                Iterator<String> itr = fieldNames.iterator();
                while (itr.hasNext()) {
                    String fieldName = itr.next();
                    String fieldValue = vnp_Params.get(fieldName);
                    if ((fieldValue != null) && (fieldValue.length() > 0)) {
                        hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                        query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                        if (itr.hasNext()) {
                            query.append('&');
                            hashData.append('&');
                        }
                    }
                }

                String vnp_SecureHash = Config.hmacSHA512(Config.vnp_HashSecret, hashData.toString());
                query.append("&vnp_SecureHash=").append(vnp_SecureHash);
                String paymentUrl = Config.vnp_PayUrl + "?" + query.toString();

                // 6. Chuyển hướng sang VNPay
                response.sendRedirect(paymentUrl);
                return; // Ngăn request forward ở dưới chạy tiếp

            } else {
                url = "/cart.jsp";
            }
        }

        sc.getRequestDispatcher(url)
                .forward(request, response);
    }
}