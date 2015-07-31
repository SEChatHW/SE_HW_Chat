
import java.net.*;
import java.io.*;
import java.util.*;

public class ChatClientHandler extends Thread{
    Socket socket;
    BufferedReader in;
    BufferedWriter out ;
    List clients;
    List ngUser = new ArrayList();//rejectされたユーザを格納するlist
    String name;
    
    ChatSaba chat = new ChatSaba();//ChatSaba型のオブジェクトを呼び出しておく
    
    ChatClientHandler(Socket socket,List clients){
	this.socket = socket;
	this.clients = clients;
	this.name = "undefined" + chat.getClientNum();//接続してきた順番を名前にする
    }
    
     /*講義内で作製したそのまま↓*/
    void open() throws IOException{
InputStream socketIn = socket.getInputStream();
OutputStream socketOut = socket.getOutputStream();
in = new BufferedReader(new InputStreamReader(socketIn));
out = new BufferedWriter(new OutputStreamWriter(socketOut));
    }
    String receive() throws IOException{
String line = in.readLine();
System.out.println(line);
return line;
    }
    void send(String message) throws IOException{
out.write(message);
out.write("\r\n");
out.flush();
    }
    void close(){
if(in != null){try{in.close();}catch(IOException e){}}
if(out != null){try{out.close();}catch(IOException e){}}
if(socket != null){try{socket.close();}catch(IOException e){}}
    }
    /*講義内で作製したそのまま↑*/

    public void run(){
	try{
	    open();
	    while(true){
		String message = receive();
		String[] commands = message.split(" ");//入力をスペースで区切る
		/*入力に応じてそれぞれのコマンドを呼び出す*/	
	    }//while
	}catch(IOException e){
	    e.printStackTrace();
	}finally{
	    close();
	}
}
    


 
    
 
 
}//class