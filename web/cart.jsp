<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>Murach's Java Servlets and JSP</title>
    <link rel="stylesheet" href="styles/main.css?v=2" type="text/css"/>
</head>
<body>

<h1>Your cart</h1>

<table>
  <tr>
    <th>Quantity</th>
    <th>Description</th>
    <th>Price</th>
    <th>Amount</th>
    <th></th>
  </tr>

<c:forEach var="item" items="${cart.items}">
  <tr>
    <td>
      <form action="cart" method="post">
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="productCode" value="${item.product.code}">
        <input type="text" name="quantity" value="${item.quantity}" id="quantity">
        <input type="submit" value="Update">
      </form>
    </td>
    <td>${item.product.description}</td>
    <td>${item.product.priceCurrencyFormat}</td>
    <td>${item.totalCurrencyFormat}</td>
    <td>
      <form action="cart" method="post">
        <input type="hidden" name="productCode" value="${item.product.code}">
        <input type="hidden" name="quantity" value="0">
        <input type="submit" value="Remove Item">
      </form>
    </td>
  </tr>
</c:forEach>
</table>

<p><b>To change the quantity</b>, enter the new quantity
      and click on the Update button.</p>

<!-- Nút tiếp tục mua sắm đã được sửa action -->
<form action="cart" method="post">
  <input type="hidden" name="action" value="shop">
  <input type="submit" value="Continue Shopping">
</form>

<br>

<!-- Form thanh toán VNPay hiện đại -->
<form action="cart" method="post" class="vnpay-form">
    <input type="hidden" name="action" value="checkout">
    <h3>Chọn phương thức thanh toán:</h3>

    <div class="payment-option">
        <input type="radio" id="default" name="bankCode" value="" checked>
        <label for="default">Cổng VNPay mặc định</label>
    </div>
    <div class="payment-option">
        <input type="radio" id="atm" name="bankCode" value="VNBANK">
        <label for="atm">Thẻ ATM nội địa</label>
    </div>
    <div class="payment-option">
        <input type="radio" id="visa" name="bankCode" value="INTCARD">
        <label for="visa">Thẻ quốc tế (Visa/Master)</label>
    </div>
    <div class="payment-option">
        <input type="radio" id="ncb" name="bankCode" value="NCB">
        <label for="ncb">Ngân hàng NCB (Để Test)</label>
    </div>

    <input type="submit" value="Thanh toán ngay" class="btn-checkout">
</form>

</body>
</html>