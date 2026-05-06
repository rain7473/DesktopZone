package com.david.tiendavirtual.ui.pago

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.repository.FacturaRepository
import com.david.tiendavirtual.databinding.ActivityPagoBinding
import com.david.tiendavirtual.ui.productos.CarritoManager
import com.david.tiendavirtual.utils.LocalOrderStore
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PagoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPagoBinding
    private lateinit var repo: FacturaRepository
    private var formatandoFecha = false
    private var totalGuardado   = 0.0
    private var tipoTarjeta     = ""   // "VISA" | "MASTERCARD"

    // Mapa: cardView → (tipo, label, checkImageViewId)
    private data class MetodoPago(val tipo: String, val label: String, val checkId: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPagoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo          = FacturaRepository(this)
        totalGuardado = CarritoManager.calcularTotal()
        binding.tvTotalPagar.text = "B/. %.2f".format(totalGuardado)
        binding.btnVolverPago.setOnClickListener { finish() }

        configurarSelectorMetodoPago()

        // Máscara automática para fecha MM/AA
        binding.etFechaVence.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (formatandoFecha) return
                formatandoFecha = true
                val digits    = s.toString().filter { it.isDigit() }.take(4)
                val formatted = if (digits.length >= 3) "${digits.take(2)}/${digits.drop(2)}" else digits
                binding.etFechaVence.setText(formatted)
                binding.etFechaVence.setSelection(formatted.length)
                formatandoFecha = false
            }
        })

        binding.etDigitos.filters = arrayOf(InputFilter.LengthFilter(16))

        binding.btnConfirmarPago.setOnClickListener {
            if (tipoTarjeta.isEmpty()) {
                Snackbar.make(binding.root, "⚠️ Selecciona un método de pago", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!validarCampos()) return@setOnClickListener
            iniciarProcesoPago()
        }
    }

    // ── Selector de método de pago ─────────────────────────────────────────
    private fun configurarSelectorMetodoPago() {
        val opciones = mapOf(
            binding.cardVisaCredito to MetodoPago("VISA",       "Visa Crédito",        R.id.ivCheckVisaCredito),
            binding.cardVisaDebito  to MetodoPago("VISA",       "Visa Débito",         R.id.ivCheckVisaDebito),
            binding.cardMcCredito   to MetodoPago("MASTERCARD", "Mastercard Crédito",  R.id.ivCheckMcCredito),
            binding.cardMcDebito    to MetodoPago("MASTERCARD", "Mastercard Débito",   R.id.ivCheckMcDebito)
        )

        val strokeColor      = getColor(R.color.green_primary)
        val strokeColorNone  = 0x00000000

        opciones.forEach { (card, metodo) ->
            card.setOnClickListener {
                // Limpiar selección previa
                opciones.forEach { (c, m) ->
                    c.strokeWidth = 0
                    c.strokeColor = strokeColorNone
                    findViewById<View>(m.checkId).visibility = View.GONE
                }
                // Marcar seleccionada
                card.strokeWidth = 3
                card.strokeColor = strokeColor
                findViewById<View>(metodo.checkId).visibility = View.VISIBLE
                tipoTarjeta = metodo.tipo
                binding.tvMetodoSeleccionado.text = "✅  ${metodo.label} seleccionada"
                binding.tvMetodoSeleccionado.setTextColor(getColor(R.color.green_primary))
            }
        }
    }

    // ── Validación de campos ──────────────────────────────────────────────
    private fun validarCampos(): Boolean {
        var valido = true

        val digitos = binding.etDigitos.text.toString().trim()
        val fecha   = binding.etFechaVence.text.toString().trim()
        val cvv     = binding.etCvv.text.toString().trim()

        if (!digitos.matches(Regex("\\d{16}"))) {
            binding.tilDigitos.error = if (digitos.isEmpty()) "Ingresa el número de tarjeta" else "Debe tener exactamente 16 dígitos"
            valido = false
        } else { binding.tilDigitos.error = null }

        if (!fecha.matches(Regex("(0[1-9]|1[0-2])/\\d{2}"))) {
            binding.tilFecha.error = if (fecha.isEmpty()) "Ingresa la fecha de vencimiento" else "Formato inválido (MM/AA)"
            valido = false
        } else {
            val parts = fecha.split("/")
            val mes   = parts[0].toInt()
            val anio  = parts[1].toInt() + 2000
            val cal   = java.util.Calendar.getInstance()
            val mesActual  = cal.get(java.util.Calendar.MONTH) + 1
            val anioActual = cal.get(java.util.Calendar.YEAR)
            if (anio < anioActual || (anio == anioActual && mes < mesActual)) {
                binding.tilFecha.error = "La tarjeta está vencida"
                valido = false
            } else {
                binding.tilFecha.error = null
            }
        }

        if (!cvv.matches(Regex("\\d{3,4}"))) {
            binding.tilCvv.error = if (cvv.isEmpty()) "Ingresa el CVV" else "CVV inválido (3-4 dígitos)"
            valido = false
        } else { binding.tilCvv.error = null }

        return valido
    }

    // ── Paso 1: Obtener / crear tarjeta ───────────────────────────────────
    private fun iniciarProcesoPago() {
        setLoading(true)

        val digitos = binding.etDigitos.text.toString().trim()
        val cvv     = binding.etCvv.text.toString().trim()
        val fecha   = binding.etFechaVence.text.toString().trim()

        repo.obtenerOCrearTarjeta(
            digitos      = digitos,
            codSeguridad = cvv,
            fechaVence   = fecha,
            tipo         = tipoTarjeta,
            onSuccess    = { idTarjeta, _ -> crearFactura(idTarjeta) },
            onError      = { msg ->
                setLoading(false)
                mostrarError(msg)
            }
        )
    }

    // ── Paso 2: Crear factura + detalles en BD ────────────────────────────
    private fun crearFactura(idTarjeta: Int) {
        repo.crearFactura(
            idTarjeta = idTarjeta,
            subtotal  = CarritoManager.calcularSubtotal(),
            itbms     = CarritoManager.calcularItbms(),
            total     = totalGuardado,
            items     = CarritoManager.obtenerItems(),
            onSuccess = { idFactura ->
                // Guardar en SharedPreferences para que solo este dispositivo vea su pedido
                LocalOrderStore.guardarPedido(this@PagoActivity, idFactura)
                CarritoManager.vaciar()
                navegarAConfirmacion(idFactura)
            },
            onError   = { msg ->
                setLoading(false)
                mostrarError(msg)
            }
        )
    }

    // ── Navegación a pantalla de confirmación ─────────────────────────────
    private fun navegarAConfirmacion(idFactura: Int) {
        val fechaStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val intent = Intent(this, ConfirmacionPagoActivity::class.java).apply {
            putExtra("total",   totalGuardado)
            putExtra("orderId", idFactura.toString())
            putExtra("fecha",   fechaStr)
        }
        startActivity(intent)
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.btnConfirmarPago.isEnabled = !loading
        binding.progressPago.visibility    = if (loading) View.VISIBLE else View.GONE
    }

    private fun mostrarError(msg: String) {
        Snackbar.make(binding.root, "❌ $msg", Snackbar.LENGTH_LONG).show()
    }
}
