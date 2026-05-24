package br.com.auttar.integration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import br.com.auttar.integration.databinding.ActivityLoginBinding
import br.com.auttar.sdk.Auttar
import br.com.auttar.sdk.authentication.ParameterLoginIntegrationMonoEC
import br.com.auttar.sdk.interfaces.authentication.LoginResultInterface
import br.com.auttar.sdk.interfaces.authentication.LoginResultListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auttarSDK = Auttar.getSDK()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Auttar.getConfigSDK().scanPermissions(this)
        startTerminalSetup()
    }

    private fun startTerminalSetup() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRetry.visibility = View.GONE
        binding.txtStatus.text = "Iniciando Autenticação Segura..."

        val params = ParameterLoginIntegrationMonoEC("hti", "h123456", "41602316000130", null)

        try {
            auttarSDK.authenticationMonoEC(params, object : LoginResultListener {
                override fun onResult(result: LoginResultInterface) {
                    runOnUiThread {
                        Log.d("LOGIN_SDK", "Resposta da autenticação: ${result.code} - ${result.message}")
                        if (result.code == 0) {
                            binding.txtStatus.text = "Terminal Pronto!"
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } else {
                            showError("Falha na autenticação: ${result.message}")
                        }
                    }
                }
            })
        } catch (e: Exception) {
            showError("Falha crítica ao iniciar: ${e.message}")
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.txtStatus.text = message
        binding.btnRetry.visibility = View.VISIBLE
        binding.btnRetry.setOnClickListener { startTerminalSetup() }
    }
}