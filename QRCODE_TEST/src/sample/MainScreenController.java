package sample;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;
import org.codehaus.jackson.map.ObjectMapper;

import static java.nio.charset.Charset.defaultCharset;
import static sample.Huffman.HuffmanCode.*;
import static sample.LZW.compress;
import static sample.LZW.decompress;
import static sample.StringUtil.ArrayToStringConverter;
import static sample.StringUtil.StringToArrayConverter;


public class MainScreenController implements Initializable{

    private String diretorio;
    private static final String DIR = "QRDir";

    @FXML
    private ImageView im_qrcode;

    @FXML
    private TextField titulo;
    @FXML
    private TextArea descricao;
    @FXML
    private TextArea detalhes;
    @FXML
    private TextArea textoNaoFormatado;
    @FXML
    private Pane pane;
    @FXML
    private Label status;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        diretorio = new File("").getAbsolutePath();
        diretorio += File.separator + DIR;
        File file = new File(diretorio);
        if (!file.isDirectory()) {
            file.mkdir();
        }
    }



    public String readQRCode(String filePath,String charset, Map hintMap)
            throws FileNotFoundException, IOException, NotFoundException {
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                new BufferedImageLuminanceSource(
                        ImageIO.read(new FileInputStream(filePath)))));
        //Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, (Hashtable) hintMap);
        Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, (Hashtable) hintMap);//O problema está aqui

        System.out.println(qrCodeResult.getText());
        return qrCodeResult.getText();
    }



    //JSON

    @FXML
    private void abrirQRcode(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(diretorio));

        FileChooser.ExtensionFilter extFilterpng
                = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
        FileChooser.ExtensionFilter extFilterJPG
                = new FileChooser.ExtensionFilter("JPG files (*.JPG)", "*.JPG");
        FileChooser.ExtensionFilter extFilterjpg
                = new FileChooser.ExtensionFilter("jpg files (*.jpg)", "*.jpg");
        FileChooser.ExtensionFilter extFilterPNG
                = new FileChooser.ExtensionFilter("PNG files (*.PNG)", "*.PNG");

        fileChooser.getExtensionFilters()
                .addAll(extFilterpng, extFilterJPG, extFilterjpg, extFilterPNG);

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                Image image = new Image(new FileInputStream(file.getAbsolutePath()));
                im_qrcode.setImage(image);

                String charset = "UTF-8"; // or  "ISO-8859-1"  "UTF-8"ISO-8859-1
                //Map hintMap = new HashMap();
                Map hintMap = new Hashtable();

                hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

                String readQRCodeA = readQRCode(file.getAbsolutePath(), charset, hintMap);//Não reconhece os caracteres




                //teste lzw
                String a = decompress(readQRCodeA);//Não funciona porque readQrCode nao dá


                System.out.println("Compactada" + readQRCodeA);
                System.out.println("\nDescompactada" + a);

                //textoNaoFormatado.setText(readQRCode); sem lzw
                textoNaoFormatado.setText(a);

                //Cria o objectMapper
                ObjectMapper om = new ObjectMapper();

                Conteudo c = new Conteudo();

                //Lê o Json, e cria um novo Conteudo com os dados que recebe
                //c = om.readValue(readQRCodeA, Conteudo.class); //sem lzw
                c = om.readValue(a, Conteudo.class);

                titulo.setText(c.getTitulo());
                descricao.setText(c.getDescricao());
                detalhes.setText(c.getDetalhes());


            } catch (IOException ex) {
                Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void gerarQRcode(ActionEvent event) throws IOException {
        String nome = titulo.getText();

        //
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar ficheiro qr");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image", "*.png"));
        String diretorio2 = String.valueOf(chooser.showSaveDialog(pane.getScene().getWindow()));
        //

        if (!nome.isEmpty()) {
            try {
                Conteudo conteudo = new Conteudo();
                conteudo.setTitulo(titulo.getText());
                conteudo.setDescricao(descricao.getText());
                conteudo.setDetalhes(detalhes.getText());

                String conteudo_qrcode = conteudo.toJson();//Cria o json

                //LZW
                String conteudo_lzw = compress(conteudo_qrcode);


                System.out.println("Descomprimido: " + conteudo_qrcode);
                System.out.println("\nComprimido: " + conteudo_lzw);

                FileOutputStream fout = new FileOutputStream(diretorio2 );//+ ".f.png"
//
                //FileOutputStream fout = new FileOutputStream(diretorio2 + File.separator + nome + ".png");
                //FileOutputStream fout = new FileOutputStream(chooser.showSaveDialog(pane.getScene().getWindow()));
                //ByteArrayOutputStream bos = QRCode.from(conteudo_qrcode).withSize(200, 200).to(ImageType.PNG).stream();//sem lzw
                ByteArrayOutputStream bos = QRCode.from(conteudo_lzw).withSize(200, 200).to(ImageType.PNG).stream();

                fout.write(bos.toByteArray());

                bos.close();
                fout.close();
                fout.flush();
                Image image = new Image(new FileInputStream(diretorio2));//+ ".f.png"

                im_qrcode.setImage(image);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    private void limpar(ActionEvent event) {
        titulo.setText("");
        descricao.setText("");
        detalhes.setText("");

        textoNaoFormatado.setText("");

        im_qrcode.setImage(null);
    }

    @FXML
    private void loadFile(ActionEvent event) throws IOException {
        Scanner scanner;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Abrir ficheiro");
        chooser.getExtensionFilters().addAll(
                //new FileChooser.ExtensionFilter("Txt", "*.txt")
                new FileChooser.ExtensionFilter("Formatted JSON", "*.f.json")//Apenas abre ficheiros JSON
        );

        List<File> file = chooser.showOpenMultipleDialog(pane.getScene().getWindow());

        if(file != null) {
            for(int i=0; i<file.size(); i++) {

                System.out.println(file.get(i));
                String a = readFile(String.valueOf(file.get(i)), defaultCharset());
                //Descompressão do lzw
                String b = decompress(a);
                System.out.println(b);

                //Cria o objectMapper
                ObjectMapper om = new ObjectMapper();


                Conteudo c = new Conteudo();

                //Lê o Json, e cria um novo Conteudo com os dados que recebe
                //c = om.readValue(a, Conteudo.class); //Sem lzw
                c = om.readValue(b, Conteudo.class);//String descomprimida


                titulo.setText(c.getTitulo());
                descricao.setText(c.getDescricao());
                detalhes.setText(c.getDetalhes());

            }

        } else {
            System.out.println("Cancelado");
        }
    }

    @FXML
    private void saveFile(ActionEvent event){
        String nome = titulo.getText();

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar ficheiro");
        //chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Formatted Text", "*.f.txt"));
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Formatted JSON", "*.f.json"));//Grava o json com a estrutura prevista


        if (!nome.isEmpty()) {
            try {
                Conteudo conteudo = new Conteudo();
                conteudo.setTitulo(titulo.getText());
                conteudo.setDescricao(descricao.getText());
                conteudo.setDetalhes(detalhes.getText());

                String conteudo_texto = conteudo.toJson();//Cria o json

                //Tentativa de compactação!!!!!!!!!!!!
                String conteudo_lzw = compress(conteudo_texto);

                //String fileName = titulo.getText() + ".txt";

                File file = chooser.showSaveDialog(pane.getScene().getWindow());

                if(!file.exists()){
                    file.createNewFile();
                    FileWriter fw = new FileWriter(file.getAbsoluteFile()); //+ ".f.txt"
                    BufferedWriter bw = new BufferedWriter(fw);
                    //bw.write(conteudo_texto); //Sem lzw
                    bw.write(conteudo_lzw);
                    bw.close();
                    status.setText("Gravado com sucesso");
                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //Texto não formatado


    @FXML
    private void abrirQRcode2(ActionEvent event) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(diretorio));

        FileChooser.ExtensionFilter extFilterpng
                = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
        FileChooser.ExtensionFilter extFilterJPG
                = new FileChooser.ExtensionFilter("JPG files (*.JPG)", "*.JPG");
        FileChooser.ExtensionFilter extFilterjpg
                = new FileChooser.ExtensionFilter("jpg files (*.jpg)", "*.jpg");
        FileChooser.ExtensionFilter extFilterPNG
                = new FileChooser.ExtensionFilter("PNG files (*.PNG)", "*.PNG");

        fileChooser.getExtensionFilters()
                .addAll(extFilterpng, extFilterJPG, extFilterjpg, extFilterPNG);

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                Image image = new Image(new FileInputStream(file.getAbsolutePath()));
                im_qrcode.setImage(image);

                String charset = "UTF-8"; // or    "ISO-8859-1" estava utf-8
                //Map hintMap = new HashMap();
                Map hintMap = new Hashtable();

                hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

                //String readQRCode = readQRCode(file.getAbsolutePath(), charset, hintMap);


                //textoNaoFormatado.setText(readQRCode);

            } catch (IOException ex) {
                Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


    @FXML
    private void gerarQRcode2(ActionEvent event) throws IOException {
                //
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar ficheiro qr não formatado");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image", "*png"));
        String diretorio2 = String.valueOf(chooser.showSaveDialog(pane.getScene().getWindow()));
        //

            try {
                String conteudo_qrcode = "";
                conteudo_qrcode += textoNaoFormatado.getText();

                FileOutputStream fout = new FileOutputStream(diretorio2 + ".png");

                ByteArrayOutputStream bos = QRCode.from(conteudo_qrcode).withSize(250, 250).to(ImageType.PNG).stream();

                fout.write(bos.toByteArray());

                bos.close();
                fout.close();
                fout.flush();
                Image image = new Image(new FileInputStream(diretorio2 + ".png"));

                im_qrcode.setImage(image);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }

    }



    @FXML
    private void loadFile2(ActionEvent event) throws IOException {
        Scanner scanner;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Abrir ficheiro não formatado");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Txt", "*.txt")
        );

        List<File> file = chooser.showOpenMultipleDialog(pane.getScene().getWindow());

        if(file != null) {
            for(int i=0; i<file.size(); i++) {

                System.out.println(file.get(i));
                String a = readFile(String.valueOf(file.get(i)), defaultCharset());

                textoNaoFormatado.setText(a);

                }

        } else {
            System.out.println("Cancelado");
        }

    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }


    @FXML
    private void saveFile2(ActionEvent event){

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar ficheiro não formatado");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text", "*txt"));

            try {
                String conteudo_texto = "";
                conteudo_texto += textoNaoFormatado.getText();

                File file = chooser.showSaveDialog(pane.getScene().getWindow());

                if(!file.exists()){
                    file.createNewFile();
                    FileWriter fw = new FileWriter(file.getAbsoluteFile() + ".txt");
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(conteudo_texto);
                    bw.close();
                    status.setText("Gravado com sucesso");
                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException e) {
                e.printStackTrace();
            }

    }


    //CRIA O QRCODE CODIFICADO
    @FXML
    private void huffTest(ActionEvent event) throws IOException{

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar ficheiro qr não formatado");
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image", "*png"));
        String diretorio2 = String.valueOf(chooser.showSaveDialog(pane.getScene().getWindow()));
        //

        try {
            String textoNaoCodificado = "";
            textoNaoCodificado += textoNaoFormatado.getText();//Recebe o texto inserido no campo - NÃO FORMATADO

            int[] charset = charsetReturner(textoNaoCodificado);//charset para construir a tree

            //Strings a ser colocadas no QR
            String charsetString = ArrayToStringConverter(charset);//Charset em String
            String textoCodificado = encodedReturner(textoNaoCodificado, charset);//Conteudo_qrcode codificado

            String conteudo_qrcode = "";
            conteudo_qrcode += "charset: " + charsetString + "\n";
            conteudo_qrcode += "Texto: " + textoCodificado + "\n";


            //QR
            FileOutputStream fout = new FileOutputStream(diretorio2 + ".png");

            ByteArrayOutputStream bos = QRCode.from(conteudo_qrcode).withSize(250, 250).to(ImageType.PNG).stream();

            fout.write(bos.toByteArray());

            bos.close();
            fout.close();
            fout.flush();
            Image image = new Image(new FileInputStream(diretorio2 + ".png"));

            im_qrcode.setImage(image);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private void abrirQRcode3(ActionEvent event) {

        String charset1, texto1;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(diretorio));

        FileChooser.ExtensionFilter extFilterpng
                = new FileChooser.ExtensionFilter("png files (*.png)", "*.png");
        FileChooser.ExtensionFilter extFilterJPG
                = new FileChooser.ExtensionFilter("JPG files (*.JPG)", "*.JPG");
        FileChooser.ExtensionFilter extFilterjpg
                = new FileChooser.ExtensionFilter("jpg files (*.jpg)", "*.jpg");
        FileChooser.ExtensionFilter extFilterPNG
                = new FileChooser.ExtensionFilter("PNG files (*.PNG)", "*.PNG");


        fileChooser.getExtensionFilters()
                .addAll(extFilterpng, extFilterPNG, extFilterjpg, extFilterJPG);

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                Image image = new Image(new FileInputStream(file.getAbsolutePath()));
                im_qrcode.setImage(image);

                String charset = "UTF-8"; // or    "ISO-8859-1"
                //Map hintMap = new HashMap();
                Map hintMap = new Hashtable();

                hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

                String readQRCode = readQRCode(file.getAbsolutePath(), charset, hintMap);





                charset1 = readQRCode.substring(readQRCode.indexOf("charset:") + 9, readQRCode.indexOf("Texto:"));
                texto1 = readQRCode.substring(readQRCode.indexOf("Texto:") + 7);

                int[] charset2 = StringToArrayConverter(charset1);//Array para enviar para descodificar
                String texto2 = decodedReturner(texto1, charset2);//Não funcionou???


                System.out.println("Charset1: " + charset1 + "\n");
                System.out.println("Charset2: " + charset2 + "\n");
                System.out.println("Texto1: " + texto1 + "\n");
                System.out.println("Texto2: " + texto2 + "\n");
                System.out.println("ReadQrCode: " + readQRCode + "\n");


                //Inserir texto nos llocais apropriados
                textoNaoFormatado.setText(texto2);

            } catch (IOException | NotFoundException ex) {
                Logger.getLogger(MainScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }



}
