package com.familiafinancial.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.google.firebase.firestore.FirebaseFirestore

class FinanceWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        const val ACTION_INCOME = "com.familiafinancial.ACTION_INCOME"
        const val ACTION_EXPENSE = "com.familiafinancial.ACTION_EXPENSE"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Кнопка Доход
            val incomeIntent = Intent(context, AddTransactionActivity::class.java).apply {
                putExtra("type", "income")
                putExtra("widgetId", widgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val incomePending = PendingIntent.getActivity(
                context, widgetId * 2, incomeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_income, incomePending)

            // Кнопка Расход
            val expenseIntent = Intent(context, AddTransactionActivity::class.java).apply {
                putExtra("type", "expense")
                putExtra("widgetId", widgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val expensePending = PendingIntent.getActivity(
                context, widgetId * 2 + 1, expenseIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_expense, expensePending)

            // Загрузка баланса из Firestore
            loadBalance(context, views, appWidgetManager, widgetId)
        }

        private fun loadBalance(
            context: Context,
            views: RemoteViews,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val db = FirebaseFirestore.getInstance()
            db.collection("transactions")
                .get()
                .addOnSuccessListener { result ->
                    var balance = 0.0
                    for (doc in result) {
                        val amount = doc.getDouble("amount") ?: 0.0
                        val type = doc.getString("type") ?: ""
                        balance += if (type == "income") amount else -amount
                    }
                    val balanceText = "Баланс: ${String.format("%.2f", balance)} ₽"
                    views.setTextViewText(R.id.tv_balance, balanceText)
                    appWidgetManager.updateAppWidget(widgetId, views)
                }
                .addOnFailureListener {
                    views.setTextViewText(R.id.tv_balance, "Баланс: —")
                    appWidgetManager.updateAppWidget(widgetId, views)
                }

            // Показываем виджет сразу, пока грузятся данные
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
