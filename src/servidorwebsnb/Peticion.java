/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorwebsnb;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author J.PEREZ
 */
public class Peticion {

    private String enc;
    int bufferSize = 10204;
    private ByteBuffer bRes;

    public void Peticion(SocketChannel ch, ByteBuffer b) {

        try {

            int n = ch.read(b);
            String lectura = "";
            if (n != -1) {
                lectura = new String(b.array(), 0, b.position());
                System.out.println("Cadena: " + lectura);
            }

            //System.out.println("Se recibieron: " + b.position() + " bytes.");
            b.flip();
            System.out.println("Se recibieron: " + b.limit() + " bytes.");

            if (lectura == null) {
                String res = "<html><head></head><body>HOLA MUNDO VACIO</body></html>";
                bRes = ByteBuffer.wrap(res.getBytes());
                ch.write(bRes);
            } else {
                enc = "HTTP/1.1 200 OK \r\nServer: JESUS-JOSE/1.0 \n";
                if (lectura.startsWith("GET")) {
                    if (lectura.indexOf("?") != -1) {//Peticion GET con parametros en URL
                        String[] lines = lectura.split("\n");
                        String[] line = lines[0].split(" ");
                        String params = getParametros(line[1]);

                        String res = "<html><head></head><body>Parametros: " + params + "</body></html>";

                        
                        bRes = ByteBuffer.wrap(enc.getBytes());
                        ch.write(bRes);
                        bRes = ByteBuffer.wrap("Content-Type: text/html \n\r\n".getBytes());
                        ch.write(bRes);
                        bRes = ByteBuffer.wrap(res.getBytes());
                        ch.write(bRes);
                        System.out.println("Respuesta GET con Parametros: " + res);

                    } else {//Peticion GET sin parametros
                        String[] lines = lectura.split("\n");
                        String[] line = lines[0].split(" ");
                        String params = getParametros(line[1]);

                        String fileName = getFileName(lectura);

                        bRes = ByteBuffer.wrap(enc.getBytes());
                        ch.write(bRes);

                        if (fileName=="") {
                            sendArchivo("index.html", ch);
                        } else {
                            sendArchivo(fileName, ch);
                        }
                    }
                } else if (lectura.startsWith("POST")) {
                    String[] lines = lectura.split("\n");
                    String[] line = lines[0].split(" ");
                    String params = getParametros(line[1]);
                    
                    enc = "HTTP/1.1 200 OK \r\nServer: JESUS-JOSE/1.0 \n";
                    bRes = ByteBuffer.wrap(enc.getBytes());
                    ch.write(bRes);
                    bRes = ByteBuffer.wrap("Content-Type: text/html \n\r\n".getBytes());
                    ch.write(bRes);
                    String res = "<html><head></head><body>RESPUESTA POST</body></html>";
                    bRes = ByteBuffer.wrap(res.getBytes());
                    ch.write(bRes);
                    System.out.println("Respuesta POST: " + res);
                } else if (lectura.startsWith("HEAD")) {
                    
                    enc = "HTTP/1.1 200 OK \r\nServer: JESUS-JOSE/1.0 \n";
                    bRes = ByteBuffer.wrap(enc.getBytes());
                    ch.write(bRes);
                    bRes = ByteBuffer.wrap("Content-Type: text/html \n\r\n".getBytes());
                    ch.write(bRes);
                    String res = "<html><head></head><body>RESPUESTA HEAD</body></html>";
                    bRes = ByteBuffer.wrap(res.getBytes());
                    ch.write(bRes);
                    System.out.println("Respuesta HEAD: " + res);

                } else if (lectura.startsWith("DELETE")) {
                    String[] lines = lectura.split("\n");
                    String[] line = lines[0].split(" ");
                    String params = getParametros(line[1]);
                    String fileName = getFileName(lectura);

                    bRes = ByteBuffer.wrap(enc.getBytes());
                    ch.write(bRes);

                    if (fileName.compareTo("") == 0) {
                        sendArchivo("index.htm", ch);
                    } else {
                        sendArchivo(fileName, ch);
                        File f = new File(fileName);
                        f.delete();
                    }
                    System.out.println("Archivo Eliminado: " + fileName);
                } else {
                    String res = "HTTP/1.1 501 No Implementado \r\nServer: JESUS-JOSE/1.0 \nContent-Type: text/html \nDate: " + new Date() + " \n\r\n";
                    bRes = ByteBuffer.wrap(res.getBytes());
                    ch.write(bRes);
                    sendArchivo("error.html", ch);
                    System.out.println("Tipo de peticion no implementada");
                }
            }
        } catch (IOException ex) {
        }

    }

    public String getParametros(String c) {
        //Hay que formatear los parametros

        return c;
    }

    public String getFileName(String line) {
        int i;
        int f;
        i = line.indexOf("/");
        f = line.indexOf(" ", i);
        String FileName = line.substring(i + 1, f);
        System.out.println("Nombre Archivo: " + FileName);
        if((i+1)==f){
            return "";
        }
        return FileName;
    }

    public void sendArchivo(String arg, SocketChannel bos) {
        ByteBuffer bRes;
        int indice = arg.indexOf(".");
        String extension = arg.substring(indice + 1, arg.length());
        System.out.println("Extension de archivo: " + extension);
        try {
            File f = new File(arg);
            if (!f.exists()) {
                System.out.println("El recurso solicitado no existe: " + arg);
                String sb = "";
//                sb = sb + "HTTP/1.0 404 Recurso no encontrado\n";
//                sb = sb + "Server: JESUS JOSE/1.0\n";
//                sb = sb + "Date: " + new Date() + " \n";
                sb = sb + "Content-Type: text/html \n";
                sb = sb + "\n<html><head><title>SERVIDOR WEB\n";
                sb = sb + "</title></head><body bgcolor=\"#AACCFF\"><center><h1><br>El recurso solicitado no se ha encontrado en el servidor, error 404</br></h1>";
                sb = sb + "</center></body></html>\n";
                bRes = ByteBuffer.wrap(sb.getBytes());
                bos.write(bRes);
            } else {
                int b_leidos = 0;
                BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(arg));
                byte[] buf = new byte[1024];
                int tam_bloque = 0;
                if (bis2.available() >= 1024) {
                    tam_bloque = 1024;
                } else {
                    bis2.available();
                }
                int tam_archivo = bis2.available();
                String sb = "";
//                sb = sb + "HTTP/1.0 200 ok\n";
//                sb = sb + "Server: JESUS JOSE/1.0 \n";
//                sb = sb + "Date: " + new Date() + " \n";
                if (extension.equals("pdf")) {
                    sb = sb + "Content-Type: application/pdf \n";
                } else if (extension.equals("jpg")) {
                    sb = sb + "Content-Type: image/jpeg \n";
                } else {
                    sb = sb + "Content-Type: text/html \n";
                }
                sb = sb + "Content-Length: " + tam_archivo + " \n";
                sb = sb + "\n";

                bRes = ByteBuffer.wrap(sb.getBytes());
                bos.write(bRes);

                while ((b_leidos = bis2.read(buf, 0, buf.length)) != -1) {
                    bRes = ByteBuffer.wrap(buf, 0, b_leidos);
                    bos.write(bRes);
                }
                bis2.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
