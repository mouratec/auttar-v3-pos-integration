package br.com.auttar.integration

import android.app.Application
import br.com.auttar.sdk.Auttar

class AuttarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Passo Obrigatório: Inicializar o SDK antes de qualquer chamada ao getSDK()
        // Use initHomolog para testes ou initProdution para produção.
        Auttar.initHomolog(this)

        // Agora o AuttarEventController pode se conectar, pois o grafo de dependências está pronto
        Auttar.getSDK().connectUI(AuttarEventController)
    }
}