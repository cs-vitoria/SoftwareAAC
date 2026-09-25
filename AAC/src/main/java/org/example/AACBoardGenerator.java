package org.example;

import io.github.ollama4j.Ollama;
import io.github.ollama4j.models.generate.OllamaGenerateRequest;
import io.github.ollama4j.models.response.OllamaResult;
import io.github.ollama4j.utils.OptionsBuilder;

public class AACBoardGenerator {

    private final Ollama ollama;
    private final String model;

    public AACBoardGenerator(String host, String model) {
        this.ollama = new Ollama(host);
        this.ollama.setRequestTimeoutSeconds(300);
        this.model = model;
    }

    /**
     * Solicita ao modelo a geração da prancha de CAA com base no pedido do usuário.
     */
    public String gerarPrancha(String pedido) throws Exception {
        String instr = "Você é um especialista em Comunicação Alternativa e Aumentativa (CAA/AAC) em pt-BR.\n"
                + "Sua tarefa é gerar uma lista de EXATAMENTE 5 itens essenciais e variados para o PEDIDO.\n\n"
                + "REGRAS ESTRITAS:\n"
                + "1. Retorne EXATAMENTE 5 linhas (5 itens no total).\n"
                + "2. O campo 'tipo' deve ser APENAS uma letra referente à categoria:\n"
                + "   - s = substantivo (ex: palhaço, pipoca)\n"
                + "   - v = verbo/ação (ex: sorrir, assistir)\n"
                + "   - a = adjetivo/qualidade (ex: divertido, grande)\n"
                + "   - e = expressão (ex: quero ver, me ajuda)\n"
                + "   - l = lugar (ex: circo, parque)\n"
                + "   - p = pronome (ex: eu, você)\n\n"
                + "FORMATO OBRIGATÓRIO POR LINHA (sem numeração, traços ou marcações):\n"
                + "palavra|tipo\n\n"
                + "EXEMPLO CORRETO PARA 'CIRCO':\n"
                + "palhaço|s\n"
                + "assistir|v\n"
                + "divertido|a\n"
                + "circo|l\n"
                + "eu quero|e\n\n"
                + "Responda APENAS as 5 linhas da lista:";

        String prompt = "<start_of_turn>user\n" + instr + "\n\nPEDIDO: " + pedido + "<end_of_turn>\n<start_of_turn>model\n";

        OptionsBuilder options = new OptionsBuilder()
                .setTemperature(0.0f)
                .setNumPredict(320);

        OllamaResult textResult = ollama.generate(
                OllamaGenerateRequest.builder()
                        .withModel(this.model)
                        .withPrompt(prompt)
                        .withOptions(options.build())
                        .build(),
                null
        );

        return textResult.getResponse();
    }

    /**
     * Traduz um termo individual do Português para o Inglês.
     */
    public String traduzirParaIngles(String termoPt) {
        try {
            String promptTraducao = "<start_of_turn>user\n"
                    + "Translate this single Portuguese term to English: \"" + termoPt + "\". "
                    + "Provide ONLY the English translated word or short phrase. Do not write 'answer', do not explain, do not add prefixes.\n"
                    + "<end_of_turn>\n<start_of_turn>model\n";

            OptionsBuilder options = new OptionsBuilder()
                    .setTemperature(0.0f)
                    .setNumPredict(20);

            OllamaResult result = ollama.generate(
                    OllamaGenerateRequest.builder()
                            .withModel(this.model)
                            .withPrompt(promptTraducao)
                            .withOptions(options.build())
                            .build(),
                    null
            );

            String traduzido = result.getResponse();

            if (traduzido.contains("\n")) {
                traduzido = traduzido.split("\n")[0];
            }

            traduzido = traduzido.replaceAll("(?i)\\b(answer|word|translation|resposta):?", "")
                    .replaceAll("[^a-zA-Z\\s]", "")
                    .trim()
                    .toLowerCase();

            return traduzido.isEmpty() ? termoPt : traduzido;

        } catch (Exception e) {
            System.err.println("Falha ao traduzir '" + termoPt + "', mantendo termo original.");
            return termoPt;
        }
    }
}
