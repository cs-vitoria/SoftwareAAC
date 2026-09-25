package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ComfyUIImageService {

    private final String comfyUrl;
    private final String workflowPath;
    private final String nodePromptPositivoId;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public ComfyUIImageService(String comfyUrl, String workflowPath, String nodePromptPositivoId) {
        this.comfyUrl = comfyUrl;
        this.workflowPath = workflowPath;
        this.nodePromptPositivoId = nodePromptPositivoId;
        this.httpClient = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
    }

    /**
     * Envia o prompt em inglês para o ComfyUI e faz o download da imagem gerada no caminho especificado.
     */
    public void gerareSalvarImagem(String promptIngles, Path caminhoDestino) throws Exception {
        String payloadJson = prepararPayloadComfy(promptIngles);
        String respostaComfy = enviarParaComfy(payloadJson);

        JsonNode respostaJson = mapper.readTree(respostaComfy);
        String promptId = respostaJson.get("prompt_id").asText();

        String[] imgInfo = obterInfoImagemComfy(promptId);
        baixarImagemComfy(imgInfo[0], imgInfo[1], imgInfo[2], caminhoDestino);
    }

    private String prepararPayloadComfy(String novoPrompt) throws Exception {
        JsonNode rootNode = mapper.readTree(new File(workflowPath));

        ObjectNode inputsDoNo = (ObjectNode) rootNode.path(nodePromptPositivoId).path("inputs");
        inputsDoNo.put("text", novoPrompt);

        ObjectNode payloadFinal = mapper.createObjectNode();
        payloadFinal.set("prompt", rootNode);

        return mapper.writeValueAsString(payloadFinal);
    }

    private String enviarParaComfy(String jsonPayload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(comfyUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }

    private String[] obterInfoImagemComfy(String promptId) throws Exception {
        String historyUrl = "http://127.0.0.1:8188/history/" + promptId;

        while (true) {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(historyUrl)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = mapper.readTree(response.body());
                if (root.has(promptId)) {
                    JsonNode saveImageOutput = root.path(promptId).path("outputs").path("27").path("images");
                    if (saveImageOutput.isArray() && saveImageOutput.size() > 0) {
                        JsonNode imgNode = saveImageOutput.get(0);
                        return new String[]{
                                imgNode.get("filename").asText(),
                                imgNode.get("subfolder").asText(),
                                imgNode.get("type").asText()
                        };
                    }
                }
            }
            Thread.sleep(1000);
        }
    }

    private void baixarImagemComfy(String filename, String subfolder, String type, Path caminhoDestino) throws Exception {
        String viewUrl = String.format("http://127.0.0.1:8188/view?filename=%s&subfolder=%s&type=%s",
                URLEncoder.encode(filename, StandardCharsets.UTF_8),
                URLEncoder.encode(subfolder, StandardCharsets.UTF_8),
                URLEncoder.encode(type, StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(viewUrl)).GET().build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            try (InputStream is = response.body()) {
                Files.copy(is, caminhoDestino, StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            throw new RuntimeException("Erro ao baixar imagem do ComfyUI: HTTP " + response.statusCode());
        }
    }
}