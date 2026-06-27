package com.familiafinancial.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.google.firebase.auth.FirebaseAuth
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

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            val incomeIntent = Intent(context, AddTransactionActivity::class.java).apply {
                putExtra("type", "income")
                putExtra("widgetId", widgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setOnClickPendingIntent(
                R.id.btn_income,
                PendingIntent.getActivity(context, widgetId * 2, incomeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )

            val expenseIntent = Intent(context, AddTransactionActivity::class.java).apply {
                putExtra("type", "expense")
                putExtra("widgetId", widgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            views.setOnClickPendingIntent(
                R.id.btn_expense,
                PendingIntent.getActivity(context, widgetId * 2 + 1, expenseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )

            appWidgetManager.updateAppWidget(widgetId, views)
            signInAndLoadBalance(views, appWidgetManager, widgetId)
        }

        private fun signInAndLoadBalance(
            views: RemoteViews,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                loadBalance(views, appWidgetManager, widgetId)
            } else {
                auth.signInAnonymously().addOnSuccessListener {
                    loadBalance(views, appWidgetManager, widgetId)
                }
            }
        }

        private fun loadBalance(
            views: RemoteViews,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            FirebaseFirestore.getInstance().collection("finances")
                .get()
                .addOnSuccessListener { result ->
                    var balance = 0.0
                    for (doc in result) {
                        val amount = doc.getDouble("amount") ?: 0.0
                        val type = doc.getString("type") ?: ""
                        balance += if (type == "income") amount else -amount
                    }
                    views.setTextViewText(R.id.tv_balance, "Баланс: %.0f ₸".format(balance))
                    appWidgetManager.updateAppWidget(widgetId, views)
                }
                .addOnFailureListener {
                    views.setTextViewText(R.id.tv_balance, "Баланс: —")
                    appWidgetManager.updateAppWidget(widgetId, views)
                }
        }
    }
}
