package org.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PranchaService {

    private static final String PASTA_DOWNLOAD = "pictogramas";

    private final AACBoardGenerator boardGenerator;
    private final ComfyUIImageService comfyService;

    public PranchaService(AACBoardGenerator boardGenerator, ComfyUIImageService comfyService) {
        this.boardGenerator = boardGenerator;
        this.comfyService = comfyService;
    }

    /** Pede as palavras ao Ollama e transforma o texto em uma lista de itens. */
    public List<ItemPrancha> gerarItens(String pedido) throws Exception {
        String resposta = boardGenerator.gerarPrancha(pedido);
        List<ItemPrancha> itens = new ArrayList<>();

        for (String linha : resposta.split("\n")) {
            linha = linha.trim();
            if (linha.isEmpty()) continue;

            String[] partes = linha.split("\\|");
            String palavra = partes[0].replaceAll("^[\\*\\-\\s]+", "").trim();
            String tipo = partes.length > 1 ? partes[1].trim() : "s";

            if (!palavra.isEmpty()) {
                itens.add(new ItemPrancha(palavra, tipo));
            }
        }
        return itens;
    }

    /** Gera a imagem de UM item e guarda o caminho dentro dele. */
    public void gerarImagem(ItemPrancha item) throws Exception {
        Files.createDirectories(Paths.get(PASTA_DOWNLOAD));

        String palavraEn = boardGenerator.traduzirParaIngles(item.getPalavra());
        String nomeArquivo = item.getPalavra().toLowerCase().replaceAll("[^a-z0-9]", "_") + ".png";
        Path destino = Paths.get(PASTA_DOWNLOAD, nomeArquivo);

        String prompt = "A clean AAC pictogram illustration of " + palavraEn
                + ", simple vector style, white background, isolated, high quality pictogram, flat colors";

        comfyService.gerareSalvarImagem(prompt, destino);
        item.setImagem(destino);
    }
}