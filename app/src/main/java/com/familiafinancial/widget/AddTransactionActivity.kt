package com.familiafinancial.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var transactionType = "income"
    private var widgetId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        db = FirebaseFirestore.getInstance()
        transactionType = intent.getStringExtra("type") ?: "income"
        widgetId = intent.getIntExtra("widgetId", 0)

        val tvTitle = findViewById<TextView>(R.id.tv_title)
        val etAmount = findViewById<EditText>(R.id.et_amount)
        val etComment = findViewById<EditText>(R.id.et_comment)
        val btnSave = findViewById<Button>(R.id.btn_save)
        val btnCancel = findViewById<Button>(R.id.btn_cancel)

        tvTitle.text = if (transactionType == "income") "Добавить доход" else "Добавить расход"

        btnCancel.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val amountText = etAmount.text.toString().trim()
            if (amountText.isEmpty()) {
                Toast.makeText(this, "Введите сумму", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Введите корректную сумму", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = hashMapOf(
                "type" to transactionType,
                "amount" to amount,
                "comment" to etComment.text.toString().trim(),
                "timestamp" to Timestamp.now()
            )

            btnSave.isEnabled = false
            db.collection("transactions")
                .add(transaction)
                .addOnSuccessListener {
                    Toast.makeText(this, "Сохранено!", Toast.LENGTH_SHORT).show()
                    refreshWidget()
                    finish()
                }
                .addOnFailureListener { e ->
                    btnSave.isEnabled = true
                    Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun refreshWidget() {
        val manager = AppWidgetManager.getInstance(this)
        val ids = manager.getAppWidgetIds(ComponentName(this, FinanceWidget::class.java))
        for (id in ids) {
            FinanceWidget.updateWidget(this, manager, id)
        }
    }
}
