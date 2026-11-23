package com.example.orderapp

import androidx.compose.runtime.Immutable

const val ITEM_PRICE = 500
const val MIN_ORDER_SUM = 1000
const val MAX_ITEMS = 10
const val RETURN_WINDOW_DAYS = 3

enum class OrderStatus(val title: String) {
    Created("Оформляется"),
    Paid("Оплачен"),
    Shipped("В пути"),
    Delivered("Доставлен"),
    Cancelled("Отменён"),
    Returned("Возвращён")
}

@Immutable
data class Order(
    val id: String = "1042",
    val title: String = "Заказ №1042",
    val status: OrderStatus = OrderStatus.Created,
    val items: Int = 1,
    val hasAddress: Boolean = false,
    val paidAmount: Int = 0,
    val returnDaysLeft: Int = 0
) {
    val total: Int get() = items * ITEM_PRICE

    val isSpecial: Boolean
        get() = status == OrderStatus.Cancelled ||
                status == OrderStatus.Returned ||
                status == OrderStatus.Shipped ||
                (status == OrderStatus.Delivered && returnDaysLeft <= 1)

    val specialLabel: String?
        get() = when {
            status == OrderStatus.Cancelled -> "Отменён"
            status == OrderStatus.Returned -> "Возврат"
            status == OrderStatus.Shipped -> "В пути"
            status == OrderStatus.Delivered && returnDaysLeft == 1 -> "Последний день возврата"
            status == OrderStatus.Delivered && returnDaysLeft == 0 -> "Возврат закрыт"
            else -> null
        }

    private val isEditable get() = status == OrderStatus.Created || status == OrderStatus.Paid

    val canAddItem get() = status == OrderStatus.Created && items < MAX_ITEMS
    val canRemoveItem get() = status == OrderStatus.Created && items > 1
    val canToggleAddress get() = isEditable
    val canPay get() = status == OrderStatus.Created && hasAddress && total >= MIN_ORDER_SUM
    val canShip get() = status == OrderStatus.Paid && hasAddress
    val canConfirmDelivery get() = status == OrderStatus.Shipped
    val canCancel get() = status == OrderStatus.Created || status == OrderStatus.Paid
    val canReturn get() = status == OrderStatus.Delivered && returnDaysLeft > 0
    val canSkipDay get() = status == OrderStatus.Delivered && returnDaysLeft > 0

    fun addItem() = if (canAddItem) copy(items = items + 1) else this
    fun removeItem() = if (canRemoveItem) copy(items = items - 1) else this
    fun toggleAddress() = if (canToggleAddress) copy(hasAddress = !hasAddress) else this
    fun pay() = if (canPay) copy(status = OrderStatus.Paid, paidAmount = total) else this
    fun ship() = if (canShip) copy(status = OrderStatus.Shipped) else this
    fun confirmDelivery() =
        if (canConfirmDelivery) copy(status = OrderStatus.Delivered, returnDaysLeft = RETURN_WINDOW_DAYS) else this
    fun cancel() = if (canCancel) copy(status = OrderStatus.Cancelled, paidAmount = 0) else this
    fun returnOrder() = if (canReturn) copy(status = OrderStatus.Returned, paidAmount = 0, returnDaysLeft = 0) else this
    fun skipDay() = if (canSkipDay) copy(returnDaysLeft = returnDaysLeft - 1) else this
    fun resetToInitial() = Order(id = id, title = title)

    val payHint: String?
        get() = when {
            status != OrderStatus.Created -> null
            !hasAddress -> "Для оплаты укажите адрес доставки"
            total < MIN_ORDER_SUM -> "Минимальная сумма заказа — $MIN_ORDER_SUM ₽"
            else -> null
        }
}

object InitialOrders {
    fun seed(): List<Order> = listOf(
        Order("1042", "Заказ №1042", OrderStatus.Created, items = 1, hasAddress = false),
        Order("1043", "Заказ №1043", OrderStatus.Paid, items = 3, hasAddress = true, paidAmount = 1500),
        Order("1044", "Заказ №1044", OrderStatus.Shipped, items = 2, hasAddress = true, paidAmount = 1000),
        Order("1045", "Заказ №1045", OrderStatus.Delivered, items = 4, hasAddress = true, paidAmount = 2000, returnDaysLeft = 1),
        Order("1046", "Заказ №1046", OrderStatus.Cancelled, items = 2, hasAddress = true, paidAmount = 0),
        Order("1047", "Заказ №1047", OrderStatus.Returned, items = 2, hasAddress = true, paidAmount = 0),
        Order("1048", "Заказ №1048", OrderStatus.Created, items = 2, hasAddress = true),
        Order("1049", "Заказ №1049", OrderStatus.Delivered, items = 3, hasAddress = true, paidAmount = 1500, returnDaysLeft = 3),
        Order("1050", "Заказ №1050", OrderStatus.Paid, items = 2, hasAddress = false, paidAmount = 1000),
        Order("1051", "Заказ №1051", OrderStatus.Created, items = 4, hasAddress = true),
        Order("1052", "Заказ №1052", OrderStatus.Delivered, items = 2, hasAddress = true, paidAmount = 1000, returnDaysLeft = 0),
        Order("1053", "Заказ №1053", OrderStatus.Created, items = 1, hasAddress = false)
    )
}