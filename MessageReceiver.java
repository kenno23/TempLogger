/*
 * Name: Ken Santoso
 * Purpose: Receive messages from ESP8266 and store it in output text
 * Code borrowed from :https://github.com/ratanak1010/Java-UDP-Chat/blob/master/ChatServer.java
 * Also used Kurose Ross code from: https://systembash.com/a-simple-java-udp-server-and-udp-client/
 * encryption obtained from
 */


import java.io.*;
import java.net.*;
import java.util.*;
import java.time.*;
import java.time.format.*;


class MessageReceiver implements Runnable {
    DatagramSocket sock; //socket to receive
    byte receiveData[]; // bytes to receive
    ArrayList<String> trustedName; //name of trusted peers
    ArrayList<InetAddress> bannedList; //banned IP addresses
    /*
     * Constructor for MessageReceiver 
     * @param s Datagram socket to receive messages
     * @param trustedName a list of trusted names and IPs
     */
    MessageReceiver(DatagramSocket s, ArrayList<String> trustedName) {
        sock = s;
        receiveData = new byte[1024];
       bannedList = new ArrayList<InetAddress>();
        this.trustedName = trustedName;
    }
    
    /*
     * Runs the thread at the same time as the messagesender
     * 
     */
    
    
    public void run() {
        //run loop to listen for messages
        while (true) {
           try {
                boolean isTrusted = false;
                boolean isBanned = false;
                
                
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); //packet to receive bytes
                sock.receive(receivePacket); //receive the packet
                String id = "";   //initialise the id which is the name
                InetAddress IP = InetAddress.getLocalHost();
                byte[] receiveData = receivePacket.getData();
          
                String received = new String(receiveData); //create a String using the bytes received
                InetAddress clientAddress = receivePacket.getAddress(); //get the address of who is sending the message
                int clientPort = receivePacket.getPort(); //get the port of who is sending the message
                String ip = clientAddress.getHostAddress(); //get the ip into a String
                

                    //if they are not banned then proceed here
             if(!isBanned)
             {      //check the trusted list for this ip
                 for(String s:trustedName)
                 {
                     String details[] = s.split(":"); //split the name and ip
                     if(ip.equals(details[1])) //if the ip is in the list then give them a name and trust this ip
                     
                     {
                         id = details[0]+",";
                         isTrusted = true;
 
                        }                
                    }
 
                
                
                 //if they are not trusted, then display the message once             
            if(!isTrusted)
            {
                
                String refused = "Refused connection from" + " " + clientAddress.getHostAddress();
                System.out.println(refused);
               bannedList.add(clientAddress); //add the address to the banned list
                
            }
            
            else if(ip.equals(IP.getHostAddress()) ){
                
                
                
            }
            
            else{ //otherwise print out the message to the user
                    
                    File file = new File("output.txt");
                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    String time = now.format(formatter);
                    if(!file.exists()){
                        file.createNewFile();
                    }
                    FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(id+time+","+received.trim()+";");
                    bw.write("\r\n");
                    bw.close();
                  System.out.println(id+time+","+received.trim()+",");
                  
                  
                  
            }
            
        }
            
                            
                            } catch(Exception e) {
                                System.err.println(e);
                            }
        }
    }
}