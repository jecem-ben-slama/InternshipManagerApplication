package com.iit.internship_manager.infrastucture.config;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Date;
import java.net.URISyntaxException; // Added import
import com.iit.internship_manager.domain.models.RendezVous;

public class ICalUtils {

    private static Uid getDeterministicUid(Long rdvId) {
        return new Uid("meeting-" + rdvId + "@iit.tn");
    }

    public static byte[] generateMeetingInvite(RendezVous rdv) {
        try {
            Calendar calendar = new Calendar();
            calendar.getProperties().add(new ProdId("-//IIT Sfax//InternshipManager//EN"));
            calendar.getProperties().add(Version.VERSION_2_0);
            calendar.getProperties().add(CalScale.GREGORIAN);
            calendar.getProperties().add(Method.PUBLISH);

            Date startDate = Date.from(rdv.getDateHeure().atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(rdv.getDateHeure().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());

            VEvent meetingEvent = new VEvent(new DateTime(startDate), new DateTime(endDate), rdv.getObjet());
            
            // Fixed: Wrapped in try-catch to handle URISyntaxException
            meetingEvent.getProperties().add(new Organizer("mailto:benslemajecem@gmail.com"));
            
            meetingEvent.getProperties().add(new Location(rdv.getLieu()));
            meetingEvent.getProperties().add(getDeterministicUid(rdv.getId()));
            meetingEvent.getProperties().add(new Sequence(0));

            calendar.getComponents().add(meetingEvent);
            return calendar.toString().getBytes(StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to create iCal Organizer URI", e);
        }
    }

    public static byte[] generateMeetingCancellation(RendezVous rdv) {
        try {
            Calendar calendar = new Calendar();
            calendar.getProperties().add(new ProdId("-//IIT Sfax//InternshipManager//EN"));
            calendar.getProperties().add(Version.VERSION_2_0);
            calendar.getProperties().add(Method.CANCEL); 

            Date startDate = Date.from(rdv.getDateHeure().atZone(ZoneId.systemDefault()).toInstant());
            VEvent event = new VEvent(new DateTime(startDate), rdv.getObjet());

            // Fixed: Wrapped in try-catch to handle URISyntaxException
            event.getProperties().add(new Organizer("mailto:benslemajecem@gmail.com"));
            
            event.getProperties().add(getDeterministicUid(rdv.getId()));
            event.getProperties().add(new Sequence(1)); 
            event.getProperties().add(Status.VEVENT_CANCELLED);

            calendar.getComponents().add(event);
            return calendar.toString().getBytes(StandardCharsets.UTF_8);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to create iCal Cancellation URI", e);
        }
    }
}