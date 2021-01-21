/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidorwebsnb;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import jdk.nashorn.internal.ir.RuntimeNode;

/**
 *
 * @author J.PEREZ
 */
public class ServidorWebSNB {

    public static void main(String[] args) {
        int puerto = 9999;
        Peticion p;
      
        try{
            ServerSocketChannel s = ServerSocketChannel.open();
            s.configureBlocking(false);//Cambiar a MODO NO BLOQUEANTE
            s.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            InetSocketAddress I = new InetSocketAddress(puerto);//Para almacenar la info del socket
            s.socket().bind(I);

            Selector sel = Selector.open();//Para las operaciones E/S (Connect, )
            s.register(sel, SelectionKey.OP_ACCEPT);
            
            System.out.println("Servidor Iniciado ...");
            
            for(;;){
                sel.select();//Consultar conexiones
                
                Iterator<SelectionKey> it = sel.selectedKeys().iterator();//Iterador

                while(it.hasNext()){
                    SelectionKey k = (SelectionKey)it.next();
                    it.remove();//Borrar del iterador

                    if(k.isAcceptable()){//Si esa operacion E/S es un Accept
                        SocketChannel cl = s.accept();
                        System.out.println("Cliente aceptado desde: "+
                                cl.socket().getInetAddress().getHostAddress()+
                                ":"+cl.socket().getPort());
                        cl.configureBlocking(false);//Cambiar conexion aceptada a MODO NO BLOQ
                        cl.register(sel, SelectionKey.OP_READ);
                        continue;
                    }
                    if(k.isReadable()){
                        String statusLine;
                        
                        SocketChannel ch = (SocketChannel)k.channel();//Obtener el descriptor
                        ByteBuffer b = ByteBuffer.allocate(1000);//Tamanio del buffer 1000
                        b.clear();//Limpiar buffer

                        p = new Peticion();
                        p.Peticion(ch, b);
                        
                        ch.close();
                        continue;
                    }else if(k.isWritable()){
                        SocketChannel ch = (SocketChannel)k.channel();//Obtener el descriptor
                        String msj = "Hola Mundo";
                        byte[] b = msj.getBytes();
                        ByteBuffer buf = ByteBuffer.wrap(b);
                        ch.write(buf);
                        continue;
                    }
                }

            }
        }catch(IOException e){
        }
    }
    
}
