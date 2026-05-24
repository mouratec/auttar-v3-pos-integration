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
import br.com.auttar.sdk.common.transaction.model.type.TypeDebitTransaction
import br.com.auttar.sdk.common.transaction.model.type.TypeInitializeTransaction
import br.com.auttar.sdk.transaction.model.data.CreditData
import br.com.auttar.sdk.transaction.model.data.DebitData
import br.com.auttar.sdk.transaction.model.data.InitializeData
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

        // --- PAGAMENTOS ---
        binding.btnCredit.setOnClickListener {
            val amount = getAmountFromInput()
            startPayment(CreditData(amount, TypeCreditTransaction.CREDITO), "R$ $amount")
        }

        binding.btnPayDebit.setOnClickListener {
            val amount = getAmountFromInput()
            startPayment(DebitData(amount, TypeDebitTransaction.DEBITO), "R$ $amount")
        }

        binding.btnPayCreditInstallments.setOnClickListener {
            val amount = getAmountFromInput()
            // Em caso de erro de referência no REDITO/CREDITO, o padrão sênior é validar o enum. 
            // O SDK V3 comumente utiliza CREDITO_PARCELADO_SEM_JUROS.
            startPayment(CreditData(amount, TypeCreditTransaction.CREDITO_PARCELADO_SEM_JUROS), "R$ $amount (Parc. Loja)")
        }

        binding.btnPayCreditInstallmentsAdm.setOnClickListener {
            val amount = getAmountFromInput()
            startPayment(CreditData(amount, TypeCreditTransaction.CREDITO_PARCELADO_COM_JUROS), "R$ $amount (Parc. ADM)")
        }

        binding.btnPayPix.setOnClickListener {
            startAdministrativeFlow()
        }

        // --- DIGITADAS ---
        binding.btnPayCreditTyped.setOnClickListener {
            val amount = getAmountFromInput()
            startPayment(CreditData(amount, TypeCreditTransaction.CREDITO_DIGITADO), "R$ $amount (Digitado)")
        }

        binding.btnPayCreditInstallmentsTyped.setOnClickListener {
            val amount = getAmountFromInput()
            startPayment(CreditData(amount, TypeCreditTransaction.CREDITO_DIGITADO_PARCELADO_SEM_JUROS), "R$ $amount (Parc. Loja Digitado)")
        }

        binding.btnPayCreditInstallmentsAdmTyped.setOnClickListener {
            val amount = getAmountFromInput()
            startPayment(CreditData(amount, TypeCreditTransaction.CREDITO_DIGITADO_PARCELADO_COM_JUROS), "R$ $amount (Parc. ADM Digitado)")
        }

        // --- CONFIGURAÇÃO E MENU ---
        binding.btnAdminMenu.setOnClickListener {
            startAdministrativeFlow()
        }

        binding.btnConfigHardware.setOnClickListener {
            startAdministrativeFlow()
        }

        binding.btnCancelTransaction.setOnClickListener {
            startAdministrativeFlow()
        }
    }

    private fun getAmountFromInput(): BigDecimal {
        val text = binding.etAmount.text.toString().trim().replace(",", ".")
        return try {
            if (text.isBlank()) BigDecimal.ZERO else BigDecimal(text)
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
    }

    private fun startPayment(data: br.com.auttar.sdk.common.transaction.model.DefaultData<*>, displayAmount: String) {
        // Se o valor for zero, informamos que o SDK solicitará o valor no pinpad
        val finalDisplay = if (displayAmount.contains("0.00") || displayAmount.contains("0,00")) 
            "Aguardando valor no dispositivo..." 
        else 
            displayAmount

        val intent = Intent(this, TransactionActivity::class.java).apply {
            putExtra("AMOUNT", finalDisplay)
        }
        startActivity(intent)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                auttarSDK.initTransaction()
                auttarSDK.paymentTransaction(data)
            } catch (e: Exception) {
                Log.e("MAIN", "Erro ao iniciar pagamento: ${e.message}")
            }
        }
    }

    private fun startAdministrativeFlow() {
        val intent = Intent(this, TransactionActivity::class.java).apply {
            putExtra("AMOUNT", "OPERAÇÃO ADMINISTRATIVA")
        }
        startActivity(intent)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                auttarSDK.initTransaction()
                // Correção Senior: administrativeTransaction requer um parâmetro de dados (DefaultData).
                // InitializeData com TypeInitializeTransaction.INICIO_DIA é o padrão para o Menu Administrativo.
                val adminData = InitializeData(TypeInitializeTransaction.INICIO_DIA)
                auttarSDK.administrativeTransaction(adminData)
            } catch (e: Exception) {
                Log.e("MAIN", "Erro ao iniciar fluxo administrativo: ${e.message}")
            }
        }
    }

    private fun observeTransactionResult() {
        // Uso de lifecycleScope.launch sem repeatOnLifecycle para garantir a coleta
        // mesmo quando a MainActivity estiver em background (coberta pela TransactionActivity)
        lifecycleScope.launch {
            auttarSDK.transactionStateFlow.collect { state ->
                if (state is AuttarTransactionState.Done) {
                    val result = state.result

                    // Sinaliza para fechar a TransactionActivity
                    AuttarEventController.endTransactionFlow()

                    runOnUiThread {
                        if (result.returnCode == "0" || result.returnCode == "00") {
                            Log.d("MAIN", "Transação Confirmada com Sucesso. NSU: ${result.nsuCTF}")
                            binding.txtStatus.text = "✅ TRANSAÇÃO APROVADA\nNSU: ${result.nsuCTF}\nAutorização: ${result.authorizedCode}"
                            Toast.makeText(this@MainActivity, "Venda Aprovada e Confirmada!", Toast.LENGTH_LONG).show()
                        } else {
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