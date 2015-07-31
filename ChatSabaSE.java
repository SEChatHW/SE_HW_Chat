import java.net.*;
import java.io.*;
import java.util.*;

public class ChatSaba{
    private ServerSocket server;
    private List clients = new ArrayList();
    private static int clientsCount = 1;//接続してきた人数を数えるための変数
    public void listen(){
	try{
	    server = new ServerSocket(18080);
	    System.out.println("Echoサーバをポート18080で起動しました.");
	    while(true){
		Socket socket = server.accept();
		ChatClientHandler handler = new ChatClientHandler(socket,clients);
		clientsCount++;//接続してきた人数を数える
		clients.add(handler);
		System.out.println("クライアントが接続してきました.");
		
		handler.start();
	    }
	}catch(IOException e){
	    e.printStackTrace();
	}
    }
    /*接続してきた人数返すメソッド。名前が被らない様にするためにメソッドを使用*/
    public int getClientNum(){
	return clientsCount;
    }
    public static void main(String[] args){
	ChatSaba chat = new ChatSaba();
	chat.listen();
    }//main
}//class

