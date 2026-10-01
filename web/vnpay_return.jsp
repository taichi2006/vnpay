<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Kết quả thanh toán</title>
    <link rel="stylesheet" href="styles/main.css" type="text/css"/>
    <style>
        .result-container {
            border: 1px solid #ddd;
            padding: 20px;
            width: 500px;
            margin-top: 20px;
            border-radius: 8px;
            background-color: #f9f9f9;
        }
        .success-msg { color: #008000; font-size: 18px; }
        .error-msg { color: #ff0000; font-size: 18px; }
        .invoice-table { width: 100%; margin-top: 20px; border-collapse: collapse; }
        .invoice-table th, .invoice-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        .invoice-table th { background-color: #f2f2f2; }
    </style>
</head>
<body>

    <h1>Trạng thái giao dịch</h1>

    <div class="result-container">
        <p class="${message == 'Thanh toán thành công!' ? 'success-msg' : 'error-msg'}">
            <strong>${message}</strong>
        </p>

        <p><strong>Mã đơn hàng:</strong> ${param.vnp_TxnRef}</p>
        <p>
            <strong>
                ${param.vnp_TransactionStatus == '00' ? 'Tổng tiền đã thanh toán:' : 'Tổng tiền đơn hàng:'}
            </strong>
            ${param.vnp_Amount / 100} VND
        </p>
        <p><strong>Ngân hàng:</strong> ${param.vnp_BankCode}</p>

        <!-- Nếu thanh toán thành công và có dữ liệu giỏ hàng, in ra danh sách sản phẩm -->
        <c:if test="${not empty invoiceCart}">
            <h3>Chi tiết sản phẩm đã mua</h3>
            <table class="invoice-table">
              <tr>
                <th>Sản phẩm</th>
                <th>Số lượng</th>
                <th>Đơn giá</th>
                <th>Thành tiền</th>
              </tr>
              <c:forEach var="item" items="${invoiceCart.items}">
              <tr>
                <td>${item.product.description}</td>
                <td>${item.quantity}</td>
                <td>${item.product.priceCurrencyFormat}</td>
                <td>${item.totalCurrencyFormat}</td>
              </tr>
              </c:forEach>
            </table>
        </c:if>

        <br><br>
        <form action="cart" method="post">
            <input type="hidden" name="action" value="shop">
            <input type="submit" value="Tiếp tục mua sắm">
        </form>
    </div>

</body>
</html>