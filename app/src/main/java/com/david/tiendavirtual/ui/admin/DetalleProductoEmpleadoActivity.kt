package com.david.tiendavirtual.ui.admin

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.david.tiendavirtual.R
import com.david.tiendavirtual.data.model.Producto
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.data.repository.AdminProductoRepository
import com.david.tiendavirtual.databinding.ActivityDetalleProductoEmpleadoBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

/**
 * DetalleProductoEmpleadoActivity
 * Pantalla de detalle completo de un producto.
 * - Empleados: solo lectura.
 * - Administradores: lectura + botones Editar y Eliminar funcionales.
 */
class DetalleProductoEmpleadoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCTO = "extra_producto_detalle"
    }

    private lateinit var binding:  ActivityDetalleProductoEmpleadoBinding
    private lateinit var repo:     AdminProductoRepository
    private var productoActual: Producto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleProductoEmpleadoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = AdminProductoRepository(this)

        // Toolbar con botón volver
        setSupportActionBar(binding.toolbarDetalle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        binding.toolbarDetalle.setNavigationOnClickListener { finish() }

        @Suppress("DEPRECATION")
        val producto = intent.getSerializableExtra(EXTRA_PRODUCTO) as? Producto
        if (producto == null) { finish(); return }

        productoActual = producto
        poblarVista(producto)
        configurarAccionesAdmin(producto)
    }

    // ── Poblar toda la UI con los datos del producto ──────────────────────
    private fun poblarVista(p: Producto) {
        binding.tvDetalleNombre.text    = p.nombre
        binding.tvDetalleCategoria.text = p.categoria?.uppercase() ?: "SIN CATEGORÍA"
        binding.tvDetalleMarca.text     = p.marca?.uppercase()     ?: "SIN MARCA"
        binding.tvDetallePrecio.text    = "B/. %.2f".format(p.precioVenta)

        if (p.stock > 0) {
            binding.tvDetalleEstado.text = "En stock"
            binding.tvDetalleEstado.setTextColor(ContextCompat.getColor(this, R.color.green_dark))
            binding.cardDetalleEstado.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.green_light))
        } else {
            binding.tvDetalleEstado.text = "Agotado"
            binding.tvDetalleEstado.setTextColor(ContextCompat.getColor(this, R.color.danger_dark))
            binding.cardDetalleEstado.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.danger_bg))
        }

        binding.tvDetalleStock.text = p.stock.toString()
        binding.tvDetalleStock.setTextColor(
            if (p.stock > 0) ContextCompat.getColor(this, R.color.green_primary)
            else             Color.parseColor("#E53935")
        )
        binding.tvDetalleUnidad.text = p.unidad
        binding.tvDetalleId.text     = p.idProducto.toString()
        binding.tvDetalleDescripcion.text = p.descripcion.ifEmpty { "Sin descripción disponible." }

        // Imagen hero
        val skeleton = ColorDrawable(Color.parseColor("#F0F0F0"))
        if (!p.imagen.isNullOrEmpty()) {
            Glide.with(this)
                .load(p.imagen)
                .placeholder(skeleton)
                .error(skeleton)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .fitCenter()
                .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                    override fun onLoadFailed(e: com.bumptech.glide.load.engine.GlideException?,
                        model: Any?, target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                        isFirstResource: Boolean): Boolean {
                        binding.viewDetalleSkeleton.visibility = View.GONE; return false
                    }
                    override fun onResourceReady(resource: android.graphics.drawable.Drawable,
                        model: Any, target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                        dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
                        binding.viewDetalleSkeleton.visibility = View.GONE; return false
                    }
                })
                .into(binding.ivDetalleHero)
        } else {
            binding.ivDetalleHero.setImageResource(R.drawable.ic_inventory_box)
            binding.ivDetalleHero.setColorFilter(ContextCompat.getColor(this, R.color.green_primary))
            binding.viewDetalleSkeleton.visibility = View.GONE
        }
    }

    // ── Mostrar/ocultar sección de acciones según rol ─────────────────────
    private fun configurarAccionesAdmin(p: Producto) {
        if (UserSession.isAdmin) {
            // Ocultar aviso de solo lectura, mostrar botones admin
            binding.layoutAvisoSoloLectura.visibility = View.GONE
            binding.layoutAccionesAdmin.visibility    = View.VISIBLE
            binding.viewSombraBottomBar.visibility    = View.VISIBLE

            // Editar → abre AdminProductoFormActivity con el producto
            binding.btnDetalleEditar.setOnClickListener {
                val intent = Intent(this, AdminProductoFormActivity::class.java)
                intent.putExtra(AdminProductoFormActivity.EXTRA_PRODUCTO, p)
                startActivity(intent)
            }

            // Eliminar → diálogo de confirmación
            binding.btnDetalleEliminar.setOnClickListener {
                confirmarEliminar(p)
            }

        } else {
            // Empleado: mostrar aviso de solo lectura, ocultar botones
            binding.layoutAvisoSoloLectura.visibility = View.VISIBLE
            binding.layoutAccionesAdmin.visibility    = View.GONE
            binding.viewSombraBottomBar.visibility    = View.GONE
        }
    }

    // ── Diálogo de confirmación para eliminar ─────────────────────────────
    private fun confirmarEliminar(p: Producto) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Eliminar «${p.nombre}»?\nEsta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                binding.btnDetalleEliminar.isEnabled = false
                binding.btnDetalleEliminar.text      = "Eliminando…"

                repo.eliminarProducto(
                    idProducto = p.idProducto,
                    onSuccess  = { msg ->
                        Snackbar.make(binding.root, "✓ $msg", Snackbar.LENGTH_SHORT).show()
                        // Regresar con resultado OK para que el inventario se refresque
                        setResult(RESULT_OK)
                        finish()
                    },
                    onError    = { msg ->
                        binding.btnDetalleEliminar.isEnabled = true
                        binding.btnDetalleEliminar.text      = "Eliminar"
                        Snackbar.make(binding.root, "❌ $msg", Snackbar.LENGTH_LONG).show()
                    }
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
