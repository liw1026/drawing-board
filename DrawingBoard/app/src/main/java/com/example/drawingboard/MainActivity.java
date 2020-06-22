package com.example.drawingboard;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    private PaintView paintView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
//        initMenu();
    }

    private void initView() {
        paintView = (PaintView) findViewById(R.id.activity_paint);
        new PenConnection().execute();
        new PaletteConnection().execute();
    }

    private class PenConnection extends AsyncTask<Void, Void, Void> {

        private String mAddress = "192.168.0.243";
        private int port = 2390;
        private String msg = "Connecting";

        protected Void doInBackground(Void... voids) {

            byte[] bytes = msg.getBytes();


            try {

                InetAddress address = InetAddress.getByName(mAddress);

                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                System.out.println("Connecting...");

                socket.send(packet);

                while(true){
                    final byte[] receive = new byte[1024];
                    DatagramPacket receiverPacket = new DatagramPacket(receive, receive.length);
                    socket.receive(receiverPacket);
                    String reply = new String(receive, 0, receiverPacket.getLength());
                    setStroke(reply);
//                    System.out.println("Force: "+reply);
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private class PaletteConnection extends AsyncTask<Void, Void, Void> {

        private String mAddress = "192.168.0.243";
        private int port = 2390;
        private String msg = "Connecting";

        protected Void doInBackground(Void... voids) {

            byte[] bytes = msg.getBytes();


            try {

                InetAddress address = InetAddress.getByName(mAddress);

                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
                DatagramSocket socket = new DatagramSocket();
                System.out.println("Connecting...");

                socket.send(packet);

                while(true){
                    final byte[] receive = new byte[1024];
                    DatagramPacket receiverPacket = new DatagramPacket(receive, receive.length);
                    socket.receive(receiverPacket);
                    String reply = new String(receive, 0, receiverPacket.getLength());
                    setStroke(reply);
//                    System.out.println("Force: "+reply);
                }

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public void setStroke(String reply) {
        paintView.setStroke(Integer.parseInt(reply));
    }
    public void setColour(String reply) { paintView.setColour(Integer.parseInt(reply));}
}


