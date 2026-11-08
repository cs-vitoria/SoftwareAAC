package org.example;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            AACBoardGenerator gerador = new AACBoardGenerator(
                    "http://localhost:11434",
                    "hf.co/tardellirs/aac-board-generator-770m-ptbr-GGUF:Q4_K_M");

            ComfyUIImageService comfy = new ComfyUIImageService(
                    "http://127.0.0.1:8188/prompt", "workflow_api.json", "6");

            PranchaService servico = new PranchaService(gerador, comfy);

            System.out.print("Digite o tema: ");
            String pedido = scanner.nextLine().trim();

            List<ItemPrancha> itens = servico.gerarItens(pedido);
            for (ItemPrancha item : itens) {
                System.out.println("Gerando: " + item.getPalavra());
                servico.gerarImagem(item);
                System.out.println("  Salvo em: " + item.getImagem());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}