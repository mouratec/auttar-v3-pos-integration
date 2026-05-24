package br.com.auttar.integration

import android.graphics.Bitmap
import android.util.Log
import br.com.auttar.sdk.Auttar
import br.com.auttar.sdk.common.data.ui.RefundConfirmData
import br.com.auttar.sdk.interfaces.ClientUserInterface
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class AuttarUIEvent {
    data class ShowMessage(val message: String) : AuttarUIEvent()
    data class AskForValue(val display: String) : AuttarUIEvent()
    data class AskForConfirm(val display: String) : AuttarUIEvent()
    data class ShowInsertCard(val display: String) : AuttarUIEvent()
    data class ShowContactlessCard(val status: String) : AuttarUIEvent()
    object FinishTransaction : AuttarUIEvent()
}

object AuttarEventController : ClientUserInterface {

    private const val TAG = "AuttarController"
    private val _events = MutableSharedFlow<AuttarUIEvent>(extraBufferCapacity = 20)
    val events = _events.asSharedFlow()

    private fun getResponse() = Auttar.getSDK().getObjectResponseCallback()

    override fun showQrCode(qrCodeImage: Bitmap, pinCode: String, transactionValue: String, transactionType: String) {
        _events.tryEmit(AuttarUIEvent.ShowMessage("Aguardando Pagamento QR Code..."))
    }

    override fun hideQrCode() {
        _events.tryEmit(AuttarUIEvent.ShowMessage("Processando..."))
    }

    override fun askGenericCapture(display: String, mask: String, maxLength: Int, editionMode: Int) {
        _events.tryEmit(AuttarUIEvent.AskForValue(display))
    }

    override fun askForValue(display: String) {
        _events.tryEmit(AuttarUIEvent.AskForValue(display))
    }

    override fun messageDisplay(message: String) {
        _events.tryEmit(AuttarUIEvent.ShowMessage(message))
    }

    override fun askForDate(display: String, mask: String) {
        _events.tryEmit(AuttarUIEvent.AskForValue(display))
    }

    override fun cleanDisplay() {
        _events.tryEmit(AuttarUIEvent.ShowMessage(""))
    }

    override fun askForConfirm(display: String) {
        _events.tryEmit(AuttarUIEvent.AskForConfirm(display))
    }

    override fun askSelect(display: String, options: Array<String>) {
        val textToDisplay = "$display\nOpções: ${options.joinToString(", ")}"
        _events.tryEmit(AuttarUIEvent.AskForValue(textToDisplay))
    }

    override fun showInsertCardMessage(display: String) {
        _events.tryEmit(AuttarUIEvent.ShowInsertCard(display))
    }

    override fun showPin(display: String) {
        _events.tryEmit(AuttarUIEvent.ShowMessage(display.ifBlank { "Digite a Senha no Teclado" }))
    }

    override fun showContactlessCard(status: String) {
        _events.tryEmit(AuttarUIEvent.ShowContactlessCard(status))
    }

    override fun askForConfirmRefund(refundData: RefundConfirmData) {
        _events.tryEmit(AuttarUIEvent.AskForConfirm("Confirmar operação de estorno?"))
    }

    fun sendUserInputValue(value: String, isAborted: Boolean = false) {
        try { getResponse().askForValue(value, isAborted) } catch (e: Exception) { Log.e(TAG, "Erro", e) }
    }

    fun sendUserConfirmation(confirmed: Boolean) {
        try { getResponse().askForConfirm(confirmed) } catch (e: Exception) { Log.e(TAG, "Erro", e) }
    }

    fun abortTransactionFromUI() {
        try {
            getResponse().askForValue("", true)
            _events.tryEmit(AuttarUIEvent.FinishTransaction)
        } catch (e: Exception) { Log.e(TAG, "Erro", e) }
    }

    fun endTransactionFlow() {
        _events.tryEmit(AuttarUIEvent.FinishTransaction)
    }
}