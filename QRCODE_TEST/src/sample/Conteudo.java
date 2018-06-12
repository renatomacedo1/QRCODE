package sample;
import java.util.ArrayList;

public class Conteudo {

/*    private ArrayList<String> conteudo = new ArrayList<String>();

    public Conteudo() {

    }

    public void addItem(String item){
        conteudo.add(item);
    }


    public Conteudo(ArrayList<String> conteudo) {
        this.conteudo = conteudo;
    }

    public ArrayList<String> getConteudo() {
        return conteudo;
    }

    public void setConteudo(ArrayList<String> conteudo) {
        this.conteudo = conteudo;
    }*/




    private String titulo;
    private String descricao;
    private String detalhes;

    public String toJson(){
        String json = "{\"titulo\":\""+this.titulo+"\", \"descricao\":\""+this.descricao+"\", \"detalhes\":\""+this.detalhes+"\"}";
        return json;
    }


    //Getters e Setters
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }
}
