package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double inHour = ticket.getInTime().getTime();
        double outHour = ticket.getOutTime().getTime();

        // Duration in hours with decimal
        double duration = (outHour - inHour) / (1000 * 60 * 60);

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                if (duration > 0.5) {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                }
                else {
                    ticket.setPrice(0.);
                }
                break;
            }
            case BIKE: {
                if (duration > 0.5) {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                }
                else {
                    ticket.setPrice(0.);
                }
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}