package com.example.orderapp

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Immutable
data class OrderListUiState(
    val orders: List<Order> = InitialOrders.seed(),
    val nextNumber: Int = 1054
)

sealed interface OrderAction {
    data object AddOrder : OrderAction
    data class DeleteOrder(val orderId: String) : OrderAction
    data class AddItem(val orderId: String) : OrderAction
    data class RemoveItem(val orderId: String) : OrderAction
    data class ToggleAddress(val orderId: String) : OrderAction
    data class Pay(val orderId: String) : OrderAction
    data class Ship(val orderId: String) : OrderAction
    data class ConfirmDelivery(val orderId: String) : OrderAction
    data class SkipDay(val orderId: String) : OrderAction
    data class ReturnOrder(val orderId: String) : OrderAction
    data class Cancel(val orderId: String) : OrderAction
    data class Reset(val orderId: String) : OrderAction
}

class OrderViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OrderListUiState())
    val uiState: StateFlow<OrderListUiState> = _uiState.asStateFlow()

    fun onAction(action: OrderAction) {
        when (action) {
            OrderAction.AddOrder -> addOrder()
            is OrderAction.DeleteOrder -> deleteOrder(action.orderId)
            is OrderAction.AddItem -> updateOrder(action.orderId) { it.addItem() }
            is OrderAction.RemoveItem -> updateOrder(action.orderId) { it.removeItem() }
            is OrderAction.ToggleAddress -> updateOrder(action.orderId) { it.toggleAddress() }
            is OrderAction.Pay -> updateOrder(action.orderId) { it.pay() }
            is OrderAction.Ship -> updateOrder(action.orderId) { it.ship() }
            is OrderAction.ConfirmDelivery -> updateOrder(action.orderId) { it.confirmDelivery() }
            is OrderAction.SkipDay -> updateOrder(action.orderId) { it.skipDay() }
            is OrderAction.ReturnOrder -> updateOrder(action.orderId) { it.returnOrder() }
            is OrderAction.Cancel -> updateOrder(action.orderId) { it.cancel() }
            is OrderAction.Reset -> updateOrder(action.orderId) { it.resetToInitial() }
        }
    }

    private fun addOrder() {
        _uiState.update { state ->
            val num = state.nextNumber
            val fresh = Order(
                id = num.toString(),
                title = "Заказ №$num"
            )
            state.copy(
                orders = listOf(fresh) + state.orders,
                nextNumber = num + 1
            )
        }
    }

    private fun deleteOrder(orderId: String) {
        _uiState.update { state ->
            state.copy(orders = state.orders.filterNot { it.id == orderId })
        }
    }

    private fun updateOrder(orderId: String, transform: (Order) -> Order) {
        _uiState.update { state ->
            state.copy(
                orders = state.orders.map { item ->
                    if (item.id == orderId) transform(item) else item
                }
            )
        }
    }
}