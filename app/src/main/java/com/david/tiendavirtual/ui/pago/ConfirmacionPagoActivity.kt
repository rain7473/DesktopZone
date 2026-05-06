package com.david.tiendavirtual.ui.pago

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.david.tiendavirtual.databinding.ActivityConfirmacionPagoBinding
import com.david.tiendavirtual.ui.home.MainActivity
import com.david.tiendavirtual.ui.pedidos.OrderHistoryActivity

class ConfirmacionPagoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmacionPagoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmacionPagoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val total   = intent.getDoubleExtra("total", 0.0)
        val orderId = intent.getStringExtra("orderId") ?: "—"
        val fecha   = intent.getStringExtra("fecha") ?: "—"

        binding.tvConfTotal.text   = "B/. %.2f".format(total)
        binding.tvConfPedidoId.text = "#$orderId"
        binding.tvConfFecha.text   = fecha

        binding.btnVerPedidos.setOnClickListener {
            val intent = Intent(this, OrderHistoryActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        binding.btnSeguirComprando.setOnClickListener {
            irAMainActivity()
        }

        // Al presionar atrás, ir directamente a MainActivity (carrito ya vacío)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                irAMainActivity()
            }
        })
    }

    private fun irAMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}

