package models;

import java.net.MalformedURLException;
import java.rmi.*;
import java.util.ArrayList;
import java.util.Collection;

public class Client {

    ServerInterface server;
    public Plane plane;
    private Collection<Station> stations;


    public void updateStation(Station s){
        try {
            server.updateStation(s);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean connect(String IP){

        try {

            server = (ServerInterface) Naming.lookup("rmi://"+IP+":1099/MyServer");
            System.out.println("haloo");
            System.out.println(server.getAllStations());
            stations=server.getAllStations();
            return true;
        } catch (NotBoundException | RemoteException | MalformedURLException ignored) {
            System.out.println("ERR con");
            return false;
        }
    }
    public void sendPlane(Plane p){
        try {
            server.bindClientPlane(p);
            plane =p;

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
    public void waitFlightCommand(Plane p){
        while (true) {
            try {

                Flight currentFlight = server.waitForFlight(p.getIdPlane());

                currentFlight.startStation().removePlane(plane.getIdPlane());
                updateStation(currentFlight.startStation());

                for (int i = 1; i < currentFlight.visitedStations.size(); i++) {
                    plane.moveTo(currentFlight.visitedStations.get(i).getPosition(), server);

                }
                currentFlight.destinationStation().addPlane(plane);
                updateStation(currentFlight.destinationStation());
                server.despawnPlane(p.getIdPlane());

            } catch (RemoteException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        try {


            // Lookup the server object
            ServerInterface server = (ServerInterface) Naming.lookup("rmi://localhost:1099/MyServer");


            // testing **
            Position pos = new Position(45.0,44.0);
            Plane p1 = new Plane(55.0, 456.0, 55.0, 55.0, 45.0,pos);
            // *****
            System.out.println(server.getAllStations());
            // Send the object to the server
            server.bindClientPlane(p1);

            System.out.println("client here" + p1.getIdPlane());


            while(true) {
                // wait for Flight signal
                Flight flight = server.waitForFlight(p1.getIdPlane());

                // while there's no flight keep waiting
                while (flight == null) {
                    Thread.sleep(1000);
                    flight = server.waitForFlight(p1.getIdPlane());
                }

                System.out.println("i'm unlocked");
                Flight currentFlight = server.getFlight();

                for (int i = 0; i < currentFlight.visitedStations.size(); i++) {
                    p1.moveTo(currentFlight.visitedStations.get(i).getPosition(), server);

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Collection<Station> getStations() {
        return stations;
    }
}
