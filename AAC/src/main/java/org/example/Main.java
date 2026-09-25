package org.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    private static final String OLLAMA_HOST = "http://localhost:11434";
    private static final String MODEL_OLLAMA = "hf.co/tardellirs/aac-board-generator-770m-ptbr-GGUF:Q4_K_M";

    private static final String COMFY_URL = "http://127.0.0.1:8188/prompt";
    private static final String CAMINHO_JSON_WORKFLOW = "workflow_api.json";
    private static final String ID_NO_PROMPT_POSITIVO = "6";
    private static final String PASTA_DOWNLOAD = "pictogramas";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Inicializa os serviços refatorados
            AACBoardGenerator boardGenerator = new AACBoardGenerator(OLLAMA_HOST, MODEL_OLLAMA);
            ComfyUIImageService comfyService = new ComfyUIImageService(COMFY_URL, CAMINHO_JSON_WORKFLOW, ID_NO_PROMPT_POSITIVO);

            // Entrada do usuário
            System.out.print("Digite o tema/pedido para a prancha de AAC: ");
            String pedido = scanner.nextLine().trim();

            if (pedido.isEmpty()) {
                System.out.println("Nenhum pedido foi digitado. Encerrando.");
                return;
            }

            // ------------------------------------------------------------------------
            // ETAPA 1: Geração da Prancha de CAA com Ollama
            // ------------------------------------------------------------------------
            System.out.println("\n=== 1. Gerando Prancha de AAC com Ollama ===");
            String pranchaResultado = boardGenerator.gerarPrancha(pedido);
            System.out.println("Saída do Modelo:\n" + pranchaResultado);

            // ------------------------------------------------------------------------
            // ETAPA 2: Tradução e Geração Individual no ComfyUI
            // ------------------------------------------------------------------------
            System.out.println("\n=== 2. Gerando Pictogramas Traduzidos no ComfyUI ===");
            Files.createDirectories(Paths.get(PASTA_DOWNLOAD));

            String[] linhas = pranchaResultado.split("\n");

            for (String linha : linhas) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;

                String[] partes = linha.split("\\|");
                if (partes.length < 1) continue;

                String palavraPt = partes[0].replaceAll("^[\\*\\-\\s]+", "").trim();
                if (palavraPt.isEmpty()) continue;

                // Chama método de tradução da classe AACBoardGenerator
                String palavraEn = boardGenerator.traduzirParaIngles(palavraPt);

                String nomeArquivo = palavraPt.toLowerCase().replaceAll("[^a-z0-9]", "_") + ".png";
                Path caminhoSalvar = Paths.get(PASTA_DOWNLOAD, nomeArquivo);

                String promptComfy = "A clean AAC pictogram illustration of " + palavraEn + ", simple vector style, white background, isolated, high quality pictogram, flat colors";

                System.out.println("Palavra (PT): " + palavraPt + " -> Tradução (EN): " + palavraEn);
                System.out.println("  -> Prompt enviado: \"" + promptComfy + "\"");

                try {
                    // Chama serviço do ComfyUI para gerar e baixar a imagem
                    comfyService.gerareSalvarImagem(promptComfy, caminhoSalvar);
                    System.out.println("  [✓] Salvo em: " + caminhoSalvar.toAbsolutePath());
                } catch (Exception e) {
                    System.err.println("  [X] Erro ao gerar imagem para '" + palavraPt + "': " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Erro na execução principal: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}