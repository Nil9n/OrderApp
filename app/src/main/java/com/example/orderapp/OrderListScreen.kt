package com.example.orderapp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    orders: List<Order>,
    onAction: (OrderAction) -> Unit,
    onOpenOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Заказы (${orders.size})") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAction(OrderAction.AddOrder) }) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(orders, key = { it.id }) { order ->
                val bgColor = when (order.status) {
                    OrderStatus.Cancelled -> MaterialTheme.colorScheme.errorContainer
                    OrderStatus.Returned -> MaterialTheme.colorScheme.tertiaryContainer
                    OrderStatus.Shipped -> MaterialTheme.colorScheme.secondaryContainer
                    OrderStatus.Delivered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val border = when {
                    order.status == OrderStatus.Cancelled -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
                    order.isSpecial -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    else -> null
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenOrder(order.id) },
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    border = border
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = order.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = buildString {
                                    append("Статус: ${order.status.title}")
                                    order.specialLabel?.let { append(" [$it]") }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (order.isSpecial) FontWeight.SemiBold else FontWeight.Normal
                            )
                            Text(
                                text = "Товаров: ${order.items} · Сумма: ${order.total} ₽ · " +
                                        "Адрес: ${if (order.hasAddress) "да" else "нет"} · Оплачено: ${order.paidAmount} ₽" +
                                        if (order.status == OrderStatus.Delivered) " · Дней возврата: ${order.returnDaysLeft}" else "",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(onClick = { onAction(OrderAction.DeleteOrder(order.id)) }) {
                            Text("Удалить")
                        }
                    }
                }
            }
        }
    }
}