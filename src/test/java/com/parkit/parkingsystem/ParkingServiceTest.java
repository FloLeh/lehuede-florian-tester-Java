package com.parkit.parkingsystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    private Ticket ticket;

    private void readVehicleRegistrationNumberMock() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @BeforeEach
    private void setUpPerTest() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");

        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @Test
    public void processExitingVehicleTest(){
        readVehicleRegistrationNumberMock();
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

        parkingService.processExitingVehicle();

        verify(ticketDAO, Mockito.times(1)).getTicket("ABCDEF");
        verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void testProcessIncomingVehicle() {
        readVehicleRegistrationNumberMock();
        when(inputReaderUtil.readSelection()).thenReturn(1); // Vehicle type 1 (CAR)
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

        parkingService.processIncomingVehicle();

        verify(ticketDAO, Mockito.times(1)).getNbTicket("ABCDEF");
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() {
        readVehicleRegistrationNumberMock();
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);
        
        parkingService.processExitingVehicle();

        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class)); // Do not update parking when update ticket is false
    }

    @Test
    public void testGetNextParkingNumberIfAvailable () {
        when(inputReaderUtil.readSelection()).thenReturn(1); // Vehicle type 1 (CAR)
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        assertEquals(1, parkingSpot.getId());
        assertEquals(true, parkingSpot.isAvailable());
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        when(inputReaderUtil.readSelection()).thenReturn(1); // Vehicle type 1 (CAR)
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0); // No parking spot available

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertEquals(null, parkingSpot);
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        when(inputReaderUtil.readSelection()).thenReturn(3); // Wrong argument

        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertEquals(null, parkingSpot);
    }

}
