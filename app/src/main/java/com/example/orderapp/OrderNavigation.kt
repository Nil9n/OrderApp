package com.example.orderapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

@Serializable
data object OrderListKey : NavKey

@Serializable
data class OrderDetailKey(val orderId: String) : NavKey

@Composable
fun OrderNavigation(
    viewModel: OrderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val backStack = rememberNavBackStack(OrderListKey)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        entryProvider = entryProvider {
            entry<OrderListKey> {
                OrderListScreen(
                    orders = state.orders,
                    onAction = viewModel::onAction,
                    onOpenOrder = { orderId ->
                        backStack.add(OrderDetailKey(orderId))
                    }
                )
            }
            entry<OrderDetailKey> { key ->
                val selectedOrder = state.orders.find { it.id == key.orderId }
                OrderScreen(
                    order = selectedOrder,
                    onAction = viewModel::onAction,
                    onBack = {
                        if (backStack.size > 1) {
                            backStack.removeAt(backStack.lastIndex)
                        }
                    }
                )
            }
        }
    )
}