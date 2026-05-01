package com.iit.internship_manager.infrastucture.config;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Date;
import com.iit.internship_manager.domain.models.RendezVous;

public class ICalUtils {

    // Predictable UID based on DB ID ensures the Cancel finds the Invite
    private static Uid getDeterministicUid(Long rdvId) {
        return new Uid("meeting-" + rdvId + "@iit.tn");
    }

    public static byte[] generateMeetingInvite(RendezVous rdv) {
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//IIT Sfax//InternshipManager//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getProperties().add(Method.PUBLISH); // Standard for new invites

        Date startDate = Date.from(rdv.getDateHeure().atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(rdv.getDateHeure().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());

        VEvent meetingEvent = new VEvent(new DateTime(startDate), new DateTime(endDate), rdv.getObjet());
        meetingEvent.getProperties().add(new Location(rdv.getLieu()));
        meetingEvent.getProperties().add(getDeterministicUid(rdv.getId()));
        meetingEvent.getProperties().add(new Sequence(0)); // Initial version

        calendar.getComponents().add(meetingEvent);
        return calendar.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] generateMeetingCancellation(RendezVous rdv) {
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//IIT Sfax//InternshipManager//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(Method.CANCEL); // Required for removal

        Date startDate = Date.from(rdv.getDateHeure().atZone(ZoneId.systemDefault()).toInstant());
        // For cancellations, only the start date and the UID are strictly required
        VEvent event = new VEvent(new DateTime(startDate), rdv.getObjet());

        event.getProperties().add(getDeterministicUid(rdv.getId()));
        event.getProperties().add(new Sequence(1)); // Must be higher than the invite
        event.getProperties().add(Status.VEVENT_CANCELLED);

        calendar.getComponents().add(event);
        return calendar.toString().getBytes(StandardCharsets.UTF_8);
    }
}