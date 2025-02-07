package com.parkit.parkingsystem.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static ParkingService parkingService;

    private static String vehicleRgNumber = "ABCDEF";

    private void createTicket(ParkingSpot parkingSpot) {
        Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(vehicleRgNumber);
        ticket.setPrice(0);
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setOutTime(null);
        ticketDAO.saveTicket(ticket);
    }

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRgNumber);
        dataBasePrepareService.clearDataBaseEntries();
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @AfterEach
    private void after() throws Exception {
        parkingService = null;
    }
    
    @AfterAll
    private static void tearDown(){
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void testParkingACar(){
        // GIVEN
        assertNull(ticketDAO.getTicket(vehicleRgNumber)); // ticket should not exist yet
        assertEquals(1, parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)); // parkingSpot 1 should be avaiblable
        
        // WHEN
        parkingService.processIncomingVehicle();

        // THEN
        Ticket ticket = ticketDAO.getTicket(vehicleRgNumber);
        assertEquals(ticket.getVehicleRegNumber(), vehicleRgNumber);
        
        int parkingSpotAvailableNb = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertEquals(2, parkingSpotAvailableNb); // Spot 1 is not anymore available
    }

    @Test
    public void testParkingLotExit(){
        // GIVEN
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        createTicket(parkingSpot);

        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        Ticket ticket = ticketDAO.getTicket(vehicleRgNumber);
        assertEquals(1.5, ticket.getPrice()); // Price for 1 hour for a CAR

        assertNotNull(ticket.getOutTime());
    }

    @Test
    public void testParkingLotExitRecurringUser() {
        // GIVEN
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        createTicket(parkingSpot);
        createTicket(parkingSpot);

        // WHEN
        parkingService.processExitingVehicle();

        // THEN
        Ticket ticket = ticketDAO.getTicket(vehicleRgNumber);
        assertEquals(1.425, ticket.getPrice()); // Price for 1 hour for a CAR with a discount
    }

}
