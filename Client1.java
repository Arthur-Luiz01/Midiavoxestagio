import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client1 extends JFrame implements ActionListener, KeyListener{
    private static final long serialVersionUID = 1L;
    private JTextArea texto;
    private JTextField txtMsg;
    private JButton btnSend;
    private JButton btnSair;
    private JLabel lblHistorico;
    private JLabel lblMsg;
    private JPanel pnlContent;
    private Socket socket;
    private OutputStream ou ;
    private Writer ouw; 
    private BufferedWriter bfw;
    private JTextField txtIP;
    private JTextField txtPorta;
    private JTextField txtNome;
    private String msg;
    Data data = new Data();
    public static void main(String []args) throws IOException{
               
        Client1 app = new Client1();
        app.conectar();
        app.escutar();
     }
public Client1() throws IOException{                  
    JLabel lblMessage = new JLabel("Verificar!");
    txtIP = new JTextField("127.0.0.1");
    txtPorta = new JTextField("1234");
    txtNome = new JTextField("Cliente");                
    Object[] texts = {lblMessage, txtIP, txtPorta, txtNome };  
    JOptionPane.showMessageDialog(null, texts);              
     pnlContent = new JPanel();
     texto = new JTextArea(10,20);
     texto.setEditable(false);
     texto.setBackground(new Color(240,240,240));
     txtMsg = new JTextField(20);
     txtMsg.setDocument( new CaracterLimite(100));
     lblHistorico = new JLabel("Chat Midiavox");
     lblMsg = new JLabel("Mensagem");
     btnSend = new JButton("Enviar");
     btnSend.setToolTipText("Enviar Mensagem");
     btnSair= new JButton("Sair");
     btnSair.setToolTipText("Sair do Chat");
     btnSend.addActionListener(this);
     btnSair.addActionListener(this);
     btnSend.addKeyListener(this);
     txtMsg.addKeyListener(this);
     JScrollPane scroll = new JScrollPane(texto);
     texto.setLineWrap(true);  
     pnlContent.add(lblHistorico);
     pnlContent.add(scroll);
     pnlContent.add(lblMsg);
     pnlContent.add(txtMsg);
     pnlContent.add(btnSair);
     pnlContent.add(btnSend);
     pnlContent.setBackground(Color.LIGHT_GRAY);                                 
     texto.setBorder(BorderFactory.createEtchedBorder(Color.BLUE,Color.BLUE));
     txtMsg.setBorder(BorderFactory.createEtchedBorder(Color.BLUE, Color.BLUE));            
     setTitle(txtNome.getText());
     setContentPane(pnlContent);
     setLocationRelativeTo(null);
     setResizable(false);
     setSize(250,300);
     setVisible(true);
     setDefaultCloseOperation(EXIT_ON_CLOSE);
}
public void conectar() throws IOException{
                           
    socket = new Socket(txtIP.getText(),Integer.parseInt(txtPorta.getText()));
    ou = socket.getOutputStream();
    ouw = new OutputStreamWriter(ou);
    bfw = new BufferedWriter(ouw);
    bfw.write(txtNome.getText()+"\r\n");
    bfw.flush();
    
  }
  
public void enviarMensagem(String msg) throws IOException{     
    if(msg.equals("Sair")){
      bfw.write("Desconectado \r\n");
      texto.append("Desconectado \r\n");
    } else if(msg.equals("")){
        new Thread(data).start();
    }else{
      bfw.write(msg+"\r\n");
      texto.append( txtNome.getText() + ": " +         txtMsg.getText()+"\r\n");
      data.Interrupt();
    }
     bfw.flush();
     txtMsg.setText("");        
     
}
public void escutar() throws IOException{  
    InputStream in = socket.getInputStream();
    InputStreamReader inr = new InputStreamReader(in);
    BufferedReader bfr = new BufferedReader(inr);                 
     while(!"Sair".equalsIgnoreCase(msg)) 
        if(bfr.ready()){
          msg = bfr.readLine();
        if(msg.equals("Sair"))
          texto.append("Servidor caiu! \r\n");
         else
          texto.append(msg+"\r\n"); 
         }
 }
 public void sair() throws IOException{
                          
    enviarMensagem("Sair");
    bfw.close();
    ouw.close();
    ou.close();
    socket.close();
 }
 @Override
public void actionPerformed(ActionEvent e) {
    try {
     if(e.getActionCommand().equals(btnSend.getActionCommand()))
        enviarMensagem(txtMsg.getText());
        
     else
        if(e.getActionCommand().equals(btnSair.getActionCommand()))
        sair();
     } catch (IOException e1) {
          e1.printStackTrace();
     }                       
}
@Override
public void keyPressed(KeyEvent e) {                      
    if(e.getKeyCode() == KeyEvent.VK_ENTER){
       try {
          enviarMensagem(txtMsg.getText());
       } catch (IOException e1) {
           e1.printStackTrace();
       }                                                          
   }                       
}
    
@Override
public void keyReleased(KeyEvent arg0) {             
}
    
@Override
public void keyTyped(KeyEvent arg0) {            
}
 public class CaracterLimite extends PlainDocument{
    private int tamanhoMax = 100;
    public CaracterLimite(int tamanhoMax){
        this.tamanhoMax = tamanhoMax;
    }
    @Override
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null) return;  
                    
             String stringAntiga = getText (0, getLength() );  
             int tamanhoNovo = stringAntiga.length() + str.length(); 
                        
             if (tamanhoNovo <= tamanhoMax) {  
                 super.insertString(offset, str , attr);  
             } else {    
                 super.insertString(offset, "", attr); 
             }  
        }
    }
    class Data implements Runnable{
        Date data = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss\n");
        String dataFormatada = dateFormat.format(data);
        private volatile boolean isRuning; 
        long TimeStop;
        long padrao = 60000;
         
        public void run(){
            isRuning = true;
              try {
                while(true){
                    isRuning = true;
                    long TimeStart = new Date().getTime();
                    long wait = padrao - (TimeStart-TimeStop);
                    if(TimeStop == 0 || wait <= 0 || wait >60000)
                    wait = padrao;
                    Thread.sleep(wait);
                    if(isRuning){
                        data = new Date();
                        dataFormatada = dateFormat.format(data);
                        texto.append(dataFormatada);
                    }
                } 
               }catch (InterruptedException e) {

             }
            }
               public void Interrupt() {
                   isRuning = false;
                   TimeStop = new Date().getTime();
                   System.out.println(dateFormat.format(TimeStop));
               }
    }
}

   


