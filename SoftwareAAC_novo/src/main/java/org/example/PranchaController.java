package org.example;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.util.List;

public class PranchaController {

    // ---------- Componentes do prancha.fxml (nomes iguais aos fx:id) ----------

    @FXML private Button btn_enviar;
    @FXML private Button btn_limpar;
    @FXML private Button btn_voltar;

    @FXML private Label frase_final;
    @FXML private TextField txt_contexto;

    @FXML private ImageView imagem1;
    @FXML private ImageView imagem2;
    @FXML private ImageView imagem3;
    @FXML private ImageView imagem4;
    @FXML private ImageView imagem5;
    @FXML private ImageView imagem6;

    @FXML private Label palavra1;
    @FXML private Label palavra2;
    @FXML private Label palavra3;
    @FXML private Label palavra4;
    @FXML private Label palavra5;
    @FXML private Label palavra6;

    // ---------- Apoio ----------

    private ImageView[] imagens;
    private Label[] palavras;
    private PranchaService servico;
    private boolean gerando = false;

    // Número da geração atual. Se o usuário clicar em "limpar" no meio de uma geração,
    // o número muda e a geração antiga para de mexer na tela.
    private volatile int geracaoAtual = 0;

    /** Roda automaticamente quando a tela é carregada. */
    @FXML
    public void initialize() {
        // Junta os componentes em listas: o bloco 0 é imagem1 + palavra1, e assim por diante
        imagens = new ImageView[]{imagem1, imagem2, imagem3, imagem4, imagem5, imagem6};
        palavras = new Label[]{palavra1, palavra2, palavra3, palavra4, palavra5, palavra6};

        frase_final.setWrapText(true);

        // Clicar na imagem (ou na palavra) manda a palavra para a "Frase final"
        for (int i = 0; i < imagens.length; i++) {
            final int posicao = i;
            imagens[i].setCursor(Cursor.HAND);
            palavras[i].setCursor(Cursor.HAND);
            imagens[i].setOnMouseClicked(e -> adicionarNaFrase(palavras[posicao].getText()));
            palavras[i].setOnMouseClicked(e -> adicionarNaFrase(palavras[posicao].getText()));
        }

        AACBoardGenerator gerador = new AACBoardGenerator(
                "http://localhost:11434",
                "hf.co/tardellirs/aac-board-generator-770m-ptbr-GGUF:Q4_K_M");
        ComfyUIImageService comfy = new ComfyUIImageService(
                "http://127.0.0.1:8188/prompt", "workflow_api.json", "6");
        servico = new PranchaService(gerador, comfy);
    }

    // ---------- Ações dos botões ----------

    /** Apertar Enter no campo de contexto faz o mesmo que clicar em "enviar". */
    @FXML
    void contexto(ActionEvent event) {
        gerarPrancha();
    }

    @FXML
    void enviar(ActionEvent event) {
        gerarPrancha();
    }

    @FXML
    void limpar(ActionEvent event) {
        boolean estavaGerando = gerando;   // guarda antes do terminar() mudar o valor

        geracaoAtual++;          // invalida qualquer geração que ainda esteja rodando
        txt_contexto.clear();
        limparBlocos();
        frase_final.setText("");
        terminar();              // libera o botão enviar

        if (estavaGerando) {
            Alert aviso = new Alert(Alert.AlertType.INFORMATION,
                    "A geração foi interrompida.\nDigite um novo contexto para gerar outra prancha.");
            aviso.setTitle("Geração interrompida");
            aviso.setHeaderText(null);
            aviso.show();
        }
    }

    @FXML
    void voltar(ActionEvent event) throws IOException {
        Launcher.mudarTela(event, "tela_inicial.fxml");
    }

    // ---------- Lógica principal ----------

    private void gerarPrancha() {
        String contexto = txt_contexto.getText().trim();
        if (contexto.isEmpty() || gerando) return;

        gerando = true;
        btn_enviar.setDisable(true);
        limparBlocos();
        final int minhaGeracao = ++geracaoAtual;

        // O trabalho demorado roda em OUTRA thread, senão a janela congelaria
        Task<Void> tarefa = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 1) Pede as palavras ao Ollama (rápido)
                List<ItemPrancha> itens = servico.gerarItens(contexto);
                int total = Math.min(itens.size(), imagens.length);

                // 2) Mostra as palavras logo, antes das imagens ficarem prontas
                Platform.runLater(() -> {
                    if (minhaGeracao != geracaoAtual) return;   // foi limpo, ignora
                    for (int i = 0; i < total; i++) {
                        palavras[i].setText(itens.get(i).getPalavra());
                    }
                });

                // 3) Gera uma imagem por vez (lento) e coloca na tela assim que fica pronta
                for (int i = 0; i < total; i++) {
                    if (minhaGeracao != geracaoAtual) break;    // foi limpo, para de gerar
                    ItemPrancha item = itens.get(i);
                    final int posicao = i;
                    try {
                        servico.gerarImagem(item);

                        // toUri().toString(): o Image espera uma URL, não um caminho de arquivo
                        Image img = new Image(item.getImagem().toUri().toString(), 292, 292, true, true);

                        // Só a thread da tela pode mexer nos componentes
                        Platform.runLater(() -> {
                            if (minhaGeracao == geracaoAtual) imagens[posicao].setImage(img);
                        });
                    } catch (Exception e) {
                        System.err.println("Erro ao gerar '" + item.getPalavra() + "': " + e.getMessage());
                    }
                }
                return null;
            }
        };

        tarefa.setOnSucceeded(e -> {
            if (minhaGeracao == geracaoAtual) terminar();
        });
        tarefa.setOnFailed(e -> {
            Throwable erro = tarefa.getException();
            erro.printStackTrace();
            if (minhaGeracao != geracaoAtual) return;   // já foi limpo, não precisa avisar
            terminar();
            new Alert(Alert.AlertType.ERROR,
                    "Não foi possível gerar a prancha.\n"
                            + "Confira se o Ollama e o ComfyUI estão abertos.\n\n"
                            + erro.getMessage()).show();
        });

        Thread thread = new Thread(tarefa);
        thread.setDaemon(true);
        thread.start();
    }

    private void terminar() {
        gerando = false;
        btn_enviar.setDisable(false);
    }

    private void limparBlocos() {
        for (int i = 0; i < imagens.length; i++) {
            imagens[i].setImage(null);
            palavras[i].setText("");
        }
    }

    private void adicionarNaFrase(String palavra) {
        if (palavra == null || palavra.isEmpty()) return;
        String atual = frase_final.getText();
        frase_final.setText(atual == null || atual.isEmpty() ? palavra : atual + " " + palavra);
    }
}