package br.com.auttar.integration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.com.auttar.integration.databinding.ActivityMainBinding
import br.com.auttar.sdk.Auttar
import br.com.auttar.sdk.AuttarTransactionState
import br.com.auttar.sdk.common.transaction.model.type.TypeCreditTransaction
import br.com.auttar.sdk.transaction.model.data.CreditData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auttarSDK = Auttar.getSDK()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeTransactionResult()

        binding.btnCredit.setOnClickListener {
            startTransactionFlow(10.50)
        }
    }

    private fun startTransactionFlow(amount: Double) {
        val intent = Intent(this, TransactionActivity::class.java).apply {
            putExtra("AMOUNT", "R$ %.2f".format(amount))
        }
        startActivity(intent)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                auttarSDK.initTransaction()
                val creditData = CreditData(BigDecimal.valueOf(amount), TypeCreditTransaction.CREDITO)
                auttarSDK.paymentTransaction(creditData)
            } catch (e: Exception) {
                Log.e("MAIN", "Erro Crítico ao iniciar transação: ${e.message}")
            }
        }
    }

    private fun observeTransactionResult() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                auttarSDK.transactionStateFlow.collect { state ->
                    if (state is AuttarTransactionState.Done) {
                        val result = state.result

                        AuttarEventController.endTransactionFlow()

                        runOnUiThread {
                            if (result.returnCode == "0" || result.returnCode == "00") {
                                // CONFIRMAÇÃO EXPLICITA DA APROVAÇÃO
                                Log.d("MAIN", "Transação Confirmada com Sucesso. NSU: ${result.nsuCTF}")
                                binding.txtStatus.text = "✅ TRANSAÇÃO APROVADA\nNSU: ${result.nsuCTF}\nAutorização: ${result.authorizedCode}"
                                Toast.makeText(this@MainActivity, "Venda Aprovada e Confirmada!", Toast.LENGTH_LONG).show()

                                // Caso o SDK Auttar V3 exija comando direto para confirmar o bloco TEF:
                                try {
                                    // auttarSDK.confirm() // Descomente caso a máquina não imprima o comprovante sozinha
                                } catch (e: Exception) { }

                            } else {
                                // FALHA OU CANCELAMENTO
                                binding.txtStatus.text = "❌ FALHA NA TRANSAÇÃO\nMotivo: ${result.display}"
                                android.app.AlertDialog.Builder(this@MainActivity)
                                    .setTitle("Transação Finalizada")
                                    .setMessage("Status: ${result.display}")
                                    .setPositiveButton("OK", null)
                                    .show()
                            }
                        }
                    }
                }
            }
        }
    }
}