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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    order: Order?,
    onAction: (OrderAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(order?.title ?: "Заказ не найден") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (order == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Заказ удалён.")
                Button(onClick = onBack) { Text("К списку заказов") }
            }
            return@Scaffold
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(order)
            WarningBanner(order)

            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Состав и адрес", style = MaterialTheme.typography.titleMedium)
                    if (order.canToggleAddress || order.canAddItem || order.canRemoveItem) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onAction(OrderAction.AddItem(order.id)) },
                                enabled = order.canAddItem
                            ) {
                                Text("+ Товар")
                            }
                            OutlinedButton(
                                onClick = { onAction(OrderAction.RemoveItem(order.id)) },
                                enabled = order.canRemoveItem
                            ) {
                                Text("− Товар")
                            }
                        }
                        OutlinedButton(
                            onClick = { onAction(OrderAction.ToggleAddress(order.id)) },
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

            Button(
                onClick = { onAction(OrderAction.Pay(order.id)) },
                enabled = order.canPay,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Оплатить ${order.total} ₽")
            }
            order.payHint?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(
                onClick = { onAction(OrderAction.Ship(order.id)) },
                enabled = order.canShip,
                modifier = Modifier.fillMaxWidth()
            ) {
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
                onClick = { onAction(OrderAction.ConfirmDelivery(order.id)) },
                enabled = order.canConfirmDelivery,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Подтвердить получение") }
            Button(
                onClick = { onAction(OrderAction.SkipDay(order.id)) },
                enabled = order.canSkipDay,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Прошёл день") }
            Button(
                onClick = { onAction(OrderAction.ReturnOrder(order.id)) },
                enabled = order.canReturn,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Вернуть заказ") }
            Button(
                onClick = { onAction(OrderAction.Cancel(order.id)) },
                enabled = order.canCancel,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Отменить заказ") }

            OutlinedButton(
                onClick = { onAction(OrderAction.Reset(order.id)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Сбросить")
            }
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