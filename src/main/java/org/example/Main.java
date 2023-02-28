package org.example;

import com.jcraft.jsch.*;
import java.io.*;


public class Main {
    public static void main(String[] args) throws JSchException, IOException {

        Server[] servers = new Server[3];

        Server svMio = new Server("192.168.100.108", "usuario", "", 22, "bubaloo");
        //Servidor del peluca servidor sv2 = new servidor("192.168.7.109", "usuario", "", 22);
        //servidor sv3 = new servidor("192.168.1.16", "usuario", "", 22);
        Server svCelu = new Server("192.168.1.8", "usuario", "", 2222, "celular");
        Server svTorre = new Server("167.61....251", "joaquin", "",7777, "torre");
        Server svTablet = new Server("192.168.1.10", "usuario", "", 2222, "tablet");
        servers[0] = svMio;
        servers[1] = svTorre;
        servers[2] = svTablet;

        JSch jsch = new JSch();
        int i;
        for (i=1; i<servers.length; i++){
            try{
                Session session = jsch.getSession(servers[i].user, servers[i].ip, servers[i].port);
                session.setPassword(servers[i].password);
                session.setConfig("StrictHostKeyChecking", "no");
                System.out.println("Conectando al servidor "+i+"...");
                session.connect();
                if (session.isConnected()){
                    System.out.println("Se conectó directamente");

                    addDirectHost(servers[i].name, servers[i].ip, servers[i].port, servers[i].user);

                    copyCertificate(session);

                }
                session.disconnect();
            } catch (JSchException e) {
                System.out.println("Error.");
                Session intermediateSession = jsch.getSession(servers[i-1].user, servers[i-1].ip, servers[i-1].port);
                intermediateSession.setPassword(servers[i-1].password);
                intermediateSession.connect();
                intermediateSession.setPortForwardingL(2222, servers[i].ip, servers[i].port);
                if (connectedToPort(2222)){
                    System.out.println("Dio el salto. Se conectó al servidor "+i);

                    addInterHost(servers[i].name, servers[i-1].name, servers[i].ip, servers[i].port, servers[i].user);
                    }
                intermediateSession.disconnect();
                } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SftpException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void mostrarStream(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        System.out.println("antes del while");
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }


    public static Boolean connectedToPort(Integer port) throws JSchException {
        Boolean connected = false;
        JSch jsch = new JSch();
        try{
            Session sessionPort = jsch.getSession("usuario", "127.0.0.1", port);
            sessionPort.setPassword("kala2615");
            sessionPort.setConfig("StrictHostKeyChecking", "no");
            sessionPort.connect();

            if(sessionPort.isConnected()){
                connected = true;
            }
            sessionPort.disconnect();

        }catch (JSchException e){
            connected = false;
        }
        return connected;
    }

    public static void addDirectHost (String name, String ip, int port, String user) throws IOException {
        String stringForConfigHost = "Host "+ name;
        String HostName ="HostName " + ip;
        String PortSSHConfig = "Port "+port;

        FileWriter writer = new FileWriter("/home/usuario/.ssh/config", true);
        writer.write(stringForConfigHost+" \n");
        writer.write(HostName+" \n");
        writer.write("User "+user+"\n");
        writer.write(PortSSHConfig+" \n");
        writer.close();
    }

    public static void addInterHost (String name, String previousName, String ip, int port, String user) throws IOException {
        FileWriter writer = new FileWriter("/home/usuario/.ssh/config", true);
        String stringForConfigHost = "Host "+name;
        String proxyCommand = "ProxyCommand ssh " +previousName+" nc " +ip+" "+ port;
        String cert = "HostKeyAlgorithms ssh-ed25519-cert-v01@openssh.com,ssh-rsa-cert-v01@openssh.com,ssh-ed25519,ssh-rsa";

        writer.write(stringForConfigHost+"\n"+proxyCommand+"\n"+cert+"\n");
        writer.close();
    }

    public static void copyCertificate(Session session) throws JSchException, SftpException {
        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.put("/home/usuario/.ssh/id_rsa.pub", "/home/joaquin/.ssh/authorized_keys");
        System.out.println("Se copió la clave pública");
        sftpChannel.disconnect();
    }
}


