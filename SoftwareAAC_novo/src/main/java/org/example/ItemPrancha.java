package org.example;

import java.nio.file.Path;

public class ItemPrancha {
    private final String palavra;
    private final String tipo;   // s, v, a, e, l, p
    private Path imagem;         // caminho do arquivo PNG (começa vazio)

    public ItemPrancha(String palavra, String tipo) {
        this.palavra = palavra;
        this.tipo = tipo;
    }

    public String getPalavra() { return palavra; }
    public String getTipo() { return tipo; }
    public Path getImagem() { return imagem; }
    public void setImagem(Path imagem) { this.imagem = imagem; }
}