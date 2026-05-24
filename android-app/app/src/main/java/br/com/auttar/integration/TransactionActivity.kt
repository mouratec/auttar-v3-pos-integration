package br.com.auttar.integration

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import br.com.auttar.integration.databinding.ActivityTransactionBinding
import kotlinx.coroutines.launch

class TransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val amount = intent.getStringExtra("AMOUNT") ?: "R$ 0,00"
        binding.txtStatus.text = "Processando...\n$amount"

        setupButtons()
        observeEvents()
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            AuttarEventController.abortTransactionFromUI()
            finish()
        }

        binding.btnConfirmInput.setOnClickListener {
            val input = binding.etInput.text.toString()
            if (input.isNotBlank()) {
                AuttarEventController.sendUserInputValue(input)
                binding.inputContainer.visibility = View.GONE
                binding.etInput.text.clear()
            }
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                AuttarEventController.events.collect { event ->
                    when (event) {
                        is AuttarUIEvent.ShowMessage -> {
                            binding.txtStatus.text = event.message
                            binding.inputContainer.visibility = View.GONE
                        }
                        is AuttarUIEvent.ShowInsertCard -> {
                            binding.txtStatus.text = event.display.ifBlank { "Insira ou Aproxime o Cartão" }
                        }
                        is AuttarUIEvent.ShowContactlessCard -> {
                            binding.txtStatus.text = "Aproxime o cartão..."
                        }
                        is AuttarUIEvent.AskForValue -> {
                            binding.txtStatus.text = event.display
                            binding.inputContainer.visibility = View.VISIBLE
                            binding.etInput.requestFocus()
                        }
                        is AuttarUIEvent.AskForConfirm -> {
                            binding.txtStatus.text = event.display
                            AuttarEventController.sendUserConfirmation(true)
                        }
                        is AuttarUIEvent.FinishTransaction -> {
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() { } // Bloqueio intencional
}