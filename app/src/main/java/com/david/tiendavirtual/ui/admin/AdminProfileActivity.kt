package com.david.tiendavirtual.ui.admin

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.david.tiendavirtual.data.model.UserSession
import com.david.tiendavirtual.databinding.ActivityAdminProfileBinding
import com.david.tiendavirtual.ui.login.LoginActivity

class AdminProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarDatosUsuario()
        configurarBotones()
        animarEntrada()
    }

    private fun configurarDatosUsuario() {
        val rolNombre = if (UserSession.isAdmin) "Administrador" else "Empleado"
        binding.tvAvatarInicial.text = UserSession.inicial.toString()
        binding.tvNombreCompleto.text = UserSession.nombreCompleto
        binding.tvUsername.text = "@${UserSession.usuario}"
        binding.tvRolChip.text = rolNombre

        // Info card
        binding.tvInfoNombre.text = UserSession.nombreCompleto
        binding.tvInfoUsuario.text = UserSession.usuario
        binding.tvInfoRol.text = rolNombre
    }

    private fun configurarBotones() {
        binding.btnVolver.setOnClickListener {
            finishAfterTransition()
        }

        binding.btnVolverAbajo.setOnClickListener {
            finishAfterTransition()
        }

        binding.btnCerrarSesion.setOnClickListener {
            // Microanimación en botón
            val scaleX = ObjectAnimator.ofFloat(it, View.SCALE_X, 1f, 0.95f, 1f)
            val scaleY = ObjectAnimator.ofFloat(it, View.SCALE_Y, 1f, 0.95f, 1f)
            val set = AnimatorSet()
            set.playTogether(scaleX, scaleY)
            set.duration = 200
            set.start()

            it.postDelayed({
                cerrarSesion()
            }, 250)
        }
    }

    private fun cerrarSesion() {
        UserSession.limpiar()
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun animarEntrada() {
        // Avatar: scale + fade
        val avatar = binding.tvAvatarInicial.parent as View
        avatar.scaleX = 0f
        avatar.scaleY = 0f
        avatar.alpha = 0f

        val scaleX = ObjectAnimator.ofFloat(avatar, View.SCALE_X, 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(avatar, View.SCALE_Y, 0f, 1f)
        val alphaAvatar = ObjectAnimator.ofFloat(avatar, View.ALPHA, 0f, 1f)
        val avatarSet = AnimatorSet().apply {
            playTogether(scaleX, scaleY, alphaAvatar)
            duration = 450
            interpolator = OvershootInterpolator(1.2f)
            startDelay = 100
        }

        // Nombre: slide up + fade
        binding.tvNombreCompleto.translationY = 30f
        binding.tvNombreCompleto.alpha = 0f
        val nombreAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.tvNombreCompleto, View.TRANSLATION_Y, 30f, 0f),
                ObjectAnimator.ofFloat(binding.tvNombreCompleto, View.ALPHA, 0f, 1f)
            )
            duration = 350
            startDelay = 250
        }

        // Username: slide up + fade
        binding.tvUsername.translationY = 20f
        binding.tvUsername.alpha = 0f
        val usernameAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.tvUsername, View.TRANSLATION_Y, 20f, 0f),
                ObjectAnimator.ofFloat(binding.tvUsername, View.ALPHA, 0f, 1f)
            )
            duration = 350
            startDelay = 350
        }

        // Botones: fade in
        binding.btnCerrarSesion.alpha = 0f
        binding.btnVolverAbajo.alpha = 0f
        val btnLogoutAnim = ObjectAnimator.ofFloat(binding.btnCerrarSesion, View.ALPHA, 0f, 1f).apply {
            duration = 400
            startDelay = 500
        }
        val btnVolverAnim = ObjectAnimator.ofFloat(binding.btnVolverAbajo, View.ALPHA, 0f, 1f).apply {
            duration = 400
            startDelay = 550
        }

        AnimatorSet().apply {
            playTogether(avatarSet, nombreAnim, usernameAnim, btnLogoutAnim, btnVolverAnim)
            start()
        }
    }

    override fun onBackPressed() {
        finishAfterTransition()
    }
}


