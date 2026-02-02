# Resumo dos Testes da Classe LatinRequirementsTest

A classe `LatinRequirementsTest.java` é uma suíte de testes de instrumentação para Android que verifica uma grande variedade de requisitos de customização de operadoras (CSC/OMC) para dispositivos Samsung na América Latina. Os testes validam desde configurações de rede e APN até a presença de ícones, aplicativos pré-instalados e sons de operadora.

Aqui está a lista aninhada dos testes e um resumo do que cada um faz:

*   **`testOmcVersion()`**
    *   Resumo: (ID: 930) Verifica se a feature flag `CscFeature_Common_AutoConfigurationType` está configurada corretamente de acordo com a versão do OMC (Open Market Customization) do dispositivo, especialmente para versões anteriores a 5.0.
*   **`testChannel50Feature()`**
    *   Resumo: Valida se o canal 50 do Cell Broadcast (usado para alertas) está habilitado corretamente, dependendo da versão do sistema operacional Android.
*   **`testDataRomaing()`**
    *   Resumo: (ID: 480) Testa se a configuração padrão para roaming de dados (`carrier_default_data_roaming_enabled_bool`) está ativada, com lógica específica para diferentes versões do Android e códigos de venda (SKUs).
*   **`testDisabledDataRomaing()`**
    *   Resumo: (ID: 481) Complementa o teste anterior, verificando cenários onde o roaming de dados deve estar desativado ou ativado, dependendo da combinação de versão do SO e SKU.
*   **`testLteOnlyMenu()`**
    *   Resumo: (ID: 460) Verifica se o menu de seleção de rede "Apenas LTE" (`CscFeature_Setting_CustNetworkSelMenu4`) está configurado corretamente.
*   **`testDefaultBrowser()`**
    *   Resumo: (ID: 937) Valida se o navegador padrão é o Google Chrome e se a feature flag correspondente (`CscFeature_Web_ConfigDefaultBrowser`) está correta para certos modelos e versões do SO.
*   **`testMitelCelPermissions()`**
    *   Resumo: (ID: 912) Checa se o aplicativo "Mitel Cel" (`com.speedymovil.wire`) está instalado e se possui as permissões necessárias (Telefone, SMS, etc.).
*   **`testPhoneBookMatching()`**
    *   Resumo: (ID: 91) Testa a funcionalidade de busca de contatos na agenda (Phonebook) por número de telefone.
*   **`testSamsungTTS()`**
    *   Resumo: (ID: 80) Garante que o serviço de Text-to-Speech (TTS) da Samsung inclua suporte para os idiomas Português (Brasil) e Espanhol (México).
*   **`testServiceProvideCode()`**
    *   Resumo: (ID: 240) Verifica as configurações do código de provedor de serviço para chamadas de longa distância no Brasil, validando se as tags e preferências estão corretas, especialmente para modelos TSS.
*   **`testHiddenApnMex()`**
    *   Resumo: (ID: 902) Para operadoras do México, valida se a APN de SUPL (`supl.attmex.mx`) está corretamente oculta da lista de APNs visível ao usuário.
*   **`testHiddenApnPnt()`**
    *   Resumo: (ID: 903) Para a operadora PNT, valida se a APN `supl.google.com` está corretamente oculta.
*   **`testSpeedDialForCoe()`**
    *   Resumo: (ID: 450) Confere se o número de discagem rápida (Speed Dial) para a operadora COE está configurado corretamente.
*   **`testSpeedDialForZtr()`**
    *   Resumo: (ID: 430) Confere os números de discagem rápida para a operadora ZTR, com lógica para remover em modelos TSS.
*   **`testSpeedDialForZvv()`**
    *   Resumo: (ID: 440) Verifica se a lista de discagem rápida para a operadora ZVV está vazia, como esperado.
*   **`testSpeedDialForPsn()`**
    *   Resumo: (ID: 420) Garante que não há configurações de discagem rápida para a operadora PSN a partir do Android 10 (QQ).
*   **`testSpeedDialForIus()`**
    *   Resumo: (ID: 400) Valida a configuração de discagem rápida para a operadora IUS, que varia com a versão do Android.
*   **`testSpeedDialForCti()`**
    *   Resumo: (ID: 360) Garante que não há discagem rápida configurada para a operadora CTI a partir do Android 10 (QQ).
*   **`testSpeedDialForIce()`**
    *   Resumo: (ID: 390) Assegura que a lista de discagem rápida para a operadora ICE está vazia.
*   **`testNfcIcon()`**
    *   Resumo: Valida se o ícone do NFC é exibido na barra de status, verificando as feature flags `CscFeature_SystemUI_ConfigDefIndicatorAdditionalSystemIcon` e `CscFeature_NFC_StatusBarIconType`.
*   **`testDisableNetworkModeMenu()`**
    *   Resumo: Confere se a opção de "modo de rede preferencial" está desabilitada no menu de configurações de rede móvel.
*   **`testDisableNetworkMode()`**
    *   Resumo: (ID: 922) Verifica se certas opções de modo de rede (como "wcdmagsmonly") estão desabilitadas.
*   **`testNetworkModeMenu5G()`**
    *   Resumo: Testa a visibilidade e configuração do menu de rede 5G com base no suporte do hardware do dispositivo e na operadora.
*   **`testCellBroadcastForMex()`**
    *   Resumo: (ID: 410) Para o México, verifica se os canais de Cell Broadcast (alerta de emergência) estão ativos.
*   **`testVMNumberEdition()`**
    *   Resumo: (ID: 350) Garante que a edição do número da caixa postal (Voicemail) pelo usuário está desativada.
*   **`testOmcResourceSoundPath()`**
    *   Resumo: (ID: 570) Valida se os caminhos dos recursos de som (toques, notificações) da operadora estão configurados corretamente via OMC.
*   **`testUaProf()`**
    *   Resumo: (ID: 70) Verifica se a URL do Perfil de Agente de Usuário (UAProf) para MMS está configurada com o valor esperado.
*   **`testSupportRmm()`**
    *   Resumo: (ID: 11) Testa a feature flag `CscFeature_Common_SupportRmm`, que depende da presença do aplicativo SITIC na ROM.
*   **`testComPermission()`**
    *   Resumo: (ID: 950) Checa se o aplicativo SITIC (`co.sitic.pp`) está instalado e com as permissões corretas.
*   **`testDlnaSupport()`**
    *   Resumo: (ID: 10) Valida o suporte a DLNA (Smart View), conferindo se a feature flag está correta em relação à instalação do pacote `com.samsung.android.smartmirroring`.
*   **`testRegulatoryInfo()`**
    *   Resumo: (IDs: 800, 820, 806) Verifica a presença da imagem de informações regulatórias (ex: ANATEL, IFT) e sua respectiva tag de ativação para países como México e Costa Rica.
*   **`testSoundsForMovistar()`**
    *   Resumo: (ID: 569) Para a Movistar, garante que sons de boot customizados não estão presentes e que a sincronização de som de boot está desativada.
*   **`testDualCustomization()`**
    *   Resumo: (ID: 860) Testa cenários de customização dupla (Dual SIM), onde o toque padrão muda dependendo da operadora do SIM inserido (UNE vs. IUS).
*   **`testHomeUrl()`**
    *   Resumo: (ID: 1040) Verifica a configuração da página inicial (Home URL) do navegador, que pode variar automaticamente com o SIM inserido.
*   **`testGalaxyBookmark()`**
    *   Resumo: (ID: 601) Valida a presença e a URL correta do bookmark "Galaxy Shop" no navegador, que muda de acordo com a região (Brasil, México, etc.).
*   **`testPermanentDataIcon()`**
    *   Resumo: (IDs: 290, 280) Confere se o ícone de dados na barra de status exibe a marca da operadora permanentemente.
*   **`testMmsSize()`**
    *   Resumo: (ID: 220) Verifica se o tamanho máximo para mensagens MMS está configurado corretamente de acordo com a operadora.
*   **`testSkipNumber()`**
    *   Resumo: (ID: 942) Testa se a discagem para certos números de emergência ou serviço não é registrada no histórico de chamadas.
*   **`testContactSavePosition()`**
    *   Resumo: (IDs: 690, 150) Valida o local padrão para salvar novos contatos (SIM, telefone ou "perguntar sempre"), conforme a operadora.
*   **`testNewRingtoneForDor()`**
    *   Resumo: (ID: 900) Para a operadora DOR, verifica se o toque padrão é o "ALTICE RINGTONE V7".
*   **`testNewRingtoneForPsnAndPnt()`**
    *   Resumo: (ID: 740) Para as operadoras PSN e PNT, confere se o toque padrão é "Personal".
*   **`testContactEntelSupportWhatsApp()`**
    *   Resumo: (ID: 691) Garante que o contato "Entel Whatsapp" vem pré-instalado na agenda.
*   **`testContactColombiaSupportWhatsApp()`**
    *   Resumo: (ID: 692) Garante que o contato "WhatsApp Movistar Colombia" vem pré-instalado na agenda.
*   **`testSdn()`**
    *   Resumo: Testa o acesso aos Números de Discagem de Serviço (SDN) do cartão SIM.
*   **`testCobAndSamNetworkIcon()`**
    *   Resumo: (IDs: 919, 924) Valida a configuração do ícone de rede para as operadoras COB e SAM, que devem usar a marca "TEF".
*   **`testEnableIPV4IPV6ConnectionProfile()`**
    *   Resumo: (ID: 918) Verifica se o perfil de conexão de internet está configurado para usar "IPV4V6" (Dual Stack).
*   **`testEap()`**
    *   Resumo: (ID: 50) Testa a configuração de redes Wi-Fi com autenticação EAP-SIM, verificando se a rede da operadora é provisionada automaticamente.
*   **`testEapforCom()`**
    *   Resumo: (ID: 830) Garante que, para certas operadoras, nenhuma rede EAP-SIM é configurada por padrão.
*   **`testKeyboardSwipe()`**
    *   Resumo: (ID: 300) Para a operadora ZVV, verifica se a função de escrita por deslize (Swype/trace) no teclado Samsung está ativada por padrão.
*   **`testHidePinterest()`**
    *   Resumo: (ID: 180) Verifica se o aplicativo Pinterest foi removido da ROM para operadoras específicas.
*   **`testHideEvernote()`**
    *   Resumo: (ID: 170) Verifica se o aplicativo Evernote foi removido da ROM para operadoras específicas.
*   **`testChileTimeZone()`**
    *   Resumo: (ID: 640) Testa se o fuso horário do Chile ("America/Santiago") e suas regras de horário de verão estão funcionando corretamente.
*   **`testCheckIfPcwPaNetworkAreRemoved()`**
    *   Resumo: (ID: 915) Valida se as informações de rede da operadora "PCW_PA" são removidas da ROM da operadora "TTT".
*   **`testCheckDuplicatedNetworkInfo()`**
    *   Resumo: (IDs: 916, 933) Procura por informações de APN ou rede duplicadas que podem ocorrer em customizações de OMC.
*   **`testDisabledCallerId()`**
    *   Resumo: (ID: 391) Verifica se a edição do ID de Chamada (Caller ID) está desabilitada.
*   **`testFmsDisableMms()`**
    *   Resumo: (ID: 780) Testa se a funcionalidade de MMS está completamente desativada para certas operadoras.
*   **`testSlaveMode()`**
    *   Resumo: (ID: 251) Em aparelhos Dual SIM, verifica se o slot secundário (slave) suporta "WCDMA/GSM" em vez de apenas "GSM".
*   **`testCheckIfDeviceSupportedNfc()`**
    *   Resumo: (ID: 2) Confirma se o hardware do dispositivo relata ter suporte a NFC.
*   **`testValidateApns()`**
    *   Resumo: (ID: 610) Valida a integridade geral das configurações de APN, checando se os perfis de navegador e MMS estão corretamente definidos e sem duplicatas.
*   **`testNetworkLock()`**
    *   Resumo: (ID: 550) Testa as configurações de bloqueio de rede (Network Lock), garantindo que a inserção manual do código NCK esteja desativada.
*   **`testIusDefaultCsc()`**
    *   Resumo: (ID: 901) Verifica se o código de CSC padrão para dispositivos sem SIM é "IUS" e se o tipo de autoconfiguração está correto.
*   **`testGsmOnlyOptionNotAvailable()`**
    *   Resumo: (IDs: 520, 510) Garante que a opção de rede "Apenas GSM" não está disponível no menu de seleção de rede para certas operadoras.
*   **`testClientIdForTimCarrier()`**
    *   Resumo: (ID: 955) Valida os valores específicos do ClientID do Google (GMS) para a operadora TIM Brasil.
*   **`testClientIdForIUSCarrier()`**
    *   Resumo: (ID: 631) Valida os valores específicos do ClientID do Google para a operadora AT&T México (IUS), cuja lógica depende da data de lançamento do modelo.
*   **`testAmxNetworkIcon()`**
    *   Resumo: (IDs: 935, 936) Verifica a customização do ícone de rede para operadoras do grupo América Móvil (AMX).
*   **`testClearCodes()`**
    *   Resumo: (IDs: 500, 501) Testa as mensagens de erro de chamada (Clear Codes) para garantir que as traduções customizadas da operadora apareçam (ex: "Usuário Ocupado").
*   **`testCmasForPeru()`**
    *   Resumo: (ID: 215) Valida as configurações de CMAS (alerta de emergência) para o Peru, incluindo as strings de texto exibidas.
*   **`testCmasForChile()`**
    *   Resumo: (ID: 214) Valida as configurações de CMAS para o Chile, conferindo se os canais corretos estão ativos.
*   **`testCmasForPuertoRico()`**
    *   Resumo: (ID: 214) Valida a configuração do operador CMAS para Porto Rico.
*   **`testCmasForTfg()`**
    *   Resumo: (ID: 211) Valida as configurações de CMAS para a operadora TFG.
*   **`testDataRoamingMessageRequirement()`**
    *   Resumo: (ID: 560) Testa a exibição de uma mensagem de aviso customizada ao ativar o roaming de dados para a operadora ZTA.
*   **`testAmxWeatherWidget()`**
    *   Resumo: (ID: 185) Garante que o link web no widget de Clima está desativado para operadoras AMX.
*   **`testLteSupport()`**
    *   Resumo: (ID: 256) Verifica se o dispositivo é corretamente detectado como tendo suporte a LTE.
*   **`testChileNetworkIcon()`**
    *   Resumo: (ID: 947) Garante que não há um ícone de rede com marca da operadora para as operadoras do Chile.
*   **`testDisplayRatInfoInManualNetSearchList()`**
    *   Resumo: (ID: 530) Testa se o tipo de tecnologia de rede (2G, 3G, LTE) é exibido ao lado do nome da operadora na lista de busca manual de redes.
*   **`test4gNetworkIconOnHSPAP()`**
    *   Resumo: (ID: 109) Verifica se o ícone de rede exibe "4G" (ou similar) quando conectado em redes HSPA+, dependendo da operadora.
*   **`testVoicemailMultiSim()`**
    *   Resumo: (ID: 125) Para aparelhos Dual SIM, confirma se o sistema está configurado para diferenciar as notificações de caixa postal de cada SIM.
*   **`testEmailSignature()`**
    *   Resumo: (ID: 913) Testa a assinatura de e-mail padrão, que deve ser removida para a operadora UFN.
*   **`testDataRoamingForJdi()`**
    *   Resumo: (ID: 540) Garante que o pop-up de notificação de roaming de dados é removido para a operadora JDI.
*   **`testDefaultKeyboard()`**
    *   Resumo: (ID: 370) Verifica se os idiomas de teclado padrão (Espanhol, Inglês, Coreano) estão presentes.
*   **`testWifi()`**
    *   Resumo: (ID: 4) Um teste básico para ligar o Wi-Fi e verificar se ele consegue encontrar redes disponíveis.
*   **`testSlotCount()`**
    *   Resumo: (ID: 965) Verifica se o sufixo "/DS" (Dual SIM) é adicionado ao nome do modelo nas informações do dispositivo.
*   **`testRemoveCallerID()`**
    *   Resumo: (ID: 956) Garante que o menu de configuração de ID de Chamada é removido para a operadora TCE.
*   **`testPowerOnMustBeSilent()`**
    *   Resumo: (ID: 720) Confere se o som de inicialização (`PowerOn.ogg`) foi removido para operadoras que exigem um boot silencioso.
*   **`testNetworkIconFeature()`**
    *   Resumo: (IDs: 927, 925) Testa a exibição de ícones de rede avançados como "4.5G" quando em áreas de agregação de portadoras (Carrier Aggregation).
*   **`testTelefonicaNetworkIcon()`**
    *   Resumo: (ID: 931) Valida a complexa lógica de ícones de rede para operadoras do grupo Telefónica.
*   **`testTss()`**
    *   Resumo: Um teste fundamental que verifica se a customização via TSS (TSS SIM-subscriber Service) foi ativada corretamente no dispositivo.
*   **`testClientIdForTss()`**
    *   Resumo: Testa a configuração do ClientID do Google em um ambiente TSS.
*   **`testUssdFeature()`**
    *   Resumo: (ID: 951) Verifica se o dispositivo emite um som e vibra ao receber uma mensagem de alerta USSD.
*   **`testHdIconForAmrWbInCallUi()`**
    *   Resumo: (ID: 650) Testa a exibição do ícone "HD" na tela de chamada quando a chamada está usando o codec de áudio AMR-WB (Wideband).
*   **`testNewSiticAppPermission()`**
    *   Resumo: (ID: 949) Semelhante ao `testComPermission`, mas para um pacote SITIC diferente (`cv.sitic.tg`) em modelos específicos.
*   **`testSecuredPowerOff()`**
    *   Resumo: (ID: 960) Verifica se a função "Desligamento Seguro" (que exige senha para desligar o aparelho) está ativa.
*   **`testManufactureString()`**
    *   Resumo: (ID: 970) Testa a exibição da string de fabricante na tela de "e-Label" (etiqueta eletrônica), com lógica para modelos JDM.
*   **`testMatches()`**
    *   Resumo: Um teste genérico que lê um arquivo `matches.json` e compara uma lista de tags CSC/Carrier com os valores reais no dispositivo, atuando como um validador em massa.
*   **`testRemoveEAPforTSS()`**
    *   Resumo: (ID: 9) Garante que a configuração de EAP-SIM é removida para modelos TSS.
*   **`testRemoveSPCforTSS()`**
    *   Resumo: (ID: 12) Garante que a configuração de Código de Provedor de Serviço é removida para modelos TSS.
*   **`testRemoveKeyboardSwipeForTSS()`**
    *   Resumo: (ID: 13) Garante que a escrita por deslize no teclado é desativada para modelos TSS.
*   **`testRemoveSpeedDialForTSS()`**
    *   Resumo: (ID: 14) Garante que os números de discagem rápida são removidos para modelos TSS.
*   **`testRemoveLteOnlyMenuForTSS()`**
    *   Resumo: (ID: 17) Garante que o menu "Apenas LTE" é removido para modelos TSS.
*   **`testRemoveNfcIconForTSS()`**
    *   Resumo: (ID: 18) Garante que o ícone do NFC na barra de status é removido para modelos TSS.
*   **`testRemoveBookmarkFolderNameForTSS()`**
    *   Resumo: (ID: 20) Garante que o nome da pasta de favoritos padrão é removido do Chrome para modelos TSS.
*   **`testRemoveSoundsForTSS()`**
    *   Resumo: (ID: 21) Garante que os sons customizados da operadora são removidos para modelos TSS do Brasil.
*   **`testRemoveResourcesForTSS()`**
    *   Resumo: (ID: 21) Semelhante ao anterior, valida a remoção dos recursos de som para modelos TSS.
*   **`testRemoveSearchEngineForTSS()`**
    *   Resumo: (ID: 22) Garante que todos os motores de busca, exceto o Google, são desativados no navegador para modelos TSS.
*   **`testOmcResourceForTSS()`**
    *   Resumo: (ID: 23) Para a Argentina, verifica se os sons customizados estão configurados para serem baixados via OMC em vez de pré-instalados.
*   **`testRemoveT9PredictiveTextForTSS()`**
    *   Resumo: (ID: 24) Garante que a entrada de texto preditiva (T9) está desativada por padrão para modelos TSS da Argentina.
*   **`testRemoveMobileHotspotForTSS()`**
    *   Resumo: (ID: 25) Garante que a customização de nome/senha do Hotspot Móvel é removida para modelos TSS.
*   **`testRemoveContactSavePositionForTSS()`**
    *   Resumo: (ID: 26) Garante que a configuração de local padrão para salvar contatos é removida para modelos TSS.
*   **`testChromeCustomization()`**
    *   Resumo: (ID: 1) Verifica se o pacote de customizações do Chrome (`com.sec.android.app.chromecustomizations`) está instalado.
*   **`testClientId()`**
    *   Resumo: (ID: 8) Um teste abrangente que valida o ClientID do Google com base em uma matriz complexa que envolve o modelo do dispositivo, data de lançamento e operadora.
*   **`testNetworkModePsnNumeric()`**
    *   Resumo: (ID: 461) Garante que o menu de modo de rede exibe as opções de forma "numérica" (`4G/3G/2G`) para a operadora PSN.
*   **`testNoBroadcastChannelMenuForChile()`**
    *   Resumo: (ID: 217) Para o Chile (a partir do Android 11), verifica se o menu "Canais de Transmissão" foi removido do aplicativo de Mensagens.
*   **`testSomething()`**
    *   Resumo: Este parece ser um teste de depuração (`Log.d`, `Log.e`) para inspecionar o mapeamento de perfis de conexão e não um teste de requisito funcional.
