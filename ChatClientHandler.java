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
    
    public String getClientName(){//名前を返すメソッド
    return name;
    }
    
    public void run(){
    try{
        open();
        while(true){
        String message = receive();
        String[] commands = message.split(" ");//入力をスペースで区切る
        /*入力に応じてそれぞれのコマンドを呼び出す*/
        if(commands[0].equalsIgnoreCase("post")){
            post(commands[1]);
        }
        else if(commands[0].equals("bye")){
            bye();
        }
        else if(commands[0].equalsIgnoreCase("tell")){
            tell(commands[1],commands[2]);
        }
        else if(commands[0].equalsIgnoreCase("name")){
            name(commands[1]);
        }
        else if(commands[0].equalsIgnoreCase("whoami")){
            whoami();
        }
        else if(commands[0].equalsIgnoreCase("users")){
            users();
        }
        else if(commands[0].equalsIgnoreCase("help")){
            help();
        }
        else if(commands[0].equalsIgnoreCase("reject")){
            reject(commands[1]);
        }
        
        }//while
    }catch(IOException e){
        e.printStackTrace();
    }finally{
        close();
    }
    }
    
    public void post(String message)throws IOException{//講義内で作製したそのまま
    //接続しているクライアント全員にmessageを送信
    List names = new ArrayList();
    String[] ngName = new String[clients.size()];//NGユーザの名前を格納
    int ngCount=0;
    for(int i = 0; i < clients.size();i++){
        ChatClientHandler handler = (ChatClientHandler)clients.get(i);
        if(handler != this){
        /*誰に送信できたか、できなかったかを知らせる
          NGユーザに登録されていない人にのみ送信
         */     
        if(handler.ngUser.indexOf(this) != -1){
            send(handler.getClientName()+"にメッセージを送信できませんでした");
            ngName[i] = " ";//送信できなければ名前を入れない
            ngCount++;
        }else{      
            names.add(handler.getClientName());//クライアントの名前を格納
            handler.send("["+this.getClientName() + "]"+message);
            ngName[i] = handler.getClientName();//送信できれば名前を格納
        }
        }
    }
    if(ngCount == clients.size()-1){//誰にも送信できなかった場合のエラーメッセージ
        send("no one received message");
        return;
    }
    Collections.sort(names);//名前をソート
    String returnMessage = "";
    /*送信に成功したユーザの名前だけを返す*/
    for(int i = 0; i < names.size();i++){
        for(int j = 0; j < clients.size(); j++){
        if(names.get(i).equals(ngName[j])){//送信できたユーザかどうかを比較
            returnMessage = returnMessage + names.get(i)+",";   
        }
        }
    }
    this.send(returnMessage);
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
    void bye()throws IOException{
    /*チャットを終了し、メンバーから抜ける*/
    List names = new ArrayList();
    for(int i = 0; i < clients.size();i++){
        //クライアント毎のオブジェクトを呼び出す
        ChatClientHandler handler = (ChatClientHandler)clients.get(i);
        if(handler != this){
        names.add(handler.getClientName());
        handler.send("["+this.getClientName() + "]"+"さんが退室しました。");//○○○がチャットを終了したというメッセージを返す            
        }else{
        /*自分を見つけたらチャットを終了し、リストからも消去*/
        names.add(handler.getClientName());
        String returnMessage = "チャットを終了します";//終了メッセージを返す
        this.send(returnMessage);
        
        handler.remove(i);//リストから自分を消去
        
        }
    }
    
    close();//チャットを終了
    }//bye
    /*リストから消去するためのメソッド*/
    public void remove(int index){//講義用twitterアカウントに載せられていた内容を参照
    clients.remove(index);
    }
    
    void tell(String name,String message)throws IOException{
    /*指定されたクライアントにmessageを送信*/
    List names = new ArrayList();//クライアントの名前を格納するリスト
    String adName;
    int flg = 0;    
    
    /*クライアント毎のオブジェクトを呼び出す*/
    for(int i = 0;i < clients.size();i++){
        ChatClientHandler handler = (ChatClientHandler)clients.get(i);
        if(handler != this){
        names.add(handler.getClientName());//クライアントの名前を格納
        adName = handler.getClientName();
        if(adName.equalsIgnoreCase(name)){//送り先が見つかれば送信
            /*NGユーザに登録されていればエラーメッセージを返す*/
            if(handler.ngUser.indexOf(this) != -1){
            send("no one received message");
            return;//メッセージを送信せずに処理を終了させる
            }
            handler.send("["+this.getClientName()+"->"+name+"]"+message);//誰から誰へ送っているのかを表示
            flg = 1;
        }    
        
        }//if
    }//for
    /*送り先に指定したユーザが存在しなかった場合のエラーメッセージ*/
    if(flg == 0){
        this.send("指定されたユーザは存在しません");
        return;
    }
    Collections.sort(names);//名前をソート
    String returnMessage = "";
    for(int i = 0;i < names.size();i++){
        if(names.get(i) == name){//誰に届いたかを返す
        returnMessage = returnMessage + names.get(i);
        }
    }
    this.send(returnMessage);
    }//tell
    
    void name(String newName)throws IOException{
    /*自分の名前を変更するメソッド*/
    List names = new ArrayList();
    int flg = 0;
    for(int i = 0;i < clients.size();i++){
        ChatClientHandler handler = (ChatClientHandler)clients.get(i);
        if(handler != this){
        names.add(handler.getClientName());
        /*既に使用されている名前ならエラーメッセージを返す(名前は変更しない)*/
        if(handler.getClientName().equalsIgnoreCase(newName)){
            String returnMessage = "この名前は他のユーザが使用しています"; 
            this.send(returnMessage);
            flg = 1;
        }
        }
    }//for
    if(flg == 0){
        this.name = newName;//現在の名前を更新したい名前に変更
    }
    }//name
    
    void whoami()throws IOException{
    /*自分の名前を返すメソッド*/
    String nowName = getClientName();
    this.send(nowName);
    }//whoami
    
    
    void users()throws IOException{
    /*現在接続しているクライアントの名前を全て返す*/
    List names = new ArrayList();
    for(int i = 0; i < clients.size();i++){
        ChatClientHandler handler = (ChatClientHandler)clients.get(i);
        names.add(handler.getClientName());//namesに名前を格納
    }
    Collections.sort(names);//名前をソート
    String returnMessage = "";
    /*名前の一覧を表示*/
    for(int i = 0; i < names.size();i++){
        returnMessage = returnMessage + names.get(i)+",";   
    }
    this.send(returnMessage);
    }//users
    
    void help()throws IOException{
    /*使用できるコマンドを表示*/
    String returnMessage;
    returnMessage = "HELP, NAME, WHOAMI, USERS, BYE, POST, TELL, REJECT";
    this.send(returnMessage);
    }//help
    
    void reject(String rName)throws IOException{
    /*受け取りたくない相手からのメッセージを受け取らなくするメソッド*/
    List names = new ArrayList();
    for(int i = 0;i < clients.size();i++){
        ChatClientHandler handler = (ChatClientHandler)clients.get(i);
        if(handler != this){
        names.add(handler.getClientName());
        /*受け取りたくないクライアントをリストに追加*/
        if(handler.getClientName().equalsIgnoreCase(rName)){
            send(rName+"をブロックしました");
            ngUser.add(handler);//リストにクライアントを追加
        }
        }//if
    }//for
    
    
    }//reject
    
}//class