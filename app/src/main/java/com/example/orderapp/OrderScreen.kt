package com.example.orderapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ---------- Модель ----------

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

data class Order(
    val status: OrderStatus = OrderStatus.Created,
    val items: Int = 1,
    val hasAddress: Boolean = false,
    val paidAmount: Int = 0,
    val returnDaysLeft: Int = 0
) {
    val total: Int get() = items * ITEM_PRICE

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

    val payHint: String?
        get() = when {
            status != OrderStatus.Created -> null
            !hasAddress -> "Для оплаты укажите адрес доставки"
            total < MIN_ORDER_SUM -> "Минимальная сумма заказа — $MIN_ORDER_SUM ₽"
            else -> null
        }
}

val OrderSaver = mapSaver(
    save = {
        mapOf(
            "status" to it.status.name,
            "items" to it.items,
            "address" to it.hasAddress,
            "paid" to it.paidAmount,
            "days" to it.returnDaysLeft
        )
    },
    restore = {
        Order(
            status = OrderStatus.valueOf(it["status"] as String),
            items = it["items"] as Int,
            hasAddress = it["address"] as Boolean,
            paidAmount = it["paid"] as Int,
            returnDaysLeft = it["days"] as Int
        )
    }
)

// ---------- Экран ----------

@Composable
fun OrderScreen(modifier: Modifier = Modifier) {
    var order by rememberSaveable(stateSaver = OrderSaver) { mutableStateOf(Order()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Заказ №1042", style = MaterialTheme.typography.headlineMedium)

        StatusCard(order)
        WarningBanner(order)

        Card {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Состав и адрес", style = MaterialTheme.typography.titleMedium)
                if (order.canToggleAddress || order.canAddItem || order.canRemoveItem) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { order = order.addItem() }, enabled = order.canAddItem) {
                            Text("+ Товар")
                        }
                        OutlinedButton(onClick = { order = order.removeItem() }, enabled = order.canRemoveItem) {
                            Text("− Товар")
                        }
                    }
                    OutlinedButton(
                        onClick = { order = order.toggleAddress() },
                        enabled = order.canToggleAddress
                    ) {
                        Text(if (order.hasAddress) "Стереть адрес" else "Указать адрес")
                    }
                } else {
                    Text(
                        "🔒 Изменение состава и адреса недоступно",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Text("Действия", style = MaterialTheme.typography.titleMedium)

        Button(onClick = { order = order.pay() }, enabled = order.canPay, modifier = Modifier.fillMaxWidth()) {
            Text("Оплатить ${order.total} ₽")
        }
        order.payHint?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = { order = order.ship() }, enabled = order.canShip, modifier = Modifier.fillMaxWidth()) {
            Text("Отправить")
        }
        if (order.status == OrderStatus.Paid && !order.hasAddress) {
            Text(
                "Адрес стёрт — отправка невозможна, укажите адрес",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Button(
            onClick = { order = order.confirmDelivery() },
            enabled = order.canConfirmDelivery,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Подтвердить получение") }
        Button(
            onClick = { order = order.skipDay() },
            enabled = order.canSkipDay,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Прошёл день") }
        Button(
            onClick = { order = order.returnOrder() },
            enabled = order.canReturn,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Вернуть заказ") }
        Button(
            onClick = { order = order.cancel() },
            enabled = order.canCancel,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Отменить заказ") }

        OutlinedButton(onClick = { order = Order() }, modifier = Modifier.fillMaxWidth()) {
            Text("Сбросить")
        }
    }
}

@Composable
private fun StatusCard(order: Order) {
    val colors = MaterialTheme.colorScheme
    val container = when (order.status) {
        OrderStatus.Cancelled -> colors.errorContainer
        OrderStatus.Returned -> colors.tertiaryContainer
        OrderStatus.Delivered -> colors.primaryContainer
        OrderStatus.Shipped -> colors.secondaryContainer
        else -> colors.surfaceVariant
    }
    Card(colors = CardDefaults.cardColors(containerColor = container), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Статус: ${order.status.title}", style = MaterialTheme.typography.titleLarge)
            Text("Товаров: ${order.items} × $ITEM_PRICE ₽ = ${order.total} ₽")
            Text("Адрес: ${if (order.hasAddress) "указан" else "не указан"}")
            Text("Оплачено: ${order.paidAmount} ₽")
            if (order.status == OrderStatus.Delivered) {
                Text("Дней на возврат: ${order.returnDaysLeft}")
            }
        }
    }
}

@Composable
private fun WarningBanner(order: Order) {
    val text = when {
        order.status == OrderStatus.Cancelled -> "Заказ отменён. Дальнейшие действия невозможны."
        order.status == OrderStatus.Returned -> "Заказ возвращён, деньги возвращены."
        order.status == OrderStatus.Delivered && order.returnDaysLeft == 0 ->
            "Срок возврата истёк, вернуть заказ нельзя."
        order.status == OrderStatus.Delivered && order.returnDaysLeft == 1 ->
            "Остался последний день на возврат!"
        order.status == OrderStatus.Shipped -> "Заказ в пути — отменить его уже нельзя."
        else -> null
    } ?: return
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "⚠ $text",
            modifier = Modifier.padding(12.dp),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}